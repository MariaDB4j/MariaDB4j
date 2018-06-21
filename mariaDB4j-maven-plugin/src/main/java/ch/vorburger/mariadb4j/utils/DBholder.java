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

package ch.vorburger.mariadb4j.utils;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

/**
 * Holds the database and configuration data for MarioDB4j
 *
 * @author William Dutton
 * @since 1.0.0
 */
public class DBholder {

    private static DB db;
    private static DBConfigurationBuilder configurationBuilder;

    public static DB getDB() {
        if (db == null)
            throw new IllegalStateException("db not set");
        return db;
    }

    public static void setDB(DB db) {
        DBholder.db = db;
    }

    public static DBConfigurationBuilder getConfigurationBuilder() {
        if (configurationBuilder == null)
            throw new IllegalStateException("configuration builder not set");
        return configurationBuilder;
    }

    public static void setConfigurationBuilder(DBConfigurationBuilder configurationBuilder) {
        DBholder.configurationBuilder = configurationBuilder;
    }
}
