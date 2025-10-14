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

import static ch.vorburger.mariadb4j.TestUtil.buildTempDB;
import static ch.vorburger.mariadb4j.TestUtil.configureTempDB;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests the functioning of MariaDB4j Dump/Restore illustrating how to use MariaDB4j.
 *
 * @author Carlos Ortiz
 */
class MariaDB4jSampleDumpTest {

    private static final String DBNAME = "planetexpress";
    private DB db;

    @TempDir private Path tempDir;

    @BeforeEach
    void beforeTest() throws ManagedProcessException, SQLException {
        DBConfigurationBuilder config = configureTempDB(tempDir);
        db = buildTempDB(tempDir, config);
        db.start();
        db.createDB("planetexpress");
        db.source("ch/vorburger/mariadb4j/dumpTest.sql");

        try (var conn = DriverManager.getConnection(config.getURL(DBNAME), "root", "")) {
            var qr = new QueryRunner();
            var results = qr.query(conn, "SELECT * FROM crew;", new ColumnListHandler<String>());
            assertEquals(4, results.size());
            assertEquals("John A Zoidberg", results.get(2));
        }
    }

    @Test
    void sqlDump() throws IOException {
        Path outputDumpFile = Files.createTempFile(tempDir, "sqlDump ", ".sql");
        ManagedProcess dumpProcess = db.dumpSQL(outputDumpFile, DBNAME, "root", "");
        dumpProcess.start();
        assertEquals(0, dumpProcess.waitForExit());
        assertTrue(Files.isRegularFile(outputDumpFile));
        assertTrue(Files.size(outputDumpFile) > 0);
    }

    @Test
    void xmlDump() throws IOException, SAXException, ParserConfigurationException {
        Path outputDumpFile = Files.createTempFile(tempDir, "xmlsqlDump", ".xml");
        ManagedProcess dumpProcess = db.dumpXML(outputDumpFile, DBNAME, "root", "");
        dumpProcess.start();
        assertEquals(0, dumpProcess.waitForExit());
        assertTrue(Files.isRegularFile(outputDumpFile));
        assertTrue(Files.size(outputDumpFile) > 0);
        // We just want to check that the file is a valid XML, output of it is mysqldump's
        // responsibility
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        dBuilder.parse(new InputSource(outputDumpFile.toUri().toASCIIString()));
    }

    @AfterEach
    void afterTest() throws ManagedProcessException {
        db.stop();
    }
}
