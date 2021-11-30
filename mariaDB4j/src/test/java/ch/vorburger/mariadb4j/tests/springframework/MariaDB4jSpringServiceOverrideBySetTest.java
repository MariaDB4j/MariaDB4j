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
package ch.vorburger.mariadb4j.tests.springframework;

import static org.junit.Assert.assertEquals;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests programmatically setting the configuration of a MariaDB4jSpringService via setters in a {@link Configuration}.
 * 
 * @author Michael Vorburger
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class MariaDB4jSpringServiceOverrideBySetTest {

    @Configuration
    public static class TestConfiguration extends MariaDB4jSpringServiceTestSpringConfiguration {

        @Override protected void configureMariaDB4jSpringService(MariaDB4jSpringService s) {
            s.setDefaultPort(5677);
            s.setDefaultBaseDir("target/MariaDB4jSpringServiceOverrideBySetTest/baseDir");
            // do NOT s.setDefaultLibDir() - it will (should) default to "baseDir/libs"; see issue #39
            s.setDefaultDataDir("target/MariaDB4jSpringServiceOverrideBySetTest/dataDir");
        }
    }

    @Autowired MariaDB4jSpringService s;

    @Test public void testOverrideBySet() {
        assertEquals(5677, s.getConfiguration().getPort());
        assertEquals("target/MariaDB4jSpringServiceOverrideBySetTest/baseDir", s.getConfiguration().getBaseDir());
        assertEquals("target/MariaDB4jSpringServiceOverrideBySetTest/baseDir/libs", s.getConfiguration().getLibDir());
        assertEquals("target/MariaDB4jSpringServiceOverrideBySetTest/dataDir", s.getConfiguration().getDataDir());
    }

}