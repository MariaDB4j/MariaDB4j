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
package ch.vorburger.mariadb4j.tests;

import static ch.vorburger.mariadb4j.TestUtil.configureTempDB;

import static org.junit.jupiter.api.Assertions.*;

import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfiguration.Executable;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.Util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.FileSystems;
import java.nio.file.Path;

class DBConfigurationBuilderTest {

    @Test
    void defaultDataDirIsTemporaryAndIncludesPortNumber() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        Path defaultDataDir = config.dataDir();
        assertTrue(Util.isTemporaryDirectory(defaultDataDir));
        assertEquals(String.valueOf(config.port()), defaultDataDir.getFileName().toString());
    }

    @Test
    void defaultDataDirIsTemporaryAndIncludesPortNumberEvenIfPortIsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setPort(12345);
        DBConfiguration config = builder.build();
        Path defaultDataDir = config.dataDir();
        assertTrue(Util.isTemporaryDirectory(defaultDataDir));
        assertEquals(String.valueOf(config.port()), defaultDataDir.getFileName().toString());
    }

    @Test
    void dataDirDoesNotIncludePortNumberEvenItsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        Path dataDir = Path.of("db").resolve("data");
        builder.setDataDir(dataDir);
        DBConfiguration config = builder.build();
        Path defaultDataDir = config.dataDir();
        assertEquals(dataDir, defaultDataDir);
        assertFalse(Util.isTemporaryDirectory(defaultDataDir));
    }

    @Test
    void resetDataDirToDefaultTemporary() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        Path dataDir = Path.of("db").resolve("data");
        builder.setDataDir(dataDir);
        assertEquals(dataDir, builder.getDataDir());
        builder.setDataDir(null);
        DBConfiguration config = builder.build();
        Path defaultDataDir = config.dataDir();
        assertTrue(Util.isTemporaryDirectory(defaultDataDir));
        assertEquals(String.valueOf(config.port()), defaultDataDir.getFileName().toString());
    }

    @Test
    void defaultTmpDirIsTemporaryAndIncludesPortNumber() {
        DBConfiguration config = DBConfigurationBuilder.newBuilder().build();
        Path defaultTmpDir = config.tmpDir();
        assertTrue(Util.isTemporaryDirectory(defaultTmpDir));
        assertEquals(String.valueOf(config.port()), defaultTmpDir.getFileName().toString());
    }

    @Test
    void defaultTmpDirIsTemporaryAndIncludesPortNumberEvenIfPortIsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setPort(12345);
        DBConfiguration config = builder.build();
        Path defaultTmpDir = config.tmpDir();
        assertTrue(Util.isTemporaryDirectory(defaultTmpDir));
        assertEquals(String.valueOf(12345), defaultTmpDir.getFileName().toString());
    }

    @Test
    void tmpDirDoesNotIncludePortNumberEvenItsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        Path tmpDir = Path.of("db").resolve("tmp");
        builder.setTmpDir(tmpDir);
        DBConfiguration config = builder.build();
        Path defaultTmpDir = config.tmpDir();
        assertFalse(Util.isTemporaryDirectory(defaultTmpDir));
        assertEquals(tmpDir, defaultTmpDir);
    }

    @Test
    void resetTmpDirToDefaultTemporary() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        Path tempDir = Path.of("db").resolve("tmp");
        builder.setTmpDir(tempDir);
        assertEquals(tempDir, builder.getTmpDir());
        builder.setTmpDir(null);
        assertTrue(Util.isTemporaryDirectory(builder.getTmpDir()));
        DBConfiguration config = builder.build();
        Path defaultTmpDir = config.tmpDir();
        assertTrue(Util.isTemporaryDirectory(defaultTmpDir));
    }

    @Test
    void defaultLibDirIsRelativeToBaseDir() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        Path defaultBaseDir = config.baseDir();
        assertTrue(Util.isTemporaryDirectory(defaultBaseDir));
        Path defaultLibDir = config.libDir();
        assertEquals(defaultLibDir, defaultBaseDir.resolve("libs"));
    }

    @Test
    void defaultLibDirIsRelativeToUpdatedBaseDir(@TempDir Path tempDir) {
        DBConfigurationBuilder builder = configureTempDB(tempDir);
        builder.setBaseDir(tempDir.resolve("MariaDB4j"));
        DBConfiguration config = builder.build();

        Path baseDir = config.baseDir();
        Path defaultLibDir = config.libDir();
        assertEquals(defaultLibDir, baseDir.resolve("libs"));
    }

    @Test
    void modifiedLibDir(@TempDir Path tempDir) {
        DBConfigurationBuilder builder = configureTempDB(tempDir);
        Path libDir = tempDir.resolve("libsdir");
        builder.setLibDir(libDir);
        builder.setBaseDir(tempDir.resolve("mariadb"));
        DBConfiguration config = builder.build();

        Path updatedLibDir = config.libDir();
        assertEquals(updatedLibDir, libDir);
    }

    @Test
    void deletesTemporaryDirectoriesAsDefault() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        assertTrue(config.isDeletingTemporaryBaseAndDataDirsOnShutdown());
    }

    @Test
    void keepsTemporaryDirectories() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setDeletingTemporaryBaseAndDataDirsOnShutdown(false);
        DBConfiguration config = builder.build();
        assertFalse(config.isDeletingTemporaryBaseAndDataDirsOnShutdown());
    }

    @Test
    void defaultCharacterSet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        String character = "utf8mb4";
        builder.setDefaultCharacterSet(character);
        DBConfiguration config = builder.build();
        String defaultCharacterSet = config.defaultCharacterSet();
        assertEquals(character, defaultCharacterSet);
    }

    @Test
    void defaultCharacterSetIsEmpty() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        String defaultCharacterSet = config.defaultCharacterSet();
        assertNull(defaultCharacterSet);
    }

    @Test
    void defaultExecutables() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        var pathSeparator = FileSystems.getDefault().getSeparator();
        var expectedMariaDB4jString = "MariaDB4j/base/".replace("/", pathSeparator);
        var expectedMariaDBString = "bin/mariadbd".replace("/", pathSeparator);
        var expectedMySqlString = "bin/mysqld".replace("/", pathSeparator);
        String executable = config.getExecutable(Executable.SERVER).toString();
        assertTrue(
                executable.contains(expectedMariaDB4jString)
                        || executable.contains(expectedMariaDBString)
                        || executable.contains(expectedMySqlString));
    }

    @Test
    void customExecutables() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setExecutable(Executable.SERVER, "/usr/sbin/mariadbd");
        DBConfiguration config = builder.build();
        assertEquals(
                Path.of("/usr").resolve("sbin").resolve("mariadbd"),
                config.getExecutable(Executable.SERVER));
    }
}
