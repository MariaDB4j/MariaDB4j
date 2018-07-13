/*
 * #%L
 * MariaDB4j
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
package ch.vorburger.mariadb4j;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessBuilder;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.exec.OutputStreamLogDispatcher;

/**
 * Provides capability to install, start, and use an embedded database.
 *
 * @author Michael Vorburger
 * @author Michael Seaton
 */
public class DB {

    private static final Logger logger = LoggerFactory.getLogger(DB.class);

    protected final DBConfiguration configuration;

    private File baseDir;
    private File libDir;
    private File dataDir;
    private ManagedProcess mysqldProcess;

    protected int dbStartMaxWaitInMS = 30000;

    protected DB(DBConfiguration config) {
        this.configuration = config;
    }

    public DBConfiguration getConfiguration() {
        return configuration;
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
                    "mysql_install_db was not found, neither in bin/ nor in scripts/ under " + baseDir.getAbsolutePath());
        ManagedProcessBuilder builder = new ManagedProcessBuilder(installDbCmdFile);
        builder.setOutputStreamLogDispatcher(getOutputStreamLogDispatcher("mysql_install_db"));
        builder.getEnvironment().put(configuration.getOSLibraryEnvironmentVarName(), libDir.getAbsolutePath());
        builder.addFileArgument("--datadir", dataDir).setWorkingDirectory(baseDir);
        if (!configuration.isWindows()) {
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
    synchronized protected void install() throws ManagedProcessException {
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
        return configuration.isWindows() ? ".exe" : "";
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
            ready = mysqldProcess.startAndWaitForConsoleMessageMaxMs(getReadyForConnectionsTag(), dbStartMaxWaitInMS);
        } catch (Exception e) {
            logger.error("failed to start mysqld", e);
            throw new ManagedProcessException("An error occurred while starting the database", e);
        }
        if (!ready) {
            if (mysqldProcess.isAlive())
                mysqldProcess.destroy();
            throw new ManagedProcessException("Database does not seem to have started up correctly? Magic string not seen in "
                    + dbStartMaxWaitInMS + "ms: " + getReadyForConnectionsTag() + mysqldProcess.getLastConsoleLines());
        }
        logger.info("Database startup complete.");
    }

    protected String getReadyForConnectionsTag() {
        return "mysqld" + getWinExeExt() + ": ready for connections.";
    }

    synchronized ManagedProcess startPreparation() throws ManagedProcessException, IOException {
        ManagedProcessBuilder builder = new ManagedProcessBuilder(newExecutableFile("bin", "mysqld"));
        builder.setOutputStreamLogDispatcher(getOutputStreamLogDispatcher("mysqld"));
        builder.getEnvironment().put(configuration.getOSLibraryEnvironmentVarName(), libDir.getAbsolutePath());
        builder.addArgument("--no-defaults"); // *** THIS MUST COME FIRST ***
        builder.addArgument("--console");
        if(this.configuration.isSecurityDisabled()) {
            builder.addArgument("--skip-grant-tables");
        }
        if (! hasArgument("--max_allowed_packet")) {
            builder.addArgument("--max_allowed_packet=64M");
        }
        builder.addFileArgument("--basedir", baseDir).setWorkingDirectory(baseDir);
        builder.addFileArgument("--datadir", dataDir);
        addPortAndMaybeSocketArguments(builder);
        for (String arg : configuration.getArgs()) {
            builder.addArgument(arg);
        }
        cleanupOnExit();
        // because cleanupOnExit() just installed our (class DB) own
        // Shutdown hook, we don't need the one from ManagedProcess:
        builder.setDestroyOnShutdown(false);
        logger.info("mysqld executable: " + builder.getExecutable());
        return builder.build();
    }

    protected boolean hasArgument(final String argumentName) {
        for (String argument : this.configuration.getArgs()) {
            if (argument.startsWith(argumentName)) {
                return true;
            }
        }
        return false;
    }

    protected File newExecutableFile(String dir, String exec) {
        return new File(baseDir, dir + "/" + exec + getWinExeExt());
    }

    protected void addPortAndMaybeSocketArguments(ManagedProcessBuilder builder) throws IOException {
        builder.addArgument("--port=" + configuration.getPort());
        if (!configuration.isWindows()) {
            builder.addFileArgument("--socket", getAbsoluteSocketFile());
        }
    }

    protected void addSocketOrPortArgument(ManagedProcessBuilder builder) throws IOException {
        if (!configuration.isWindows()) {
            builder.addFileArgument("--socket", getAbsoluteSocketFile());
        } else {
            builder.addArgument("--port=" + configuration.getPort());
        }
    }

    /**
     * Config Socket as absolute path. By default this is the case because DBConfigurationBuilder
     * creates the socket in /tmp, but if a user uses setSocket() he may give a relative location,
     * so we double check.
     * 
     * @return config.getSocket() as File getAbsolutePath()
     */
    protected File getAbsoluteSocketFile() {
        String socket = configuration.getSocket();
        File socketFile = new File(socket);
        return socketFile.getAbsoluteFile();
    }

    public void source(String resource) throws ManagedProcessException {
        source(resource, null, null, null);
    }

    public void source(String resource, String dbName) throws ManagedProcessException {
        source(resource, null, null, dbName);
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
    public void source(String resource, String username, String password, String dbName) throws ManagedProcessException {
        InputStream from = getClass().getClassLoader().getResourceAsStream(resource);
        if (from == null)
            throw new IllegalArgumentException("Could not find script file on the classpath at: " + resource);
        run("script file sourced from the classpath at: " + resource, from, username, password, dbName);
    }

    public void run(String command, String username, String password, String dbName) throws ManagedProcessException {
        InputStream from = IOUtils.toInputStream(command, Charset.defaultCharset());
        run("command: " + command, from, username, password, dbName);
    }

    public void run(String command) throws ManagedProcessException {
        run(command, null, null, null);
    }

    public void run(String command, String username, String password) throws ManagedProcessException {
        run(command, username, password, null);
    }

    protected void run(String logInfoText, InputStream fromIS, String username, String password, String dbName)
            throws ManagedProcessException {
        logger.info("Running a " + logInfoText);
        try {
            ManagedProcessBuilder builder = new ManagedProcessBuilder(newExecutableFile("bin", "mysql"));
            builder.setOutputStreamLogDispatcher(getOutputStreamLogDispatcher("mysql"));
            builder.setWorkingDirectory(baseDir);
            if (username != null && !username.isEmpty())
                builder.addArgument("-u", username);
            if (password != null && !password.isEmpty())
                builder.addArgument("-p", password);
            if (dbName != null && !dbName.isEmpty())
                builder.addArgument("-D", dbName);
            addSocketOrPortArgument(builder);
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

    public void createDB(String dbName, String username, String password) throws ManagedProcessException {
        this.run("create database if not exists `" + dbName + "`;", username, password);
    }

    protected OutputStreamLogDispatcher getOutputStreamLogDispatcher(@SuppressWarnings("unused") String exec) {
        return new MariaDBOutputStreamLogDispatcher();
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
        if (configuration.getBinariesClassPathLocation() == null) {
            logger.info("Not unpacking any embedded database (as BinariesClassPathLocation configuration is null)");
            return;
        }

        try {
            Util.extractFromClasspathToFile(configuration.getBinariesClassPathLocation(), baseDir);
            if (!configuration.isWindows()) {
                Util.forceExecutable(newExecutableFile("bin", "my_print_defaults"));
                Util.forceExecutable(newExecutableFile("bin", "mysql_install_db"));
                Util.forceExecutable(newExecutableFile("scripts", "mysql_install_db"));
                Util.forceExecutable(newExecutableFile("bin", "mysqld"));
                Util.forceExecutable(newExecutableFile("bin", "mysqldump"));
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
        baseDir = Util.getDirectory(configuration.getBaseDir());
        libDir = Util.getDirectory(configuration.getLibDir());
        try {
            String dataDirPath = configuration.getDataDir();
            if (Util.isTemporaryDirectory(dataDirPath)) {
                FileUtils.deleteDirectory(new File(dataDirPath));
            }
            dataDir = Util.getDirectory(dataDirPath);
        } catch (Exception e) {
            throw new ManagedProcessException("An error occurred while preparing the data directory", e);
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
                    logger.warn("cleanupOnExit() ShutdownHook: An error occurred while stopping the database", e);
                }

                if (dataDir.exists() && (configuration.isDeletingTemporaryBaseAndDataDirsOnShutdown() && Util.isTemporaryDirectory(dataDir.getAbsolutePath()))) {
                    logger.info("cleanupOnExit() ShutdownHook quietly deleting temporary DB data directory: " + dataDir);
                    FileUtils.deleteQuietly(dataDir);
                }
                if (baseDir.exists() && (configuration.isDeletingTemporaryBaseAndDataDirsOnShutdown() && Util.isTemporaryDirectory(dataDir.getAbsolutePath()))) {
                    logger.info("cleanupOnExit() ShutdownHook quietly deleting temporary DB base directory: " + baseDir);
                    FileUtils.deleteQuietly(baseDir);
                }
            }
        });
    }

    // The dump*() methods are intentionally *NOT* made "synchronized",
    // (even though with --lock-tables one could not run two dumps concurrently anyway)
    // because in theory this could cause a long-running dump to deadlock an application
    // wanting to stop() a DB. Let it thus be a caller's responsibility to not dump
    // concurrently (and if she does, it just fails, which is much better than an
    // unexpected deadlock).

    public ManagedProcess dumpXML(File outputFile, String dbName, String user, String password)
            throws IOException, ManagedProcessException {
        return dump(outputFile, Arrays.asList(dbName), true, true, true, user, password);
    }

    public ManagedProcess dumpSQL(File outputFile, String dbName, String user, String password)
            throws IOException, ManagedProcessException {
        return dump(outputFile, Arrays.asList(dbName), true, true, false, user, password);
    }

    protected ManagedProcess dump(File outputFile, List<String> dbNamesToDump,
                                               boolean compactDump, boolean lockTables, boolean asXml,
                                               String user, String password)
            throws ManagedProcessException, IOException {

        ManagedProcessBuilder builder = new ManagedProcessBuilder(newExecutableFile("bin", "mysqldump"));

        builder.addStdOut(new BufferedOutputStream(new FileOutputStream(outputFile)));
        builder.setOutputStreamLogDispatcher(getOutputStreamLogDispatcher("mysqldump"));
        builder.addArgument("--port=" + configuration.getPort());
        if (!configuration.isWindows()) {
            builder.addFileArgument("--socket", getAbsoluteSocketFile());
        }
        if (lockTables) {
            builder.addArgument("--flush-logs");
            builder.addArgument("--lock-tables");
        }
        if (compactDump) {
            builder.addArgument("--compact");
        }
        if (asXml) {
            builder.addArgument("--xml");
        }
        if (StringUtils.isNotBlank(user)) {
            builder.addArgument("-u");
            builder.addArgument(user);
            if (StringUtils.isNotBlank(password)) {
                builder.addArgument("-p" + password);
            }
        }
        builder.addArgument(StringUtils.join(dbNamesToDump, StringUtils.SPACE));
        builder.setDestroyOnShutdown(true);
        return builder.build();
    }
}
