package ch.vorburger.mariadb4j.tests.junit;

import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.junit.MariaDBRule;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MariaDB4jUnitRuleInitiDBTest {

    private final String DB_NAME = "junittest";

    @Rule
    public MariaDBRule dbRule = new MariaDBRule(DBConfigurationBuilder.newBuilder().build(), DB_NAME, "ch/vorburger/mariadb4j/basicSource.sql");

    @Test
    public void validateSourceInitialization() throws SQLException {
        String connString = dbRule.getConnectionString();
        Connection conn;
        conn = DriverManager.getConnection(connString, "root", "");
        QueryRunner qr = new QueryRunner();

        // Should be able to create a new table
        List<String> results = qr.query(conn, "SELECT * FROM test;", new ColumnListHandler<>(2));
        assertEquals(2, results.size());
        assertEquals("John Doe", results.get(0));
        assertEquals("Jane Doe", results.get(1));
    }
}
