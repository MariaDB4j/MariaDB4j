package com.github.mike10004.mariadb4jmavenplugintest.basic;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class BasicUsageIT {

    @Test(timeout = 5000)
    public void openDatabase() throws Exception {
        int port = Integer.parseInt(System.getProperty("mariadb4j.port"));
        assertTrue("expect positive port value: " + port, port > 0);
        String jdbcUrl = "jdbc:mysql://localhost:" + port + "/foo";
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'bar'")) {
                List<String> tables = new ArrayList<>();
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
                assertEquals("table names", Collections.singletonList("bar"), tables);
            }
        }
    }
}