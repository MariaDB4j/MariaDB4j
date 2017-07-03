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
 * Tests the functioning of MariaDB4j Sample / Tutorial illustrating how to use MariaDB4j.
 * 
 * @author Michael Vorburger
 * @author Michael Seaton
 */
public class MariaDB4jSampleTutorialTest {

    @Test
    public void testEmbeddedMariaDB4j() throws Exception {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(0); // 0 => autom. detect free port
        DB db = DB.newEmbeddedDB(config.build());
        db.start();

        String dbName = "mariaDB4jTest"; // or just "test"
        if (!dbName.equals("test")) {
            // mysqld out-of-the-box already has a DB named "test"
            // in case we need another DB, here's how to create it first
            db.createDB(dbName);
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(config.getURL(dbName), "root", "");
            QueryRunner qr = new QueryRunner();

            // Should be able to create a new table
            qr.update(conn, "CREATE TABLE hello(world VARCHAR(100))");

            // Should be able to insert into a table
            qr.update(conn, "INSERT INTO hello VALUES ('Hello, world')");

            // Should be able to select from a table
            List<String> results = qr.query(conn, "SELECT * FROM hello",
                    new ColumnListHandler<String>());
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("Hello, world", results.get(0));

            // Should be able to source a SQL file
            db.source("ch/vorburger/mariadb4j/testSourceFile.sql", "root", null, dbName);
            db.source("ch/vorburger/mariadb4j/testSourceFile.sql", "root", "", dbName);
            results = qr.query(conn, "SELECT * FROM hello", new ColumnListHandler<String>());
            Assert.assertEquals(5, results.size());
            Assert.assertEquals("Hello, world", results.get(0));
            Assert.assertEquals("Bonjour, monde", results.get(1));
            Assert.assertEquals("Hola, mundo", results.get(2));
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    @Test
    public void testEmbeddedMariaDB4jWithSecurity() throws Exception {
        DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
        config.setPort(0); // 0 => autom. detect free port
        config.setSecurityDisabled(false);
        DB db = DB.newEmbeddedDB(config.build());
        db.start();

        String dbName = "mariaDB4jTestWSecurity"; // or just "test"
        if (!dbName.equals("test")) {
            // mysqld out-of-the-box already has a DB named "test"
            // in case we need another DB, here's how to create it first
            db.createDB(dbName, "root", "");
        }

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(config.getURL(dbName), "root", "");
            QueryRunner qr = new QueryRunner();

            // Should be able to create a new table
            qr.update(conn, "CREATE TABLE hello(world VARCHAR(100))");

            // Should be able to insert into a table
            qr.update(conn, "INSERT INTO hello VALUES ('Hello, world')");

            // Should be able to create a new user and grant privileges.
            qr.update(conn, "CREATE USER 'testUser'@'localhost' IDENTIFIED BY 'superSecret'");
            qr.update(conn, "GRANT ALL PRIVILEGES ON mariaDB4jTestWSecurity.* TO 'testUser'@'localhost'");

            //reconnect with the new user
            conn = DriverManager.getConnection(config.getURL(dbName), "testUser", "superSecret");

            // Should be able to select from a table
            List<String> results = qr.query(conn, "SELECT * FROM hello",
                    new ColumnListHandler<String>());
            Assert.assertEquals(1, results.size());
            Assert.assertEquals("Hello, world", results.get(0));

        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

}
