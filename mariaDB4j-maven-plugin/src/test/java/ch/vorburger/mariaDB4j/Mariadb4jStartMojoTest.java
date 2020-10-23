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
package ch.vorburger.mariaDB4j;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.StartMojo;
import ch.vorburger.mariadb4j.utils.DBSingleton;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLNonTransientConnectionException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


/**
 * Mariadb4jStartMojoTest to ensure mariaDB4j-maven-plugin works like previous version by mike10004.
 *
 * @author mike10004
 * @author William Dutton
 */

public class Mariadb4jStartMojoTest {

    @SuppressWarnings("rawtypes")
    private Map pluginContext;

    @Rule
    public MojoRule mojoRule = new MojoRule() {
        @Override
        protected void after() {
            try {
                System.out.println("stopping database...");
                DBSingleton.shutdownDB();
                System.out.println("database stopped");
            } catch (ManagedProcessException e) {
                System.err.println("could not stop database");
                e.printStackTrace(System.err);
            }

        }
    };

    private static final String BASIC_TABLE_NAME = "bar";
    private static final String BASIC_DB_NAME = "foo";
    private static final String BASIC_TABLE_INSERT_STMT = "INSERT INTO bar (baz) VALUES (?)";
    private static final String BASIC_TABLE_VALUE_COLUMN = "baz";

    @Test(timeout = 500)
    public void shouldSkipIfSkipIsSet() throws Exception {
        File pom = new File(getClass().getResource( "/skip/pom.xml").toURI());
        StartMojo mojo = (StartMojo) mojoRule.lookupMojo("start", pom);
        assertThat(mojo).isNotNull();
        mojoRule.configureMojo(mojo, "mariaDB4j-maven-plugin", pom);
        mojo.execute();
        // without config and plugin context, combined with timeout, this will most certainly fail if not skipped
        try {
            DBSingleton.getDB();
            fail("database was created when it should have skipped");
        } catch ( IllegalStateException e) {
            //pass
        }
    }

    @Test
    public void basicUsage() throws Exception {
        File pom = new File(getClass().getResource( "/basic-usage/pom.xml").toURI());
        assertThat(pom.isFile()).overridingErrorMessage( "not found: %s", pom).isTrue();
        assertThat(pom.exists()).isTrue();
        StartMojo mojo = (StartMojo) mojoRule.lookupMojo("start", pom);
        assertThat(mojo).isNotNull();
        pluginContext = mojo.getPluginContext();
        if (pluginContext == null) {
            mojo.setPluginContext(pluginContext = new HashMap<>());
        }
        mojoRule.configureMojo(mojo, "mariaDB4j-maven-plugin", pom);
        String databaseName = mojo.getDatabaseName();
        assertThat(databaseName).overridingErrorMessage("databaseName(name)").isEqualTo(BASIC_DB_NAME);
        mojo.execute();
        DB db = getDb();
        assertThat(db).overridingErrorMessage("db from plugin context").isNotNull();
        System.out.println("querying database...");
        Map<String, String> vars;
        Table<Integer, String, Object> table;
        try (Connection conn = openConnection(db, BASIC_DB_NAME)) {
            vars = DbUtils.showVariables(conn, "version");
            System.out.println(vars);
            table = DbUtils.selectAll(conn, BASIC_TABLE_NAME);
            System.out.format("table pre-insertion: %s%n", table);
            try (PreparedStatement stmt = conn.prepareStatement(BASIC_TABLE_INSERT_STMT)) {
                stmt.setString(1, "a");
                stmt.execute();
                stmt.setString(1, "b");
                stmt.execute();
            }
            table = DbUtils.selectAll(conn, BASIC_TABLE_NAME);
            System.out.format("table post-insertion: %s%n", table);
        }
        assertThat(vars.size()).overridingErrorMessage("vars like version").isEqualTo(1);
        Set<Object> valueSet = ImmutableSet.copyOf(table.column(BASIC_TABLE_VALUE_COLUMN).values());
        assertThat(valueSet).overridingErrorMessage("table values").isEqualTo(ImmutableSet.of("a", "b"));
    }

    private DB getDb() {
        try {
            return DBSingleton.getDB();
        } catch ( IllegalStateException e) {
            return null;
        }
    }

    private Connection openConnection(DB db, String databaseName) throws SQLException {
        assertThat(db).isNotNull();
        String jdbcUrl = "jdbc:mysql://localhost:" + db.getConfiguration().getPort() + "/" + databaseName + "?serverTimezone=UTC";
        return DriverManager.getConnection(jdbcUrl);
    }

    private static final String UTF8MB4_TEST_DB_NAME = "charset_test";

    @Test
    public void utf8mb4() throws Exception {
        File pom = new File(getClass().getResource( "/utf8mb4/pom.xml").toURI());
        assertThat(pom.isFile()).overridingErrorMessage( "not found: %s", pom).isTrue();
        StartMojo mojo = (StartMojo) mojoRule.lookupMojo("start", pom);
        assertThat(mojo).isNotNull();
        pluginContext = mojo.getPluginContext();
        if (pluginContext == null) {
            mojo.setPluginContext(pluginContext = new HashMap<>());
        }
        mojoRule.configureMojo(mojo, "mariaDB4j-maven-plugin", pom);
        try {
            mojo.execute();
        } catch (MojoExecutionException ex) {
            boolean utf8mb4NotSupported = ex.getCause() != null
                && ex.getCause() instanceof ManagedProcessException
                && ex.getCause().getCause() != null
                && ex.getCause().getCause().getMessage().contains("'utf8mb4' is not a compiled character set");
            if (utf8mb4NotSupported)
                return;
            throw ex;
        }
        byte[] pokerHandBytes = {
                (byte) 0xf0, (byte) 0x9f, (byte) 0x82, (byte) 0xa1,
                (byte) 0xf0, (byte) 0x9f, (byte) 0x82, (byte) 0xa8,
                (byte) 0xf0,(byte) 0x9f, (byte) 0x83, (byte) 0x91,
                (byte) 0xf0, (byte) 0x9f, (byte) 0x83, (byte) 0x98,
                (byte) 0xf0, (byte) 0x9f, (byte) 0x83, (byte) 0x93,
        };
        String complexString = new String(pokerHandBytes, StandardCharsets.UTF_8);
        try (Connection conn = openConnection(getDb(), UTF8MB4_TEST_DB_NAME);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO supertext (content) VALUES (?)")) {
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
}