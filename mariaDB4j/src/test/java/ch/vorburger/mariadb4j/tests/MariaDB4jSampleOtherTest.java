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

import org.junit.Test;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

/**
 * Tests more functionality of MariaDB4j.
 */
public class MariaDB4jSampleOtherTest {

	/**
	 * This test ensure that there is no conflict between sockets if two MariaDB4j run on the same port.
	 */
	@Test
	public void startTwoMariaDB4j() throws Exception {
		DB db1 = startNewDB();
        DB db2 = startNewDB();
		db1.stop();
		db2.stop();
	}

	protected DB startNewDB() throws ManagedProcessException {
		DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
		config.setPort(0);
		DB db = DB.newEmbeddedDB(config.build());
		db.start();
		return db;
	}
	
}
