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

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

/**
 * Reproduces issue #10.
 * 
 * @see <a href="https://github.com/vorburger/MariaDB4j/issues/10">MariaDB4j issue #10</a>
 * 
 * @author Michael Vorburger
 */
public class StartManyTimes {

    public static void main(String[] args) throws ManagedProcessException {
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(0);
        DBConfiguration config = configBuilder.build();

        for (int i = 0; i < 100000; i++) {
            DB db = DB.newEmbeddedDB(config);
            db.start();
            db.stop();
            System.out.println("## " + i);
        }
    }

}
