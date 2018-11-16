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
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MariaDB4JSpringConfigurationTest {

    @Configuration
    static class Config{
        @Bean
        public DataSourceProperties dataSourceProperties(){
            DataSourceProperties dsp = Mockito.mock(DataSourceProperties.class);
            when(dsp.getUrl()).thenReturn("");
            return dsp;
        }
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MariaDB4jSpringConfiguration.class));

    @Test
    public void shouldAutoConfigureEmbeddedMariaDB() {
        this.contextRunner.withUserConfiguration(MariaDB4jSpringConfiguration.class, Config.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(MariaDB4jSpringService.class);
                    assertThat(context.getBean(MariaDB4jSpringService.class))
                            .isSameAs(context.getBean(MariaDB4jSpringConfiguration.class)
                                    .mariaDB4j(context.getBean(DataSourceProperties.class)));
                });
    }

    @Test
    public void shouldParseCorrectlyMariaDbUrl(){
        MariaDBUrl mariaDBUrl = new MariaDBUrl("jdbc:mariadb://host:3306/db");
        assertThat(mariaDBUrl.getHost()).isEqualTo("host");
        assertThat(mariaDBUrl.getPort()).isEqualTo(3306);
        assertThat(mariaDBUrl.getDb()).isEqualTo("db");
    }

    @Test
    public void shouldParseCorrectlyMariaDbUrlWithoutPort(){
        MariaDBUrl mariaDBUrl = new MariaDBUrl("jdbc:mariadb://host/db");
        assertThat(mariaDBUrl.getHost()).isEqualTo("host");
        assertThat(mariaDBUrl.getPort()).isEqualTo(-1);
        assertThat(mariaDBUrl.getDb()).isEqualTo("db");
    }

}