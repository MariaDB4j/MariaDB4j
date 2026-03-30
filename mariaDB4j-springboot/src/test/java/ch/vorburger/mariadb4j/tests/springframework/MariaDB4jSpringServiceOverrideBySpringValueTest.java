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

import static com.google.common.truth.Truth.assertThat;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;

/**
 * Tests setting the configuration of a MariaDB4jSpringService via Spring Value properties.
 *
 * @author Michael Vorburger
 */
@ContextConfiguration(classes = MariaDB4jSpringServiceTestSpringConfiguration.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource(
        properties = {
            MariaDB4jSpringService.PORT + "=5679",
            MariaDB4jSpringService.BASE_DIR
                    + "=target/MariaDB4jSpringServiceOverrideBySpringValueTest/baseDir",
            MariaDB4jSpringService.LIB_DIR
                    + "=target/MariaDB4jSpringServiceOverrideBySpringValueTest/baseDir/libs",
            MariaDB4jSpringService.DATA_DIR
                    + "=target/MariaDB4jSpringServiceOverrideBySpringValueTest/dataDir",
            MariaDB4jSpringService.TMP_DIR
                    + "=target/MariaDB4jSpringServiceOverrideBySpringValueTest/tmpDir"
        })
public class MariaDB4jSpringServiceOverrideBySpringValueTest {

    @Autowired MariaDB4jSpringService s;

    @BeforeEach
    public void setUp() {
        if (!s.isRunning()) {
            s.start(); // Only start if not already running
        }
    }

    @Test
    public void testOverrideBySpringValue() {
        assertThat(s.getConfiguration().getPort()).isEqualTo(5679);
        assertThat(s.getConfiguration().getBaseDir())
                .isEqualTo(
                        new File("target/MariaDB4jSpringServiceOverrideBySpringValueTest/baseDir"));
        assertThat(s.getConfiguration().getLibDir())
                .isEqualTo(
                        new File(
                                "target/MariaDB4jSpringServiceOverrideBySpringValueTest/baseDir/libs"));
        assertThat(s.getConfiguration().getDataDir())
                .isEqualTo(
                        new File("target/MariaDB4jSpringServiceOverrideBySpringValueTest/dataDir"));
        assertThat(s.getConfiguration().getTmpDir())
                .isEqualTo(
                        new File("target/MariaDB4jSpringServiceOverrideBySpringValueTest/tmpDir"));
    }

    @AfterEach
    public void tearDown() {
        if (s.isRunning()) {
            s.stop();
        }
    }
}
