/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2018 Yuexiang Gao
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
package ch.vorburger.mariadb4j.springboot.autoconfigure;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class MariaDB4JSpringConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(MariaDB4jSpringService.class));

    @Test
    public void shouldAutoConfigureEmbeddedMariaDB() {
        contextRunner
                .withUserConfiguration(MariaDB4jSpringService.class)
                .run(
                        context -> {
                            Assertions.assertThat(
                                            context.getBeansOfType(MariaDB4jSpringService.class))
                                    .isNotEmpty();
                        });
    }
}
