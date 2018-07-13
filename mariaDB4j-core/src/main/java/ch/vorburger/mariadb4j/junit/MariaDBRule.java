/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2018 Michael Vorburger
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
package ch.vorburger.mariadb4j.junit;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.junit.rules.ExternalResource;

public class MariaDBRule extends ExternalResource {

    private DB db;
    private DBConfiguration config;
    private final String dbName;
    private final String resource;

    public MariaDBRule(DBConfiguration config, String dbName, String resource) {
        this.config = config;
        this.dbName = dbName;
        this.resource = resource;
    }

    public MariaDBRule(int port) {
        this(DBConfigurationBuilder.newBuilder().setPort(port).build(), null, null);
    }

    @Override
    protected void before() throws Throwable {
        db = DB.newEmbeddedDB(config);
        db.start();
        initDB();
    }

    private void initDB() throws ManagedProcessException {
        if(dbName != null)
            db.createDB(dbName);

        if(resource != null)
            db.source(resource, dbName);
    }

    @Override
    protected void after() {
        try {
            db.stop();
        } catch (ManagedProcessException e) {
            throw new AssertionError();
        }
    }

    public String getConnectionString() {
        return db.getConfiguration().getConnectionURL(dbName);
    }

    public DBConfiguration getConfiguration() {
        return db.getConfiguration();
    }
}
