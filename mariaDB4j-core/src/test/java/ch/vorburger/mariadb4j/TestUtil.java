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

import ch.vorburger.exec.ManagedProcessException;

import java.nio.file.Path;

public class TestUtil {
    public static DBConfigurationBuilder configureTempDB(Path tempDir) {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setBaseDir(tempDir.resolve("MariaDB4j").resolve("base"));
        config.setDataDir(tempDir.resolve("data"));
        config.setTmpDir(tempDir.resolve("tmp"));
        return config;
    }

    public static DBConfigurationBuilder configureTempDBAndResolvePort(Path tempDir) {
        DBConfigurationBuilder config = configureTempDB(tempDir);
        int port = config._getPort();
        config.setDataDir(config.getDataDir().resolve(String.valueOf(port)));
        config.setTmpDir(config.getTmpDir().resolve(String.valueOf(port)));
        return config;
    }

    public static DB buildTempDB(Path tempDir, DBConfigurationBuilder config)
            throws ManagedProcessException {
        return buildTempDB(tempDir, config, DB::newEmbeddedDB);
    }

    public static DB buildTempDB(Path tempDir, DBConfigurationBuilder config, DBFactory dbFactory)
            throws ManagedProcessException {
        int port = config._getPort();
        Path dataDir = config.getDataDir();
        if (dataDir == null || dataDir.equals(tempDir.resolve("data")))
            config.setDataDir(tempDir.resolve("data").resolve(String.valueOf(port)));
        Path tmpDir = config.getTmpDir();
        if (tmpDir == null || tmpDir.equals(tempDir.resolve("tmp")))
            config.setTmpDir(tempDir.resolve("tmp").resolve(String.valueOf(port)));
        return dbFactory.create(config.build());
    }
}
