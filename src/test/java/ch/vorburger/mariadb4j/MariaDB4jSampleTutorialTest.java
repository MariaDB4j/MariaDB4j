/*
 * Copyright (c) 2012 Michael Vorburger
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */
package ch.vorburger.mariadb4j;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Test;

/**
 * Sample / Tutorial illustrating how to use MariaDB4j.
 * 
 * Running as tests as well.
 * 
 * @author Michael Vorburger
 */
public class MariaDB4jSampleTutorialTest {

	@Test
	// TODO This test, with it's hard-coded basedir, will probably be removed when I do the resource lookup for embedded, which will implicitly test the same
	public void testFixedPathMariaDB4j() throws Exception {
		final String basedir = "src/main/resources/ch/vorburger/mariadb4j/mariadb-5.3.4-rc/win32";
		DB db = new DB(basedir, "target/db1");
		db.start();
	}

	@Test
	public void testEmbeddedMariaDB4j() throws Exception {
		DB db = new EmbeddedDB("target/db1");
		db.start();

		// TODO Should DB have a getJdbcURL() ? UID? PWD?
		Connection conn = DriverManager.getConnection(
				"jdbc:somejdbcvendor:other data needed by some jdbc vendor",
				"myLogin", "myPassword");
		Statement stmt = conn.createStatement();
		stmt.execute("CREATE TABLE hello(world VARCHAR(100))");
		stmt.execute("INSERT INTO hello VALUES 'Hello, world'");
		stmt.close();
		conn.close();
	}

}
