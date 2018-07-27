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
import org.apache.commons.lang3.StringUtils;
import org.junit.rules.ExternalResource;

public class MariaDB4jRule extends ExternalResource {

    private DB db;
    private final String dbName;
    private final String resource;
    private final DBConfiguration dbConfiguration;

    public MariaDB4jRule(DBConfiguration dbConfiguration, String dbName, String resource) {
        this.dbConfiguration = dbConfiguration;
        this.dbName = dbName;
        this.resource = resource;
    }

    public MariaDB4jRule(int port) {
        this(DBConfigurationBuilder.newBuilder().setPort(port).build(), "", null);
    }

    @Override
    protected void before() throws Throwable {
        db = DB.newEmbeddedDB(dbConfiguration);
        db.start();
        initDB();
    }

    protected void initDB() throws ManagedProcessException {
        if (!StringUtils.isEmpty(dbName)) {
            db.createDB(dbName);
            if (!StringUtils.isEmpty(resource))
                db.source(resource, dbName);
        }
    }

    @Override
    protected void after() {
        try {
            db.stop();
        } catch (ManagedProcessException e) {
            throw new AssertionError("db.stop() failed", e);
        }
    }

    public String getURL() {
        return dbConfiguration.getURL(dbName);
    }

    public DBConfiguration getDBConfiguration() {
        return db.getConfiguration();
    }
}
