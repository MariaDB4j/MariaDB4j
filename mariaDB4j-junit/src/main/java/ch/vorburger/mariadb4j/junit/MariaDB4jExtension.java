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
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.nio.file.Path;

public class MariaDB4jExtension
        implements Extension,
                BeforeEachCallback,
                AfterEachCallback,
                BeforeAllCallback,
                AfterAllCallback {
    private static final ExtensionContext.Namespace NS =
            ExtensionContext.Namespace.create(MariaDB4jExtension.class, "temp");

    private static TempRoot getOrCreateTempRoot(ExtensionContext ctx) {
        var store = ctx.getStore(NS);
        return store.getOrComputeIfAbsent(
                "root",
                k -> {
                    try {
                        return new TempRoot();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                TempRoot.class);
    }

    private DB db;
    private final String dbName;
    private final String resource;
    private final DBConfigurationBuilder dbConfigurationBuilder;
    private DBConfiguration dbConfiguration;
    private final Lifecycle lifecycle;

    public MariaDB4jExtension(
            DBConfigurationBuilder dbConfigurationBuilder,
            String dbName,
            String resource,
            Lifecycle lifecycle) {
        this.dbConfigurationBuilder = dbConfigurationBuilder;
        this.dbName = dbName;
        this.resource = resource;
        this.lifecycle = lifecycle;
    }

    public MariaDB4jExtension(
            DBConfigurationBuilder dbConfigurationBuilder, String dbName, String resource) {
        this(dbConfigurationBuilder, dbName, resource, Lifecycle.PER_METHOD);
    }

    public MariaDB4jExtension(int port, Lifecycle lifecycle) {
        this(DBConfigurationBuilder.newBuilder().setPort(port), "", null, lifecycle);
    }

    public MariaDB4jExtension(int port) {
        this(port, Lifecycle.PER_METHOD);
    }

    @Override
    public void beforeAll(ExtensionContext ctx) throws Exception {
        if (lifecycle == Lifecycle.PER_CLASS) startAndInit(ctx);
    }

    @Override
    public void afterAll(ExtensionContext ctx) {
        if (lifecycle == Lifecycle.PER_CLASS) stopQuietly();
    }

    @Override
    public void beforeEach(ExtensionContext ctx) throws Exception {
        if (lifecycle == Lifecycle.PER_METHOD) startAndInit(ctx);
    }

    @Override
    public void afterEach(ExtensionContext ctx) {
        if (lifecycle == Lifecycle.PER_METHOD) stopQuietly();
    }

    public String getURL() {
        return ((db != null) ? db.getConfiguration() : dbConfiguration).getURL(dbName);
    }

    public DBConfiguration getDBConfiguration() {
        return db != null ? db.getConfiguration() : dbConfiguration;
    }

    private void startAndInit(ExtensionContext ctx) throws ManagedProcessException {
        Path tempDir = getOrCreateTempRoot(ctx).dir;
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder(dbConfigurationBuilder);

        Path javaIoTmpDir = Path.of(SystemUtils.JAVA_IO_TMPDIR);

        Path baseDir = config.getBaseDir();
        if (baseDir == null || baseDir.equals(javaIoTmpDir.resolve("MariaDB4j").resolve("base")))
            config.setBaseDir(tempDir.resolve("MariaDB4j").resolve("base"));

        int port = config.getPort();
        if (port == 0) {
            port = DBConfigurationBuilder.detectFreePort();
            config.setPort(port);
        }

        Path dataDir = config.getDataDir();
        if (dataDir == null || dataDir.equals(javaIoTmpDir.resolve("data")))
            config.setDataDir(tempDir.resolve("data").resolve(String.valueOf(port)));
        Path tmpDir = config.getTmpDir();
        if (tmpDir == null || tmpDir.equals(javaIoTmpDir.resolve("tmp")))
            config.setTmpDir(tempDir.resolve("tmp").resolve(String.valueOf(port)));

        dbConfiguration = config.build();

        db = DB.newEmbeddedDB(dbConfiguration);
        db.start();
        initDB();
    }

    private void initDB() throws ManagedProcessException {
        if (StringUtils.isEmpty(dbName)) return;
        db.createDB(dbName);
        if (!StringUtils.isEmpty(resource)) db.source(resource, dbName);
    }

    private void stopQuietly() {
        try {
            if (db != null) db.stop();
        } catch (ManagedProcessException e) {
            throw new AssertionError("db.stop() failed", e);
        } finally {
            db = null;
        }
    }
}
