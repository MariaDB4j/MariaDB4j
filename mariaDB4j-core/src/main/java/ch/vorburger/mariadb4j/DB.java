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
package ch.vorburger.mariadb4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessBuilder;
import ch.vorburger.exec.ManagedProcessException;

/**
 * Provides capability to install, start, and use an embedded database.
 * 
 * @author Michael Vorburger
 * @author Michael Seaton
 */
public class DB {

    private static final Logger logger = LoggerFactory.getLogger(DB.class);

    protected final DBConfiguration config;

    private File baseDir;
    private File dataDir;
    private ManagedProcess mysqldProcess;

    protected int dbStartMaxWaitInMS = 30000;

    protected DB(DBConfiguration config) {
        this.config = config;
    }

    /**
     * This factory method is the mechanism for constructing a new embedded database for use. This
     * method automatically installs the database and prepares it for use.
     * 
     * @param config Configuration of the embedded instance
     * @return a new DB instance
     * @throws ManagedProcessException if something fatal went wrong
     */
    public static DB newEmbeddedDB(DBConfiguration config) throws ManagedProcessException {
        DB db = new DB(config);
        db.prepareDirectories();
        db.unpackEmbeddedDb();
        db.install();
        return db;
    }

    /**
     * This factory method is the mechanism for constructing a new embedded database for use. This
     * method automatically installs the database and prepares it for use with default
     * configuration, allowing only for specifying port.
     * 
     * @param port the port to start the embedded database on
     * @return a new DB instance
     * @throws ManagedProcessException if something fatal went wrong
     */
    public static DB newEmbeddedDB(int port) throws ManagedProcessException {
        DBConfigurationBuilder config = new DBConfigurationBuilder();
        config.setPort(port);
        return newEmbeddedDB(config.build());
    }

    ManagedProcess installPreparation() throws ManagedProcessException, IOException {
        logger.info("Installing a new embedded database to: " + baseDir);
        File installDbCmdFile = newExecutableFile("bin", "mysql_install_db");
        if (!installDbCmdFile.exists())
            installDbCmdFile = newExecutableFile("scripts", "mysql_install_db");
        if (!installDbCmdFile.exists())
            throw new ManagedProcessException(
                    "mysql_install_db was not found, neither in bin/ nor in scripts/ under "
                            + baseDir.getAbsolutePath());
        ManagedProcessBuilder builder = new ManagedProcessBuilder(installDbCmdFile);
        builder.addFileArgument("--datadir", dataDir).setWorkingDirectory(baseDir);
        if (!config.isWindows()) {
            builder.addFileArgument("--basedir", baseDir);
            builder.addArgument("--no-defaults");
            builder.addArgument("--force");
            builder.addArgument("--skip-name-resolve");
            // builder.addArgument("--verbose");
        }
        ManagedProcess mysqlInstallProcess = builder.build();
        return mysqlInstallProcess;
    }

    /**
     * Installs the database to the location specified in the configuration.
     * 
     * @throws ManagedProcessException if something fatal went wrong
     */
    protected void install() throws ManagedProcessException {
        try {
            ManagedProcess mysqlInstallProcess = installPreparation();
            mysqlInstallProcess.start();
            mysqlInstallProcess.waitForExit();
        } catch (Exception e) {
            throw new ManagedProcessException("An error occurred while installing the database", e);
        }
        logger.info("Installation complete.");
    }

    protected String getWinExeExt() {
        return config.isWindows() ? ".exe" : "";
    }

    /**
     * Starts up the database, using the data directory and port specified in the configuration.
     * 
     * @throws ManagedProcessException if something fatal went wrong
     */
    public synchronized void start() throws ManagedProcessException {
        logger.info("Starting up the database...");
        boolean ready = false;
        try {
            mysqldProcess = startPreparation();
            ready = mysqldProcess.startAndWaitForConsoleMessageMaxMs(getReadyForConnectionsTag(),
                    dbStartMaxWaitInMS);
        } catch (Exception e) {
            logger.error("failed to start mysqld", e);
            throw new ManagedProcessException("An error occurred while starting the database", e);
        }
        if (!ready) {
            if (mysqldProcess.isAlive())
                mysqldProcess.destroy();
            throw new ManagedProcessException(
                    "Database does not seem to have started up correctly? Magic string not seen in "
                            + dbStartMaxWaitInMS + "ms: " + getReadyForConnectionsTag()
                            + mysqldProcess.getLastConsoleLines());
        }
        logger.info("Database startup complete.");
    }

    protected String getReadyForConnectionsTag() {
        return "mysqld" + getWinExeExt() + ": ready for connections.";
    }

    synchronized ManagedProcess startPreparation() throws ManagedProcessException, IOException {
        ManagedProcessBuilder builder = new ManagedProcessBuilder(
                newExecutableFile("bin", "mysqld"));
        builder.addArgument("--no-defaults"); // *** THIS MUST COME FIRST ***
        builder.addArgument("--console");
        builder.addArgument("--skip-grant-tables");
        builder.addArgument("--max_allowed_packet=64M");
        builder.addFileArgument("--basedir", baseDir).setWorkingDirectory(baseDir);
        builder.addFileArgument("--datadir", dataDir);
        builder.addArgument("--port=" + config.getPort());
        if (!config.isWindows()) {
            builder.addArgument("--socket=" + getAbsoluteSocketPath());
        }
        for (String arg : config.getArgs()) {
            builder.addArgument(arg);
        }
        cleanupOnExit();
        // because cleanupOnExit() just installed our (class DB) own
        // Shutdown hook, we don't need the one from ManagedProcess:
        builder.setDestroyOnShutdown(false);
        logger.info("mysqld executable: " + builder.getExecutable());
        return builder.build();
    }

    protected File newExecutableFile(String dir, String exec) {
        return new File(baseDir, dir + "/" + exec + getWinExeExt());
    }

    /**
     * Config Socket as absolute path. By default this is the case because DBConfigurationBuilder
     * creates the socket in /tmp, but if a user uses setSocket() he may give a relative location,
     * so we double check.
     * 
     * @return config.getSocket() as File getAbsolutePath()
     */
    protected String getAbsoluteSocketPath() {
        String socket = config.getSocket();
        File socketFile = new File(socket);
        return socketFile.getAbsolutePath();
    }

    public void source(String resource) throws ManagedProcessException {
        source(resource, null, null, null);
    }

    /**
     * Takes in a string that represents a resource on the classpath and sources it via the mysql
     * command line tool.
     * 
     * @param resource the path to a resource on the classpath to source
     * @param username the username used to login to the database
     * @param password the password used to login to the database
     * @param dbName the name of the database (schema) to source into
     * @throws ManagedProcessException if something fatal went wrong
     */
    public void source(String resource, String username, String password, String dbName)
            throws ManagedProcessException {
        InputStream from = getClass().getClassLoader().getResourceAsStream(resource);
        if (from == null)
            throw new IllegalArgumentException("Could not find script file on the classpath at: "
                    + resource);
        run("script file sourced from the classpath at: " + resource, from, username, password,
                dbName);
    }

    public void run(String command, String username, String password, String dbName)
            throws ManagedProcessException {
        InputStream from = IOUtils.toInputStream(command, Charset.defaultCharset());
        run("command: " + command, from, username, password, dbName);
    }

    public void run(String command) throws ManagedProcessException {
        run(command, null, null, null);
    }

    protected void run(String logInfoText, InputStream fromIS, String username, String password,
            String dbName) throws ManagedProcessException {
        logger.info("Running a " + logInfoText);
        try {
            ManagedProcessBuilder builder = new ManagedProcessBuilder(newExecutableFile("bin",
                    "mysql"));
            builder.setWorkingDirectory(baseDir);
            if (username != null)
                builder.addArgument("-u" + username);
            if (password != null)
                builder.addArgument("-p" + password);
            if (dbName != null)
                builder.addArgument("-D" + dbName);
            if (!config.isWindows()) {
                builder.addArgument("--socket=" + getAbsoluteSocketPath());
            } else {
                builder.addArgument("--port=" + config.getPort());
            }
            if (fromIS != null)
                builder.setInputStream(fromIS);
            ManagedProcess process = builder.build();
            process.start();
            process.waitForExit();
        } catch (Exception e) {
            throw new ManagedProcessException("An error occurred while running a " + logInfoText, e);
        } finally {
            IOUtils.closeQuietly(fromIS);
        }
        logger.info("Successfully ran the " + logInfoText);
    }

    public void createDB(String dbName) throws ManagedProcessException {
        this.run("create database if not exists `" + dbName + "`;");
    }

    /**
     * Stops the database.
     * 
     * @throws ManagedProcessException if something fatal went wrong
     */
    public synchronized void stop() throws ManagedProcessException {
        if (mysqldProcess.isAlive()) {
            logger.debug("Stopping the database...");
            mysqldProcess.destroy();
            logger.info("Database stopped.");
        } else {
            logger.debug("Database was already stopped.");
        }
    }

    /**
     * Based on the current OS, unpacks the appropriate version of MariaDB to the file system based
     * on the configuration.
     */
    protected void unpackEmbeddedDb() {
        if (config.getBinariesClassPathLocation() == null) {
            logger.info("Not unpacking any embedded database (as BinariesClassPathLocation configuration is null)");
            return;
        }

        try {
            Util.extractFromClasspathToFile(config.getBinariesClassPathLocation(), baseDir);
            if (!config.isWindows()) {
                Util.forceExecutable(newExecutableFile("bin", "my_print_defaults"));
                Util.forceExecutable(newExecutableFile("bin", "mysql_install_db"));
                Util.forceExecutable(newExecutableFile("bin", "mysqld"));
                Util.forceExecutable(newExecutableFile("bin", "mysql"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error unpacking embedded DB", e);
        }
    }

    /**
     * If the data directory specified in the configuration is a temporary directory, this deletes
     * any previous version. It also makes sure that the directory exists.
     * 
     * @throws ManagedProcessException if something fatal went wrong
     */
    protected void prepareDirectories() throws ManagedProcessException {
        baseDir = Util.getDirectory(config.getBaseDir());
        try {
            String dataDirPath = config.getDataDir();
            if (Util.isTemporaryDirectory(dataDirPath)) {
                FileUtils.deleteDirectory(new File(dataDirPath));
            }
            dataDir = Util.getDirectory(dataDirPath);
        } catch (Exception e) {
            throw new ManagedProcessException(
                    "An error occurred while preparing the data directory", e);
        }
    }

    /**
     * Adds a shutdown hook to ensure that when the JVM exits, the database is stopped, and any
     * temporary data directories are cleaned up.
     */
    protected void cleanupOnExit() {
        String threadName = "Shutdown Hook Deletion Thread for Temporary DB " + dataDir.toString();
        final DB db = this;
        Runtime.getRuntime().addShutdownHook(new Thread(threadName) {

            @Override
            public void run() {
                // ManagedProcess DestroyOnShutdown ProcessDestroyer does
                // something similar, but it shouldn't hurt to better be save
                // than sorry and do it again ourselves here as well.
                try {
                    // Shut up and don't log if it was already stop() before
                    if (mysqldProcess != null && mysqldProcess.isAlive()) {
                        logger.info("cleanupOnExit() ShutdownHook now stopping database");
                        db.stop();
                    }
                } catch (ManagedProcessException e) {
                    logger.warn(
                            "cleanupOnExit() ShutdownHook: An error occurred while stopping the database",
                            e);
                }
                try {
                    if (dataDir.exists() && Util.isTemporaryDirectory(dataDir.getAbsolutePath())) {
                        logger.info("cleanupOnExit() ShutdownHook deleting temporary DB data directory: "
                                + dataDir);
                        FileUtils.deleteDirectory(dataDir);
                    }
                    if (baseDir.exists() && Util.isTemporaryDirectory(baseDir.getAbsolutePath())) {
                        logger.info("cleanupOnExit() ShutdownHook deleting temporary DB base directory: "
                                + baseDir);
                        FileUtils.deleteDirectory(baseDir);
                    }
                } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
                    // IGNORE, as this frequently happens, but isn't cause for any concern,
                    // because there isn't just one but N Shutdown Hook Threads, and if DB
                    // was used several times for one and the same shared baseDir / dataDir,
                    // then one Shutdown Hook Thread will have deleted files before another,
                    // and FileUtils would throw an IllegalArgumentException; we can ignore.
                } catch (@SuppressWarnings("unused") FileNotFoundException e) {
                    // dito, see above
                } catch (IOException e) {
                    logger.warn(
                            "cleanupOnExit() ShutdownHook: An error occurred while deleting a directory",
                            e);
                }
            }
        });
    }

}
