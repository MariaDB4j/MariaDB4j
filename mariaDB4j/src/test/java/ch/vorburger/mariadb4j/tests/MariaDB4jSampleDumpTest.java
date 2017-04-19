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

import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the functioning of MariaDB4j Dump/Restore illustrating how to use MariaDB4j.
 *
 * @author Michael Vorburger
 * @author Michael Seaton
 * @author Carlos Ortiz
 */
public class MariaDB4jSampleDumpTest {

    private  DB db;
    private  DBConfigurationBuilder config;
    public  final String DBNAME="planetexpress";


    public void beforeTest(){
        config = DBConfigurationBuilder.newBuilder();
        config.setPort(0);
        try {
            db = DB.newEmbeddedDB(// 0 => autom. detect free port
                    config.build());
            db.start();
            db.createDB("planetexpress");
            db.source("ch/vorburger/mariadb4j/dumpTest.sql");
        } catch (ManagedProcessException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void checkDBIntegrity(){
        Connection conn;
        try {
            conn = DriverManager.getConnection(config.getURL(DBNAME), "root", "");
            QueryRunner qr = new QueryRunner();
            // Should be able to create a new table
            List<String> resutls=qr.query(conn, "SELECT * FROM crew;",new ColumnListHandler<String>());
            assertEquals(4,resutls.size());
            assertEquals("John A Zoidberg",resutls.get(2));
        }catch (Exception ex){
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }

    @Test
    public void simpleDump(){
        try {
            beforeTest();
            checkDBIntegrity();
            File outputDumpFile = File.createTempFile("sqlDump ",".sql");
            ManagedProcess dumpProcess = db.dumpDB(outputDumpFile, DBNAME,"root","");
            dumpProcess.start();
            assertTrue(outputDumpFile.exists() || outputDumpFile.isDirectory());
            assertTrue(FileUtils.sizeOf(outputDumpFile)>0);
            FileUtils.forceDeleteOnExit(outputDumpFile);
        }catch (ManagedProcessException ex){
            ex.printStackTrace();
            fail(ex.getMessage());
        }catch (IOException ex){
            ex.printStackTrace();
            fail(ex.getMessage());
        }
        afterTest();
    }


    @Test
    public void XMLDump(){
        try {
            beforeTest();
            checkDBIntegrity();
            File outputDumpFile = File.createTempFile("xmlsqlDump",".xml");
            ManagedProcess dumpProcess = db.xmlDumpDB(outputDumpFile,DBNAME,"root","");
            dumpProcess.start();
            assertTrue(outputDumpFile.exists() || outputDumpFile.isDirectory());
            assertTrue(FileUtils.sizeOf(outputDumpFile)>0);
            // We just want to check that the file is a valid xml, output of it it's mysqldump resolvability .
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(outputDumpFile);
            FileUtils.forceDeleteOnExit(outputDumpFile);
        }catch (ManagedProcessException ex){
            ex.printStackTrace();
            fail(ex.getMessage());
        }catch (IOException ex){
            ex.printStackTrace();
            fail(ex.getMessage());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (SAXException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        afterTest();
    }

    public  void afterTest(){
        try {
            db.stop();
        } catch (ManagedProcessException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
