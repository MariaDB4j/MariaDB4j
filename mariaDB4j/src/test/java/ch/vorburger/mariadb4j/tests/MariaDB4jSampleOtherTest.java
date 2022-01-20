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

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

/**
 * Tests more functionality of MariaDB4j.
 */
public class MariaDB4jSampleOtherTest {

    /**
     * This test ensure that there is no conflict between sockets if two MariaDB4j run on the same port.
     */
    @Test public void startTwoMariaDB4j() throws Exception {
        DB db1 = startNewDB();
        DB db2 = startNewDB();
        db1.stop();
        db2.stop();
        // see below in customBaseDir() why we need this here
        FileUtils.deleteQuietly(new File(db1.getConfiguration().getBaseDir()));

    }

    protected DB startNewDB() throws ManagedProcessException {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(0);
        DB db = DB.newEmbeddedDB(config.build());
        db.start();
        return db;
    }

    /**
     * Reproduces issue #30 re. Exception if there are spaces in the data directory path #30.
     * 
     * @see <a href="https://github.com/vorburger/MariaDB4j/issues/30">MariaDB4j issue #30</a>
     */
    @Test public void dataDirWithSpace() throws Exception {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        // Note that this dataDir intentionally contains a space before its last word
        config.setDataDir(SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/" + MariaDB4jSampleOtherTest.class.getName() + " dataDirWithSpace");
        DB db = DB.newEmbeddedDB(config.build());
        db.start();
        db.stop();
        // see below in customBaseDir() why we need this here
        FileUtils.deleteQuietly(new File(db.getConfiguration().getBaseDir()));
    }

    /**
     * Reproduces issue #39 re. libDir having to be correctly sate "late" and not on DBConfigurationBuilder constructor in case of a
     * non-default baseDir.
     * 
     * <p>This test passes even without the bug fix if another test "left over" a base dir with a libs/ directory in JAVA_IO_TMPDIR. The two
     * tests above does clean up after themselves directly. The default behaviour is for the class DB to do this only in a Shutdown hook,
     * which is too late for what this test wants to ensure.
     * 
     * @see <a href="https://github.com/vorburger/MariaDB4j/issues/39">MariaDB4j issue #39</a>
     */
    @Test public void customBaseDir() throws Exception {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setBaseDir(SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/" + MariaDB4jSampleOtherTest.class.getName() + "customBaseDir");
        DB db = DB.newEmbeddedDB(config.build());
        db.start();
        db.stop();
    }

    @Test public void customCharacterSet() throws Exception {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setBaseDir(SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/" + MariaDB4jSampleOtherTest.class.getName() + "customBaseDir");
        config.setDefaultCharacterSet("utf8mb4");
        DB db = DB.newEmbeddedDB(config.build());
        db.start();
        db.createDB("junittest");
        db.source("ch/vorburger/mariadb4j/characterTest.sql");
        db.stop();
    }

}
