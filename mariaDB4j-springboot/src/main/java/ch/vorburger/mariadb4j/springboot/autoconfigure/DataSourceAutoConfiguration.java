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
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//separate with MariaDB4jSpringConfiguration for test of it
@Configuration
public class DataSourceAutoConfiguration {

    private static final String URL_PATTERN = "(jdbc:mariadb://\\S{1,255}:)(\\d+)(/\\w+)";

    @Value("${mariaDB4j.port}")
    private Integer configuredMariaDBPort;

    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties, MariaDB4jSpringService mariaDB4j) {
        String actualUrl = generateActualUrl(dataSourceProperties, mariaDB4j.getPort());

        return DataSourceBuilder.create()
                .driverClassName(dataSourceProperties.getDriverClassName())
                .url(actualUrl)
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword())
                .build();
    }

    private String generateActualUrl(DataSourceProperties dataSourceProperties, int actualPort) {
        String actualUrl = dataSourceProperties.getUrl();
        if (configuredMariaDBPort != null && configuredMariaDBPort == 0) {
            Matcher matcher = Pattern.compile(URL_PATTERN).matcher(actualUrl);
            if (!matcher.find()) {
                // throw exception to determine configure error
                throw new BeanCreationException("dataSource", "Cannot create bean dataSource cause mariaDB4j.port is 0 and we cannot match database url.");
            }
            actualUrl = matcher.group(1) + actualPort + matcher.group(3);
        }
        return actualUrl;
    }
}
