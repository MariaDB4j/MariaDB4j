/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2023 Michael Vorburger
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ch.vorburger.mariadb4j.tests;

import static ch.vorburger.mariadb4j.DBConfiguration.Executable.SERVER;
import static ch.vorburger.mariadb4j.TestUtil.buildTempDB;
import static ch.vorburger.mariadb4j.TestUtil.configureTempDB;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Tests the functioning of MariaDB4j Sample / Tutorial illustrating how to use MariaDB4j.
 *
 * @author Michael Vorburger
 * @author Michael Seaton
 */
class MariaDB4jSampleTutorialTest {

    private static final Logger logger = LoggerFactory.getLogger(MariaDB4jSampleTutorialTest.class);

    /**
     * Tests & illustrates using MariaDB4j with an existing native MariaDB binary on the host,
     * instead of one that was bundled with and extracted from a MariaDB4j JAR.
     */
    @Test
    void testLocalMariaDB(@TempDir Path tempDir) throws ManagedProcessException, SQLException {
        final String LINUX_EXECUTABLE = "/usr/sbin/mysqld";
        final String MACOS_EXECUTABLE = "/opt/homebrew/opt/mariadb@11.4/bin/mariadbd";
        final String WINDOWS_EXECUTABLE = "C:\\Program Files\\MariaDB 11.4\\bin\\mysqld.exe";

        DBConfigurationBuilder config = configureTempDB(tempDir);
        config.setUnpackingFromClasspath(false);

        if (config.isMacOS()) {
            config.setLibDir(tempDir.resolve("MariaDB4j").resolve("no-libs"));
            config.setBaseDir(
                    Path.of("/opt").resolve("homebrew").resolve("opt").resolve("mariadb@11.4"));
            config.setExecutable(SERVER, MACOS_EXECUTABLE);
        } else if (config.isWindows()) {
            config.setLibDir(tempDir.resolve("MariaDB4j").resolve("no-libs"));
            config.setBaseDir(Path.of("C:\\").resolve("Program Files").resolve("MariaDB 11.4"));
            config.setExecutable(SERVER, WINDOWS_EXECUTABLE);
        } else { // Linux
            config.setLibDir(tempDir.resolve("MariaDB4j").resolve("no-libs"));
            config.setBaseDir(Path.of("/usr"));
            config.setExecutable(SERVER, LINUX_EXECUTABLE);
        }

        // Only actually run this test if the binary is available
        Path executable = config.getExecutable(SERVER);
        if (Files.isExecutable(executable)) {
            check(tempDir, config, "mariaDB4jTest");
        } else {
            logger.warn(
                    "MariaDB4jSampleTutorialTest: Skipping testLocalMariaDB(), because {} is not executable",
                    executable);
        }
    }

    /**
     * Illustrates how to use a mysqld binary that is extracted from "embedded" binaries in JAR on
     * classpath.
     */
    @Test
    void testEmbeddedMariaDB4j(@TempDir Path tempDir) throws ManagedProcessException, SQLException {
        DBConfigurationBuilder config = configureTempDB(tempDir);
        check(tempDir, config, "mariaDB4jTest");
    }

    @SuppressWarnings("SameParameterValue")
    protected void check(Path tempDir, DBConfigurationBuilder config, String dbName)
            throws SQLException, ManagedProcessException {
        DB db = buildTempDB(tempDir, config);
        db.start();
        db.createDB(dbName);

        try (Connection conn =
                DriverManager.getConnection(db.getConfiguration().getURL(dbName), "root", "")) {
            QueryRunner qr = new QueryRunner();

            // Should be able to create a new table
            qr.update(conn, "CREATE TABLE hello(world VARCHAR(100))");

            // Should be able to insert into a table
            qr.update(conn, "INSERT INTO hello VALUES ('Hello, world')");

            // Should be able to select from a table
            List<String> results = qr.query(conn, "SELECT * FROM hello", new ColumnListHandler<>());
            assertEquals(1, results.size());
            assertEquals("Hello, world", results.get(0));

            // Should be able to source a SQL file
            db.source("ch/vorburger/mariadb4j/testSourceFile.sql", "root", null, dbName);
            db.source("ch/vorburger/mariadb4j/testSourceFile.sql", "root", "", dbName);

            results = qr.query(conn, "SELECT * FROM hello", new ColumnListHandler<>());
            assertEquals(5, results.size());
            assertEquals("Hello, world", results.get(0));
            assertEquals("Bonjour, monde", results.get(1));
            assertEquals("Hola, mundo", results.get(2));
        }
        db.stop();
    }

    @Test
    void testEmbeddedMariaDB4jWithSecurity(@TempDir Path tempDir)
            throws ManagedProcessException, SQLException {
        DBConfigurationBuilder config = configureTempDB(tempDir);
        config.setSecurityDisabled(false);
        DB db = buildTempDB(tempDir, config);
        db.start();
        // Starting with MariaDB 10.4, the root user has an invalid password.
        // We will therefore modify the root user password to a secure random string (a security
        // best practice).
        // Using the UID of the user that owns the data directory, we can execute this initial
        // bootstrapping command:
        // Note that on Windows MariaDB apparently does not implement this, still uses empty string
        // password for the
        // root user, so we can just use the root user.
        var randomRootPassword = RandomStringUtils.secureStrong().next(69, 97, 122, true, true);
        db.run(
                /* language=sql */ "ALTER USER 'root'@'localhost' IDENTIFIED BY '"
                        + randomRootPassword
                        + "';",
                config.isWindows() ? "root" : System.getProperty("user.name"),
                "");
        String dbName = "mariaDB4jTestWSecurity";
        db.createDB(dbName, "root", randomRootPassword);
        try (Connection conn =
                DriverManager.getConnection(config.getURL(dbName), "root", randomRootPassword)) {
            QueryRunner qr = new QueryRunner();

            // Should be able to create a new table
            qr.update(conn, "CREATE TABLE hello(world VARCHAR(100))");

            // Should be able to insert into a table
            qr.update(conn, "INSERT INTO hello VALUES ('Hello, world')");

            // Should be able to create a new user and grant privileges.
            qr.update(conn, "CREATE USER 'testUser'@'localhost' IDENTIFIED BY 'superSecret'");
            qr.update(
                    conn,
                    "GRANT ALL PRIVILEGES ON mariaDB4jTestWSecurity.* TO 'testUser'@'localhost'");
        }
        // reconnect with the new user
        try (Connection conn =
                DriverManager.getConnection(config.getURL(dbName), "testUser", "superSecret")) {
            QueryRunner qr = new QueryRunner();
            // Should be able to select from a table
            List<String> results = qr.query(conn, "SELECT * FROM hello", new ColumnListHandler<>());
            assertEquals(1, results.size());
            assertEquals("Hello, world", results.get(0));
        }
        db.stop();
    }

    /**
     * Tests & illustrates reopening an existing MariaDB4j database. This is useful for testing the
     * persistence of data across restarts.
     */
    @Test
    void testEmbeddedMariaDB4jReopenExisting(@TempDir Path tempDir)
            throws ManagedProcessException, SQLException {
        String dbName = "mariaDB4jTestReopen";
        DBConfigurationBuilder config = configureTempDB(tempDir);
        config.setDeletingTemporaryBaseAndDataDirsOnShutdown(false);
        DB db = buildTempDB(tempDir, config);
        db.start();
        db.createDB(dbName);
        try (Connection conn =
                DriverManager.getConnection(db.getConfiguration().getURL(dbName), "root", "")) {
            QueryRunner qr = new QueryRunner();
            qr.update(conn, "CREATE TABLE hello(world VARCHAR(100))");
            qr.update(conn, "INSERT INTO hello VALUES ('Hello, world')");
        }
        db.stop();
        db = buildTempDB(tempDir, config);
        db.start();
        try (Connection conn =
                DriverManager.getConnection(db.getConfiguration().getURL(dbName), "root", "")) {
            QueryRunner qr = new QueryRunner();
            List<String> results = qr.query(conn, "SELECT * FROM hello", new ColumnListHandler<>());
            assertEquals("Hello, world", results.get(0));
        }
        db.stop();
    }
}
