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

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Tests overriding the default configuration of a MariaDB4jSpringService set in a {@link
 * Configuration} via Spring Value properties.
 *
 * @author Michael Vorburger
 */
@SpringJUnitConfig(classes = MariaDB4jSpringServiceTestSpringConfiguration.class)
@TestPropertySource(properties = {MariaDB4jSpringService.PORT + "=5678"})
class MariaDB4jSpringServiceNewDefaultsOverriddenBySpringValueTest {

    @Autowired MariaDB4jSpringService s;

    @BeforeEach
    void setUp() {
        if (!s.isRunning()) {
            s.start(); // Only start if not already running
        }
    }

    @Test
    void testNewDefaults() {
        assertEquals(5678, s.getConfiguration().port());
    }

    @AfterEach
    void tearDown() {
        if (s.isRunning()) {
            s.stop();
        }
    }
}
