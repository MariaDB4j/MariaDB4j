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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.math.IntMath;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * DbUtils for testing
 *
 * @author mike10004
 * @author William Dutton
 */
@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
class DbUtils {
    private DbUtils() {}

    public static ImmutableMap<String, String> showVariables(Connection conn, String likeness) throws SQLException {
        checkArgument(likeness.matches("[\\w%]*"), "likeness value invalid");
        ImmutableMap.Builder<String, String> b = ImmutableMap.builder();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW VARIABLES LIKE '" + likeness + "'")) {
            while (rs.next()) {
                b.put(rs.getString(1), rs.getString(2));
            }
        }
        return b.build();
    }

    public static ImmutableTable<Integer, String, Object> selectAll(Connection conn, String table) throws SQLException {
        checkArgument(table.matches("[A-Za-z]\\w+"), "table name invalid");
        ImmutableTable.Builder<Integer, String, Object> b = ImmutableTable.builder();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `" + table + "` WHERE 1")) {
            ResultSetMetaData md = rs.getMetaData();
            int row = 0;
            while (rs.next()) {
                for (int column = 1; column <= md.getColumnCount(); column++) {
                    String columnName = md.getColumnName(column);
                    Object value = rs.getObject(column);
                    b.put(row, columnName, value);
                }
                row = IntMath.checkedAdd(row, 1);
            }
        }
        return b.build();
    }
}
