/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2025 Michael Vorburger
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.vorburger.exec.ManagedProcessException;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DB}.
 */
public class DBTest {

    @Test
    public void testCreateDBEscaping() throws ManagedProcessException {
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        TestDB db = new TestDB(configBuilder.build());

        db.createDB("normalDB");
        assertEquals("create database if not exists `normalDB`;", db.lastCommand);

        db.createDB("db`with`backticks");
        assertEquals("create database if not exists `db``with``backticks`;", db.lastCommand);

        db.createDB("dbWith;Semicolon");
        assertEquals("create database if not exists `dbWith;Semicolon`;", db.lastCommand);
    }

    @Test
    public void testCreateDBWithCredentialsEscaping() throws ManagedProcessException {
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        TestDB db = new TestDB(configBuilder.build());

        db.createDB("another`DB", "user", "pass");
        assertEquals("create database if not exists `another``DB`;", db.lastCommand);
    }

    @Test
    public void testCreateDBBlankThrows() {
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        TestDB db = new TestDB(configBuilder.build());

        assertThrows(IllegalArgumentException.class, () -> db.createDB(""));
        assertThrows(IllegalArgumentException.class, () -> db.createDB(null));
        assertThrows(IllegalArgumentException.class, () -> db.createDB("  "));
    }

    private static class TestDB extends DB {
        String lastCommand;

        protected TestDB(DBConfiguration config) {
            super(config);
        }

        @Override
        public void run(String command, String username, String password, String dbName) throws ManagedProcessException {
            this.lastCommand = command;
        }
    }
}
