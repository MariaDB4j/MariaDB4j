/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2014 Michael Vorburger
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
package ch.vorburger.mariadb4j.tests.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.junit.MariaDB4jExtension;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

class MariaDB4jUnitExtensionInitDBTest {

    private static final String DB_NAME = "junittest";

    @RegisterExtension
    private static final MariaDB4jExtension dbRule =
            new MariaDB4jExtension(
                    DBConfigurationBuilder.newBuilder(),
                    DB_NAME,
                    "ch/vorburger/mariadb4j/basicSource.sql");

    @Test
    void validateSourceInitialization() throws SQLException {
        String connString = dbRule.getURL();
        Connection conn = DriverManager.getConnection(connString, "root", "");
        QueryRunner qr = new QueryRunner();

        // Should be able to create a new table
        List<String> results = qr.query(conn, "SELECT * FROM test;", new ColumnListHandler<>(2));
        assertEquals(2, results.size());
        assertEquals("John Doe", results.get(0));
        assertEquals("Jane Doe", results.get(1));
    }
}
