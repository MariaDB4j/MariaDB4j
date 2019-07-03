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

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

//separate with MariaDB4jSpringConfiguration for test of it
@Configuration
@ConfigurationProperties("spring.datasource")
@ConditionalOnProperty(
    prefix = "mariaDB4j",
    name = {"enabled"},
    matchIfMissing = true
)
public class DataSourceAutoConfiguration {
    private MariaDB4jSpringService mariaDB4j;

    public DataSourceAutoConfiguration(MariaDB4jSpringService mariaDB4j){
        this.mariaDB4j = mariaDB4j;
    }

    @Bean
    @DependsOn("mariaDB4j")
    public DataSource dataSource(DataSourceProperties dataSourceProperties) throws ManagedProcessException {
        MariaDBUrl mariaDBUrl = new MariaDBUrl(dataSourceProperties.getUrl());
        mariaDB4j.getConfiguration().addArg("--user=root");
        mariaDB4j.getDB().createDB(mariaDBUrl.getDb());
        return DataSourceBuilder.create()
                .driverClassName(dataSourceProperties.getDriverClassName())
                .url(mariaDBUrl.urlWithPort(mariaDB4j.getConfiguration().getPort()))
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword())
                .build();
    }
}
