/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2017 Michael Vorburger
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
package ch.vorburger.mariadb4j.mariadb4jmavenplugintest.basic;

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
        assertEquals("database url in system properties", jdbcUrl, System.getProperty("mariadb.databaseurl"));
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
