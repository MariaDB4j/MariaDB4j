/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2023 - 2023 Michael Vorburger
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
package ch.vorburger.mariadb4j.container;

import org.junit.Test;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MariaDB4jContainerTest {
    @Test
    public void testContainer() {
        // TODO As of 2023-09-16 this fails on Fedora 38, due to https://github.com/docker-java/docker-java/issues/2201; waiting for Podman >= 4.6.3...
        var mariaDB = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.5.5"));
        mariaDB.start();
    }
}
