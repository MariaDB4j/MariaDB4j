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

import ch.vorburger.exec.ManagedProcessException;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Sample / Tutorial illustrating how to use MariaDB4j.
 * 
 * Running as tests as well.
 * 
 * @author Michael Vorburger
 */
public class MariaDB4jSampleTutorialTest {

	@BeforeClass
	public static void beforeClass() throws IOException {
		FileUtils.deleteDirectory(new File("/tmp/MariaDB4j"));
	}
	
	@Test(expected=ManagedProcessException.class)
	public void testBadFixedPathMariaDB4j() throws Exception {
		Configuration config = new Configuration();
		config.setBaseDir("src/main/resources/");
		config.setDataDir("target/db1");
		DB db = DB.newEmbeddedDB(config);
		db.start(); // will fail with a ManagedProcessException
	}

	@Test
	public void testEmbeddedMariaDB4j() throws Exception {
		Configuration options = new Configuration();
		options.setPort(3307);
		DB db = DB.newEmbeddedDB(options);
		db.start();

		// TODO Should DB have a getJdbcURL() ? UID? PWD?
		Connection conn = db.getConnection();
		Statement stmt = conn.createStatement();
		
//		stmt.execute("CREATE DATABASE test2;");
//		stmt.execute("GRANT ALL on test2.* to 'testuser'@'localhost' identified by 'testpwd'");
//		stmt.execute("FLUSH PRIVILEGES;");
//		stmt.close();
//		conn.close();
//		conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test2", "testuser", "testpwd");
//		Statement stmt = conn.createStatement();
		
		stmt.execute("CREATE TABLE hello(world VARCHAR(100))");
		int i = stmt.executeUpdate("INSERT INTO hello VALUES ('Hello, world')");
		assertEquals(i, 1);

		ResultSet rs = stmt.executeQuery("SELECT * FROM hello");
		assertTrue(rs.next());
		String msg = rs.getString(1);
		assertEquals(msg, "Hello, world");
		
		// Yeah yeah close() should be in finally() ... it's just a test.
		rs.close();
		stmt.close();
		conn.close();
	}

}
