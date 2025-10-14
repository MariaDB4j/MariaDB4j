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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jspecify.annotations.NonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DbUtils for testing.
 *
 * @author mike10004
 * @author William Dutton
 */
class DbUtils {
    private DbUtils() {}

    public static ImmutableMap<@NonNull String, @NonNull String> showVariables(
            Connection conn, String likeness) throws SQLException {
        ImmutableMap.Builder<@NonNull String, @NonNull String> b = ImmutableMap.builder();
        try (PreparedStatement ps = conn.prepareStatement("SHOW VARIABLES LIKE ?")) {
            ps.setString(1, likeness);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    b.put(rs.getString(1), rs.getString(2));
                }
            }
        }
        return b.build();
    }

    public static ImmutableTable<@NonNull Integer, @NonNull String, @NonNull Object> selectAll(
            Connection conn, String table) {
        DSLContext dsl = DSL.using(conn, SQLDialect.MARIADB);
        Table<Record> t = DSL.table(DSL.name(table));
        Result<Record> result = dsl.selectFrom(t).fetch();
        ImmutableTable.Builder<@NonNull Integer, @NonNull String, @NonNull Object> b =
                ImmutableTable.builder();
        int row = 0;
        for (Record r : result) {
            int finalRow = row;
            r.intoMap().forEach((col, val) -> b.put(finalRow, col, val));
            row++;
        }
        return b.build();
    }
}
