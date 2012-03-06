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

	@Test(expected=IOException.class)
	public void testBadFixedPathMariaDB4j() throws Exception {
		// No DB in bin/ here, should fail:
		final String basedir = "src/main/resources/"; 
		DB db = new DB(basedir, "target/db1");
		db.start(); // will fail with an IOException
	}

	@Test
	public void testEmbeddedMariaDB4jInstallDB() throws Exception {
		DB db = DBFactory.newEmbeddedTemporaryDB();
		db.installDB();
	}
	
	@Test
	public void testEmbeddedMariaDB4j() throws Exception {
		DB db = DBFactory.newEmbeddedTemporaryDB();
		db.start();

		// TODO Should DB have a getJdbcURL() ? UID? PWD?
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "");
		Statement stmt = conn.createStatement();
		
//		stmt.execute("CREATE DATABASE test2;");
//		stmt.execute("GRANT ALL on test2.* to 'testuser'@'localhost' identified by 'testpwd'");
//		stmt.execute("FLUSH PRIVILEGES;");
//		stmt.close();
//		conn.close();
		
//		conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test2", "testuser", "testpwd");
//		Statement stmt = conn.createStatement();
		
		stmt.execute("CREATE TABLE hello(world VARCHAR(100))");
		// TODO? stmt.execute("INSERT INTO hello VALUES 'Hello, world'");
		// TODO? stmt.execute("INSERT INTO hello VALUES \"Hello, world\"");
		
		stmt.executeQuery("SELECT * FROM hello");
		
		stmt.close();
		conn.close();
	}

}
