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
package ch.vorburger.mariadb4j.tests.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.vorburger.mariadb4j.junit.MariaDB4jExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MariaDB4jUnitExtensionOverrideDefaultPortTest {

    @RegisterExtension
    private static final MariaDB4jExtension dbRule = new MariaDB4jExtension(3307);

    @Test
    void validatePort() {
        assertEquals(3307, dbRule.getDBConfiguration().port());
    }
}
