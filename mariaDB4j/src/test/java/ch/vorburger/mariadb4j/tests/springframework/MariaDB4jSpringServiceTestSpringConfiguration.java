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

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import java.util.Properties;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MariaDB4jSpringServiceTestSpringConfiguration {

    @Bean public MariaDB4jSpringService mariaDB4jSpringService() {
        MariaDB4jSpringService mariaDB4jSpringService = new MariaDB4jSpringService();
        configureMariaDB4jSpringService(mariaDB4jSpringService);
        return mariaDB4jSpringService;
    }

    protected void configureMariaDB4jSpringService(
            @SuppressWarnings("unused") MariaDB4jSpringService mariaDB4jSpringService) {}

    @Bean PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        Properties properties = new Properties();
        configureProperties(properties);
        ppc.setProperties(properties);
        return ppc;
    }

    protected void configureProperties(@SuppressWarnings("unused") Properties properties) {}

}