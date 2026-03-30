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

import static com.google.common.truth.Truth.assertThat;

import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfiguration.Executable;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.Util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DBConfigurationBuilderTest {

    @Test
    public void defaultDataDirIsTemporaryAndIncludesPortNumber() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        File defaultDataDir = config.getDataDir();
        assertThat(Util.isTemporaryDirectory(defaultDataDir)).isTrue();
        int port = config.getPort();
        assertThat(defaultDataDir.toString()).contains(Integer.toString(port));
    }

    @Test
    public void defaultDataDirIsTemporaryAndIncludesPortNumberEvenIfPortIsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setPort(12345);
        DBConfiguration config = builder.build();
        File defaultDataDir = config.getDataDir();
        assertThat(Util.isTemporaryDirectory(defaultDataDir)).isTrue();
        assertThat(defaultDataDir.toString()).contains(Integer.toString(12345));
    }

    @Test
    public void dataDirDoesNotIncludePortNumberEvenItsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setDataDir(new File("db/data"));
        DBConfiguration config = builder.build();
        File defaultDataDir = config.getDataDir();
        assertThat(defaultDataDir).isEqualTo(new File("db/data"));
        assertThat(Util.isTemporaryDirectory(defaultDataDir)).isFalse();
    }

    @Test
    public void resetDataDirToDefaultTemporary() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setDataDir(new File("db/data"));
        assertThat(builder.getDataDir()).isEqualTo(new File("db/data"));
        builder.setDataDir(null);
        DBConfiguration config = builder.build();
        File defaultDataDir = config.getDataDir();
        assertThat(Util.isTemporaryDirectory(defaultDataDir)).isTrue();
        int port = config.getPort();
        assertThat(defaultDataDir.toString()).contains(Integer.toString(port));
    }

    @Test
    public void defaultTmpDirIsTemporaryAndIncludesPortNumber() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        File defaultTmpDir = config.getTmpDir();
        assertThat(Util.isTemporaryDirectory(defaultTmpDir)).isTrue();
        int port = config.getPort();
        assertThat(defaultTmpDir.toString()).contains(Integer.toString(port));
    }

    @Test
    public void defaultTmpDirIsTemporaryAndIncludesPortNumberEvenIfPortIsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setPort(12345);
        DBConfiguration config = builder.build();
        File defaultTmpDir = config.getTmpDir();
        assertThat(Util.isTemporaryDirectory(defaultTmpDir)).isTrue();
        assertThat(defaultTmpDir.toString()).contains(Integer.toString(12345));
    }

    @Test
    public void tmpDirDoesNotIncludePortNumberEvenItsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setTmpDir("db/tmp");
        DBConfiguration config = builder.build();
        File defaultTmpDir = config.getTmpDir();
        assertThat(Util.isTemporaryDirectory(defaultTmpDir)).isFalse();
        var defaultTmpDirString = defaultTmpDir.toString();
        assertThat(defaultTmpDirString).contains("db" + File.separator + "tmp");
    }

    @Test
    public void resetTmpDirToDefaultTemporary() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setTmpDir("db/tmp");
        assertThat(builder.getTmpDir()).isEqualTo(new File("db/tmp"));
        builder.setTmpDir(null);
        assertThat(Util.isTemporaryDirectory(builder.getTmpDir())).isTrue();
        builder.setTmpDir(null);
        DBConfiguration config = builder.build();
        File defaultTmpDir = config.getTmpDir();
        assertThat(Util.isTemporaryDirectory(defaultTmpDir)).isTrue();
    }

    @Test
    public void defaultLibDirIsRelativeToBaseDir() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        File defaultBaseDir = config.getBaseDir();
        assertThat(Util.isTemporaryDirectory(defaultBaseDir)).isTrue();
        File defaultLibDir = config.getLibDir();
        assertThat(defaultLibDir).isEqualTo(new File(defaultBaseDir, "libs"));
    }

    @Test
    public void defaultLibDirIsRelativeToUpdatedBaseDir() throws IOException {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        Path baseDirPath = Files.createTempDirectory("MariaDB4j");
        builder.setBaseDir(baseDirPath.toFile());
        DBConfiguration config = builder.build();

        File baseDir = config.getBaseDir();
        File defaultLibDir = config.getLibDir();
        assertThat(defaultLibDir).isEqualTo(new File(baseDir, "libs"));
    }

    @Test
    public void modifiedLibDir() throws IOException {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        Path libDir = Files.createTempDirectory("libsdir");
        builder.setLibDir(libDir.toFile());
        Path baseDir = Files.createTempDirectory("mariadb");
        builder.setBaseDir(baseDir.toFile());
        DBConfiguration config = builder.build();

        File updatedLibDir = config.getLibDir();
        assertThat(updatedLibDir).isEqualTo(libDir.toFile());
    }

    @Test
    public void deletesTemporaryDirectoriesAsDefault() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        assertThat(config.isDeletingTemporaryBaseAndDataDirsOnShutdown()).isTrue();
    }

    @Test
    public void keepsTemporaryDirectories() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setDeletingTemporaryBaseAndDataDirsOnShutdown(false);
        DBConfiguration config = builder.build();
        assertThat(config.isDeletingTemporaryBaseAndDataDirsOnShutdown()).isFalse();
    }

    @Test
    public void defaultCharacterSet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        String character = "utf8mb4";
        builder.setDefaultCharacterSet(character);
        DBConfiguration config = builder.build();
        String defaultCharacterSet = config.getDefaultCharacterSet();
        assertThat(defaultCharacterSet).isEqualTo(character);
    }

    @Test
    public void defaultCharacterSetIsEmpty() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        String defaultCharacterSet = config.getDefaultCharacterSet();
        assertThat(defaultCharacterSet).isNull();
    }

    @Test
    public void defaultExecutables() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        var pathSeparator = System.getProperty("file.separator");
        var expectedMariaDB4jString = "MariaDB4j/base/".replace("/", pathSeparator);
        var expectedMariaDBString = "bin/mariadbd".replace("/", pathSeparator);
        var expectedMySqlString = "bin/mysqld".replace("/", pathSeparator);
        String executable = config.getExecutable(Executable.Server).toString();
        assertThat(
                        executable.contains(expectedMariaDB4jString)
                                || executable.contains(expectedMariaDBString)
                                || executable.contains(expectedMySqlString))
                .isTrue();
    }

    @Test
    public void customExecutables() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setExecutable(Executable.Server, "/usr/sbin/mariadbd");
        DBConfiguration config = builder.build();
        assertThat(config.getExecutable(Executable.Server))
                .isEqualTo(new File("/usr/sbin/mariadbd"));
    }
}
