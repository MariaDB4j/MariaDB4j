/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2014 Michael Vorburger
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
package ch.vorburger.mariadb4j.tests;

import ch.vorburger.mariadb4j.MariaDB4jService;
import org.junit.Test;

public class MariaDB4jServiceTest {

    @Test
    public void testStartStop() throws Exception {
        MariaDB4jService service = new MariaDB4jService();
        service.getConfiguration().setPort(0);
        service.start();
        service.stop();
    }

}
