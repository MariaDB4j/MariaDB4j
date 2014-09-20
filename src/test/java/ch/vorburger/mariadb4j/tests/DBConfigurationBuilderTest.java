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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import ch.vorburger.mariadb4j.Util;

public class DBConfigurationBuilderTest {

    @Test
    public void defaultDataDirIsTemporaryAndIncludesPortNumber() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        DBConfiguration config = builder.build();
        String defaultDataDir = config.getDataDir();
        int port = config.getPort();
        assertTrue(Util.isTemporaryDirectory(defaultDataDir));
        assertTrue(defaultDataDir.contains(Integer.toString(port)));
    }

    @Test
    public void defaultDataDirIsTemporaryAndIncludesPortNumberEvenIfPortIsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setPort(12345);
        DBConfiguration config = builder.build();
        String defaultDataDir = config.getDataDir();
        assertTrue(Util.isTemporaryDirectory(defaultDataDir));
        assertTrue(defaultDataDir.contains(Integer.toString(12345)));
    }

    @Test
    public void dataDirDoesNotIncludePortNumberEvenItsExplicitlySet() {
        DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();
        builder.setDataDir("db/data");
        DBConfiguration config = builder.build();
        String defaultDataDir = config.getDataDir();
        assertEquals("db/data", defaultDataDir);
        assertFalse(Util.isTemporaryDirectory(defaultDataDir));
    }

}
