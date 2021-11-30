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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests the default configuration of a MariaDB4jSpringService.
 * 
 * @author Michael Vorburger
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MariaDB4jSpringServiceTestSpringConfiguration.class)
public class MariaDB4jSpringServiceStandardDefaultsTest {

    @Autowired
    MariaDB4jSpringService s;

    @Test
    public void testStandardDefaults() {
        assertNotEquals(3306, s.getConfiguration().getPort());
        assertTrue(s.getConfiguration().getBaseDir().contains(SystemUtils.JAVA_IO_TMPDIR));
        assertTrue(s.getConfiguration().getDataDir().contains(SystemUtils.JAVA_IO_TMPDIR));
    }

}