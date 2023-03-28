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

import static ch.vorburger.mariadb4j.DBConfiguration.Executable.Server;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the functioning of MariaDB4j Sample / Tutorial illustrating how to use
 * MariaDB4j.
 *
 * @author Michael Vorburger
 * @author Michael Seaton
 */
public class MariaDB4jSampleTutorialTest {

    /**
     * Tests & illustrates using MariaDB4j with an existing native MariaDB binary on
     * the host,
     * instead of one that was bundled with and extracted from a MariaDB4j JAR.
     */
    @Test
    public void testLocalMariaDB() throws Exception {
        assertExecutable("/usr/sbin/mariadbd");

        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(0); // 0 => autom. detect free port
        config.setUnpackingFromClasspath(false);
        config.setLibDir(SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/no-libs");
        config.setBaseDir("/usr");
        config.setExecutable(Server, "/usr/sbin/mariadbd");
        check(config);
    }

    private void assertExecutable(String path) {
        if (!(new File(path).canExecute())) {
            throw new IllegalStateException(path
                    + " not found/executable, but required for (only) this test; try e.g. sudo apt install mariadb-server ?");
        }
    }

    /**
     * Illustrates how to use a mariadbd binary that is extracted from "embedded"
     * binaries in JAR on classpath.
     */
    @Test
    public void testEmbeddedMariaDB4j() throws Exception {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(0); // 0 => autom. detect free port
        check(config);
    }

    protected void check(DBConfigurationBuilder config) throws SQLException, ManagedProcessException {
        DB db = DB.newEmbeddedDB(config.build());
        db.start();

        String dbName = "mariaDB4jTest"; // or just "test"
        // mariadbd out-of-the-box already has a DB named "test"
        // in case we need another DB, here's how to create it first
        db.createDB(dbName);

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(db.getConfiguration().getURL(dbName), System.getProperty("user.name"), "");
            QueryRunner qr = new QueryRunner();

            // Should be able to create a new table
            qr.update(conn, "CREATE TABLE hello(world VARCHAR(100))");

            // Should be able to insert into a table
            qr.update(conn, "INSERT INTO hello VALUES ('Hello, world')");

            // Should be able to select from a table
            List<String> results = qr.query(conn, "SELECT * FROM hello", new ColumnListHandler<String>());
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("Hello, world", results.get(0));

            // Should be able to source a SQL file
            db.source("ch/vorburger/mariadb4j/testSourceFile.sql", "root", null, dbName);
            db.source("ch/vorburger/mariadb4j/testSourceFile.sql", "root", "", dbName);
            results = qr.query(conn, "SELECT * FROM hello", new ColumnListHandler<String>());
            Assert.assertEquals(5, results.size());
            Assert.assertEquals("Hello, world", results.get(0));
            Assert.assertEquals("Bonjour, monde", results.get(1));
            Assert.assertEquals("Hola, mundo", results.get(2));
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    @Test
    public void testEmbeddedMariaDB4jWithSecurity() throws Exception {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(0); // 0 => autom. detect free port
        config.setSecurityDisabled(false);
        DB db = DB.newEmbeddedDB(config.build());
        db.start();

        String dbName = "mariaDB4jTestWSecurity"; // or just "test"
        // mariadbd out-of-the-box already has a DB named "test"
        // in case we need another DB, here's how to create it first
        // Starting with MariaDB 10.4, the root user has an invalid password
        // we will modify the root user password back to an empty string
        // Using the user that owns the data directory / uid we can execute this initial bootstrapping command
        // to give us pre 10.4 behavior for the purposes of this test
        db.run("SET PASSWORD FOR 'root'@'localhost' = PASSWORD('root');", System.getProperty("user.name"), "");
        db.createDB(dbName, "root", "root");


        Connection conn = null;
        try {
            conn = DriverManager.getConnection(config.getURL(dbName), "root", "root");
            QueryRunner qr = new QueryRunner();

            // Should be able to create a new table
            qr.update(conn, "CREATE TABLE hello(world VARCHAR(100))");

            // Should be able to insert into a table
            qr.update(conn, "INSERT INTO hello VALUES ('Hello, world')");

            // Should be able to create a new user and grant privileges.
            qr.update(conn, "CREATE USER 'testUser'@'localhost' IDENTIFIED BY 'superSecret'");
            qr.update(conn, "GRANT ALL PRIVILEGES ON mariaDB4jTestWSecurity.* TO 'testUser'@'localhost'");

            // reconnect with the new user
            conn = DriverManager.getConnection(config.getURL(dbName), "testUser", "superSecret");

            // Should be able to select from a table
            List<String> results = qr.query(conn, "SELECT * FROM hello", new ColumnListHandler<String>());
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("Hello, world", results.get(0));

        } finally {
            DbUtils.closeQuietly(conn);
        }
    }
}
