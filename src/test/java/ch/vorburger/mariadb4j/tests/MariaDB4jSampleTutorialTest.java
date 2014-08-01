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
package ch.vorburger.mariadb4j.tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.junit.Assert;
import org.junit.Test;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

/**
 * Tests the functioning of MariaDB4j
 * Sample / Tutorial illustrating how to use MariaDB4j.
 * 
 * @author Michael Vorburger
 * @author Michael Seaton
 */
public class MariaDB4jSampleTutorialTest {

	@Test
	public void testEmbeddedMariaDB4j() throws Exception {
		DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
		config.detectFreePort();
		DB db = DB.newEmbeddedDB(config.build());
		db.start();

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(config.getURL("test"), "root", "");
			QueryRunner qr = new QueryRunner();

			// Should be able to create a new table
			qr.update(conn, "CREATE TABLE hello(world VARCHAR(100))");

			// Should be able to insert into a table
			qr.update(conn, "INSERT INTO hello VALUES ('Hello, world')");

			// Should be able to select from a table
			List<String> results = qr.query(conn, "SELECT * FROM hello", new ColumnListHandler<String>());
			Assert.assertEquals(1, results.size());
			Assert.assertEquals("Hello, world", results.get(0));

			// Should be able to source a SQL file
			db.source("ch/vorburger/mariadb4j/testSourceFile.sql", "root", null, "test");
			results = qr.query(conn, "SELECT * FROM hello", new ColumnListHandler<String>());
			Assert.assertEquals(3, results.size());
			Assert.assertEquals("Hello, world", results.get(0));
			Assert.assertEquals("Bonjour, monde", results.get(1));
			Assert.assertEquals("Hola, mundo", results.get(2));
		}
		finally {
			DbUtils.closeQuietly(conn);
		}
	}

}
