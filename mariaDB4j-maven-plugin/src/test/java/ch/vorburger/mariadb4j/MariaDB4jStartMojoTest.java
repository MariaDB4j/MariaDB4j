/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2018 the original author or authors.
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
package ch.vorburger.mariadb4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.utils.DBSingleton;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;

import io.takari.maven.testing.TestMavenRuntime5;
import io.takari.maven.testing.TestResources5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Map;
import java.util.Set;

/**
 * Mariadb4jStartMojoTest to ensure mariaDB4j-maven-plugin works like previous version by mike10004.
 *
 * @author mike10004
 * @author William Dutton
 */
class MariaDB4jStartMojoTest {

    private static final Logger logger = LoggerFactory.getLogger(MariaDB4jStartMojoTest.class);

    @RegisterExtension
    final TestResources5 resources =
            new TestResources5("src/test/resources", "target/test-projects");

    @RegisterExtension final TestMavenRuntime5 maven = new TestMavenRuntime5();

    @AfterEach
    void stopDb() {
        try {
            logger.info("stopping database...");
            DBSingleton.shutdownDB();
            logger.info("database stopped");
        } catch (ManagedProcessException e) {
            logger.error("could not stop database", e);
        }
    }

    private static final String BASIC_TABLE_NAME = "bar";
    private static final String BASIC_DB_NAME = "foo";
    private static final String BASIC_TABLE_INSERT_STMT = "INSERT INTO bar (baz) VALUES (?)";
    private static final String BASIC_TABLE_VALUE_COLUMN = "baz";

    @Test
    void shouldSkipIfSkipIsSet() throws Exception {
        File basedir = resources.getBasedir("skip");
        maven.executeMojo(basedir, "start");
        assertThrows(IllegalStateException.class, DBSingleton::getDB);
    }

    @Test
    void basicUsage() throws Exception {
        File basedir = resources.getBasedir("basic-usage");
        maven.executeMojo(basedir, "start");

        DB db = getDb();
        assertThat(db).overridingErrorMessage("db from plugin context").isNotNull();
        logger.info("Querying database...");
        Map<String, String> vars;
        Table<Integer, String, Object> table;
        try (Connection conn = openConnection(db, BASIC_DB_NAME)) {
            vars = DbUtils.showVariables(conn, "version");
            logger.info(vars.toString());

            table = DbUtils.selectAll(conn, BASIC_TABLE_NAME);
            logger.info("Table pre-insertion: {}", table);

            try (PreparedStatement stmt = conn.prepareStatement(BASIC_TABLE_INSERT_STMT)) {
                stmt.setString(1, "a");
                stmt.execute();
                stmt.setString(1, "b");
                stmt.execute();
            }
            table = DbUtils.selectAll(conn, BASIC_TABLE_NAME);
            logger.info("Table post-insertion: {}", table);
        }
        assertThat(vars).overridingErrorMessage("vars like version").hasSize(1);
        Set<Object> valueSet = ImmutableSet.copyOf(table.column(BASIC_TABLE_VALUE_COLUMN).values());
        assertThat(valueSet)
                .overridingErrorMessage("table values")
                .isEqualTo(ImmutableSet.of("a", "b"));
    }

    private DB getDb() {
        try {
            return DBSingleton.getDB();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private Connection openConnection(DB db, String databaseName) throws SQLException {
        assertThat(db).isNotNull();
        String jdbcUrl =
                "jdbc:mariadb://localhost:"
                        + db.getConfiguration().port()
                        + "/"
                        + databaseName
                        + "?serverTimezone=UTC";
        return DriverManager.getConnection(jdbcUrl);
    }

    private static final String UTF8MB4_TEST_DB_NAME = "charset_test";

    @Test
    void utf8mb4() throws Exception {
        // Copies src/test/resources/utf8mb4 -> target/test-projects/<TestName>_utf8mb4_utf8mb4
        File basedir = resources.getBasedir("utf8mb4");

        // Start the DB via the plugin configured in utf8mb4/pom.xml
        maven.executeMojo(basedir, "start");

        // Insert and read back a complex UTF-8 string
        String complexString = getComplexString();

        try (Connection conn = openConnection(getDb(), UTF8MB4_TEST_DB_NAME);
                PreparedStatement stmt =
                        conn.prepareStatement("INSERT INTO supertext (content) VALUES (?)")) {
            stmt.setString(1, complexString);
            stmt.execute();
        }

        String retrievedValue;
        try (Connection conn = openConnection(getDb(), UTF8MB4_TEST_DB_NAME);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT content FROM supertext WHERE 1")) {
            assertThat(rs.next()).isTrue();
            retrievedValue = rs.getString(1);
        }

        assertThat(retrievedValue).isEqualTo(complexString);
    }

    private static String getComplexString() {
        byte[] pokerHandBytes = {
            (byte) 0xf0,
            (byte) 0x9f,
            (byte) 0x82,
            (byte) 0xa1,
            (byte) 0xf0,
            (byte) 0x9f,
            (byte) 0x82,
            (byte) 0xa8,
            (byte) 0xf0,
            (byte) 0x9f,
            (byte) 0x83,
            (byte) 0x91,
            (byte) 0xf0,
            (byte) 0x9f,
            (byte) 0x83,
            (byte) 0x98,
            (byte) 0xf0,
            (byte) 0x9f,
            (byte) 0x83,
            (byte) 0x93,
        };
        return new String(pokerHandBytes, StandardCharsets.UTF_8);
    }
}
