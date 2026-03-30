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

import static com.google.common.truth.Truth.assertWithMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class BasicUsageIT {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void openDatabase() throws Exception {
        int port = Integer.parseInt(System.getProperty("mariadb4j.port"));
        assertWithMessage("expect positive port value: " + port).that(port).isGreaterThan(0);
        String jdbcUrl = "jdbc:mariadb://localhost:" + port + "/foo";
        assertWithMessage("database url in system properties")
                .that(System.getProperty("mariadb.databaseurl"))
                .isEqualTo(jdbcUrl);
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'bar'")) {
                List<String> tables = new ArrayList<>();
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
                assertWithMessage("table names")
                        .that(tables)
                        .isEqualTo(Collections.singletonList("bar"));
            }
        }
    }
}
