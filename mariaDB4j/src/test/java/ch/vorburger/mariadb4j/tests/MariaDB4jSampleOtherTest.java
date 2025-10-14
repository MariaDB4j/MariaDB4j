/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2014 Michael Vorburger
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

import static ch.vorburger.mariadb4j.TestUtil.buildTempDB;
import static ch.vorburger.mariadb4j.TestUtil.configureTempDB;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

/** Tests more functionality of MariaDB4j. */
class MariaDB4jSampleOtherTest {

    private static DB startNewDB(Path tempDir) throws ManagedProcessException {
        DB db = buildTempDB(tempDir, configureTempDB(tempDir));
        db.start();
        return db;
    }

    /**
     * This test ensure that there is no conflict between sockets if two MariaDB4j run on the same
     * port.
     */
    @Test
    void startTwoMariaDB4j(@TempDir Path tempDir) throws ManagedProcessException {
        DB db1 = null;
        DB db2 = null;
        try {
            db1 = startNewDB(tempDir);
            db2 = startNewDB(tempDir);
        } finally {
            if (db1 != null) db1.stop();
            if (db2 != null) db2.stop();
        }
    }

    /**
     * Reproduces issue #30 re. Exception if there are spaces in the data directory path #30.
     *
     * @see <a href="https://github.com/MariaDB4j/MariaDB4j/issues/30">MariaDB4j issue #30</a>
     */
    @Test
    void dataDirWithSpace(@TempDir Path tempDir) throws ManagedProcessException {
        DBConfigurationBuilder config = configureTempDB(tempDir);
        // Note that this dataDir intentionally contains a space before its last word
        config.setDataDir(
                tempDir.resolve("MariaDB4j")
                        .resolve(MariaDB4jSampleOtherTest.class.getName() + " dataDirWithSpace"));
        DB db = null;
        try {
            db = buildTempDB(tempDir, config);
            db.start();
        } finally {
            if (db != null) db.stop();
        }
    }

    /**
     * Reproduces issue #39 re. libDir having to be correctly sate "late" and not on
     * DBConfigurationBuilder constructor in case of a non-default baseDir.
     *
     * @see <a href="https://github.com/MariaDB4j/MariaDB4j/issues/39">MariaDB4j issue #39</a>
     */
    @Test
    void customBaseDir(@TempDir Path tempDir) throws ManagedProcessException {
        DBConfigurationBuilder config = configureTempDB(tempDir);
        config.setBaseDir(
                tempDir.resolve("MariaDB4j")
                        .resolve(MariaDB4jSampleOtherTest.class.getName() + "customBaseDir"));
        DB db = null;
        try {
            db = buildTempDB(tempDir, config);
            db.start();
        } finally {
            if (db != null) db.stop();
        }
    }

    @Test
    void customCharacterSet(@TempDir Path tempDir) throws ManagedProcessException {
        DBConfigurationBuilder config = configureTempDB(tempDir);
        config.setBaseDir(
                tempDir.resolve("MariaDB4j")
                        .resolve(MariaDB4jSampleOtherTest.class.getName() + "customBaseDir"));
        config.setDefaultCharacterSet("utf8mb4");
        DB db = null;
        try {
            db = buildTempDB(tempDir, config);
            db.start();
            db.createDB("junittest");
            db.source("ch/vorburger/mariadb4j/characterTest.sql");
        } finally {
            if (db != null) db.stop();
        }
    }
}
