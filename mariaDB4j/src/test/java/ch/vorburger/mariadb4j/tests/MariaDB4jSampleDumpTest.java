/*-
 * #%L
 * mariaDB4j (all-in-one artifact)
 * %%
 * Copyright (C) 2012 - 2017 Michael Vorburger
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

/**
 * Tests the functioning of MariaDB4j Dump/Restore illustrating how to use MariaDB4j.
 *
 * @author Carlos Ortiz
 */
public class MariaDB4jSampleDumpTest {

    private DB db;
    private DBConfigurationBuilder config;
    private static final String DBNAME = "planetexpress";

    @Before public void beforeTest() throws ManagedProcessException, SQLException {
        config = DBConfigurationBuilder.newBuilder();
        config.setPort(0);// 0 => autom. detect free port
        db = DB.newEmbeddedDB(config.build());
        db.start();
        db.createDB("planetexpress");
        db.source("ch/vorburger/mariadb4j/dumpTest.sql");

        // Now check DB's integrity
        Connection conn;
        conn = DriverManager.getConnection(config.getURL(DBNAME), "root", "");
        QueryRunner qr = new QueryRunner();
        // Should be able to create a new table
        List<String> results = qr.query(conn, "SELECT * FROM crew;", new ColumnListHandler<String>());
        assertEquals(4, results.size());
        assertEquals("John A Zoidberg", results.get(2));
    }

    @Test public void sqlDump() throws IOException, ManagedProcessException, SQLException {
        File outputDumpFile = File.createTempFile("sqlDump ", ".sql");
        ManagedProcess dumpProcess = db.dumpSQL(outputDumpFile, DBNAME, "root", "");
        dumpProcess.start();
        assertEquals(0, dumpProcess.waitForExit());
        assertTrue(outputDumpFile.exists() || outputDumpFile.isDirectory());
        assertTrue(FileUtils.sizeOf(outputDumpFile) > 0);
        FileUtils.forceDeleteOnExit(outputDumpFile);
    }

    @Test public void xmlDump() throws IOException, SAXException, ManagedProcessException, ParserConfigurationException, SQLException {
        File outputDumpFile = File.createTempFile("xmlsqlDump", ".xml");
        ManagedProcess dumpProcess = db.dumpXML(outputDumpFile, DBNAME, "root", "");
        dumpProcess.start();
        assertEquals(0, dumpProcess.waitForExit());
        assertTrue(outputDumpFile.exists() || outputDumpFile.isDirectory());
        assertTrue(FileUtils.sizeOf(outputDumpFile) > 0);
        // We just want to check that the file is a valid XML, output of it is mysqldump's responsability
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        dBuilder.parse(outputDumpFile);
        FileUtils.forceDeleteOnExit(outputDumpFile);
    }

    @After public void afterTest() throws ManagedProcessException {
        db.stop();
    }
}
