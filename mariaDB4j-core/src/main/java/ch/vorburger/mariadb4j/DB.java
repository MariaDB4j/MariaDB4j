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

import static ch.vorburger.mariadb4j.DBConfiguration.Executable.*;

import ch.vorburger.exec.*;
import ch.vorburger.mariadb4j.DBConfiguration.Executable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides capability to install, start, and use an embedded database.
 *
 * @author Michael Vorburger
 * @author Michael Seaton
 * @author Gordon Little
 */
public class DB {

    private static final Logger logger = LoggerFactory.getLogger(DB.class);

    protected final DBConfiguration configuration;

    private Path baseDir;
    private Path libDir;
    private Path dataDir;
    private Path tmpDir;
    private ManagedProcess mysqldProcess;

    protected int dbStartMaxWaitInMS = 30000;

    protected DB(DBConfiguration config) {
        configuration = config;
    }

    /**
     * Getter for the field <code>configuration</code>.
     *
     * @return a {@link ch.vorburger.mariadb4j.DBConfiguration} object
     */
    public DBConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * This factory method is the mechanism for constructing a new embedded database for use. This
     * method automatically installs the database and prepares it for use.
     *
     * <p>If the data directory already exists for this DBConfiguration, it wil be reused.
     *
     * @param config Configuration of the embedded instance
     * @return a new DB instance
     * @throws ch.vorburger.exec.ManagedProcessException if something fatal went wrong
     */
    public static DB newEmbeddedDB(DBConfiguration config) throws ManagedProcessException {
        DB db = new DB(config);
        db.prepareDirectories();
        // If the data dir does not already exist and is empty, proceed to install normally.
        // Otherwise, we will reuse the existing data directory.
        try {
            final Path absPath = config.dataDir().toAbsolutePath();
            if (!Files.isDirectory(absPath)) return db;
            try (Stream<Path> entries = Files.list(absPath)) {
                if (entries.findFirst().isPresent()) return db;
                db.unpackEmbeddedDb();
                db.install();
            }
        } catch (IOException e) { // do not change the method signature, catch, wrap, and re-throw
            throw new ManagedProcessException(e.getMessage(), e);
        }
        return db;
    }

    /**
     * This factory method is the mechanism for constructing a new embedded database for use. This
     * method automatically installs the database and prepares it for use with default
     * configuration, allowing only for specifying port.
     *
     * @param port the port to start the embedded database on
     * @return a new DB instance
     * @throws ch.vorburger.exec.ManagedProcessException if something fatal went wrong
     */
    @SuppressWarnings("unused")
    public static DB newEmbeddedDB(int port) throws ManagedProcessException {
        DBConfigurationBuilder config = new DBConfigurationBuilder();
        config.setPort(port);
        return newEmbeddedDB(config.build());
    }

    protected ManagedProcess createDBInstallProcess() throws IOException {
        logger.info("Installing a new embedded database to: {}", baseDir);
        Path installDbCmdFile = configuration.getExecutable(Executable.INSTALL_DB);
        ManagedProcessBuilder builder = new ManagedProcessBuilder(installDbCmdFile.toFile());
        builder.setOutputStreamLogDispatcher(getOutputStreamLogDispatcher("mysql_install_db"));
        builder.getEnvironment()
                .put(
                        configuration.getOSLibraryEnvironmentVarName(),
                        String.valueOf(libDir.toAbsolutePath()));
        File baseDirAsFile = baseDir.toFile();
        File dataDirAsFile = dataDir.toFile();
        builder.setWorkingDirectory(baseDirAsFile);
        if (!configuration.isWindows()) {
            builder.addFileArgument("--datadir", dataDirAsFile);
            builder.addFileArgument("--basedir", baseDirAsFile);
            builder.addArgument("--no-defaults");
            builder.addArgument("--force");
            builder.addArgument("--skip-name-resolve");
            // builder.addArgument("--verbose");
        } else {
            builder.addFileArgument("--datadir", dataDirAsFile.getCanonicalFile());
        }
        return builder.build();
    }

    /**
     * Installs the database to the location specified in the configuration.
     *
     * @throws ch.vorburger.exec.ManagedProcessException if something fatal went wrong
     */
    protected synchronized void install() throws ManagedProcessException {
        try {
            ManagedProcess mysqlInstallProcess = createDBInstallProcess();
            mysqlInstallProcess.start();
            mysqlInstallProcess.waitForExit();
        } catch (Exception e) {
            throw new ManagedProcessException("An error occurred while installing the database", e);
        }
        logger.info("Installation complete.");
    }

    /**
     * Starts up the database, using the data directory and port specified in the configuration.
     *
     * @throws ch.vorburger.exec.ManagedProcessException if something fatal went wrong
     */
    public synchronized void start() throws ManagedProcessException {
        logger.info("Starting up the database...");
        boolean ready;
        try {
            mysqldProcess = startPreparation();
            ready =
                    mysqldProcess.startAndWaitForConsoleMessageMaxMs(
                            getReadyForConnectionsTag(), dbStartMaxWaitInMS);
        } catch (Exception e) {
            throw new ManagedProcessException("An error occurred while starting the database", e);
        }
        if (!ready) {
            if (mysqldProcess != null && mysqldProcess.isAlive()) {
                mysqldProcess.destroy();
            }
            throw new ManagedProcessException(
                    "Database does not seem to have started up correctly? Magic string not seen in "
                            + dbStartMaxWaitInMS
                            + "ms: "
                            + getReadyForConnectionsTag()
                            + mysqldProcess.getLastConsoleLines());
        }
        logger.info("Database startup complete.");
    }

    protected String getReadyForConnectionsTag() {
        return ": ready for connections.";
    }

    synchronized ManagedProcess startPreparation() throws IOException {
        ManagedProcessBuilder builder =
                new ManagedProcessBuilder(configuration.getExecutable(SERVER).toFile());
        builder.setOutputStreamLogDispatcher(getOutputStreamLogDispatcher("mysqld"));
        builder.getEnvironment()
                .put(
                        configuration.getOSLibraryEnvironmentVarName(),
                        String.valueOf(libDir.toAbsolutePath()));
        builder.addArgument("--no-defaults"); // *** THIS MUST COME FIRST ***
        builder.addArgument("--console");
        if (configuration.isSecurityDisabled()) {
            builder.addArgument("--skip-grant-tables");
        }
        if (!hasArgument("--max_allowed_packet")) {
            builder.addArgument("--max_allowed_packet=64M");
        }
        File baseDirAsFile = baseDir.toFile();
        File dataDirAsFile = dataDir.toFile();
        File tmpDirAsFile = tmpDir.toFile();
        builder.addFileArgument("--basedir", baseDirAsFile).setWorkingDirectory(baseDirAsFile);
        if (!configuration.isWindows()) {
            builder.addFileArgument("--datadir", dataDirAsFile);
            builder.addFileArgument("--tmpdir", tmpDirAsFile);
        } else {
            builder.addFileArgument("--datadir", dataDirAsFile.getCanonicalFile());
            builder.addFileArgument("--tmpdir", tmpDirAsFile.getCanonicalFile());
        }
        addPortAndMaybeSocketArguments(builder);
        for (String arg : configuration.args()) {
            builder.addArgument(arg);
        }
        if (StringUtils.isNotBlank(configuration.defaultCharacterSet())) {
            builder.addArgument("--character-set-server=", configuration.defaultCharacterSet());
        }
        cleanupOnExit();
        // because cleanupOnExit() just installed our (class DB) own
        // Shutdown hook, we don't need the one from ManagedProcess:
        builder.setDestroyOnShutdown(false);
        logger.info("mysqld executable: {}", builder.getExecutable());
        return builder.build();
    }

    @SuppressWarnings("SameParameterValue")
    protected boolean hasArgument(final String argumentName) {
        for (String argument : configuration.args()) {
            if (argument.startsWith(argumentName)) {
                return true;
            }
        }
        return false;
    }

    protected void addPortAndMaybeSocketArguments(ManagedProcessBuilder builder)
            throws IOException {
        builder.addArgument("--port=" + configuration.port());
        if (!configuration.isWindows()) {
            builder.addFileArgument("--socket", getAbsoluteSocketFile().toFile());
        }
    }

    protected void addSocketOrPortArgument(ManagedProcessBuilder builder) throws IOException {
        if (!configuration.isWindows()) {
            builder.addFileArgument("--socket", getAbsoluteSocketFile().toFile());
        } else {
            builder.addArgument("--port=" + configuration.port());
        }
    }

    /**
     * Config Socket as absolute path. By default, this is the case because DBConfigurationBuilder
     * creates the socket in /tmp, but if a user uses setSocket() he may give a relative location,
     * so we double-check.
     *
     * @return config.getSocket() as Path toAbsolutePath()
     */
    protected Path getAbsoluteSocketFile() {
        return Path.of(configuration.socket()).toAbsolutePath();
    }

    /**
     * Source.
     *
     * @param resource a {@link java.lang.String} object
     */
    public void source(String resource) {
        source(resource, null, null, null);
    }

    /**
     * Source.
     *
     * @param resource a {@link java.io.InputStream} object
     * @throws ch.vorburger.exec.ManagedProcessException if any.
     */
    @SuppressWarnings("unused")
    public void source(InputStream resource) throws ManagedProcessException {
        source(resource, null, null, null);
    }

    /**
     * Source.
     *
     * @param resource a {@link java.lang.String} object
     * @param dbName a {@link java.lang.String} object
     */
    public void source(String resource, String dbName) {
        source(resource, null, null, dbName);
    }

    /**
     * Source.
     *
     * @param resource a {@link java.io.InputStream} object
     * @param dbName a {@link java.lang.String} object
     * @throws ch.vorburger.exec.ManagedProcessException if any.
     */
    @SuppressWarnings("unused")
    public void source(InputStream resource, String dbName) throws ManagedProcessException {
        source(resource, null, null, dbName);
    }

    /**
     * Takes in a {@link java.io.InputStream} and sources it via the mysql command line tool.
     *
     * @param resource an {@link java.io.InputStream} InputStream to source
     * @param username the username used to log in to the database
     * @param password the password used to log in to the database
     * @param dbName the name of the database (schema) to source into
     * @throws ch.vorburger.exec.ManagedProcessException if something fatal went wrong
     */
    public void source(InputStream resource, String username, String password, String dbName)
            throws ManagedProcessException {
        run("script file sourced from an InputStream", resource, username, password, dbName, false);
    }

    /**
     * Takes in a string that represents a resource on the classpath and sources it via the mysql
     * command line tool.
     *
     * @param resource the path to a resource on the classpath to source
     * @param username the username used to log in to the database
     * @param password the password used to log in to the database
     * @param dbName the name of the database (schema) to source into
     */
    public void source(String resource, String username, String password, String dbName) {
        source(resource, username, password, dbName, false);
    }

    /**
     * Takes in a string that represents a resource on the classpath and sources it via the mysql
     * command line tool. Optionally force continue if individual statements fail.
     *
     * @param resource the path to a resource on the classpath to source
     * @param username the username used to log in to the database
     * @param password the password used to log in to the database
     * @param dbName the name of the database (schema) to source into
     * @param force if true then continue on error (mysql --force)
     */
    public void source(
            String resource, String username, String password, String dbName, boolean force) {
        try (InputStream from = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (from == null) {
                throw new IllegalArgumentException(
                        "Could not find script file on the classpath at: " + resource);
            }
            run(
                    "script file sourced from the classpath at: " + resource,
                    from,
                    username,
                    password,
                    dbName,
                    force);
        } catch (IOException ioe) {
            logger.warn(
                    "Issue trying to close source InputStream. Raise warning and continue.", ioe);
        }
    }

    /**
     * Run.
     *
     * @param command a {@link java.lang.String} object
     * @param username a {@link java.lang.String} object
     * @param password a {@link java.lang.String} object
     * @param dbName a {@link java.lang.String} object
     * @throws ch.vorburger.exec.ManagedProcessException if any.
     */
    public void run(String command, String username, String password, String dbName)
            throws ManagedProcessException {
        run(command, username, password, dbName, false, true);
    }

    /**
     * Run.
     *
     * @param command a {@link java.lang.String} object
     * @throws ch.vorburger.exec.ManagedProcessException if any.
     */
    public void run(String command) throws ManagedProcessException {
        run(command, null, null, null);
    }

    /**
     * Run.
     *
     * @param command a {@link java.lang.String} object
     * @param username a {@link java.lang.String} object
     * @param password a {@link java.lang.String} object
     * @throws ch.vorburger.exec.ManagedProcessException if any.
     */
    public void run(String command, String username, String password)
            throws ManagedProcessException {
        run(command, username, password, null);
    }

    /**
     * Run.
     *
     * @param command a {@link java.lang.String} object
     * @param username a {@link java.lang.String} object
     * @param password a {@link java.lang.String} object
     * @param dbName a {@link java.lang.String} object
     * @param force a boolean
     * @throws ch.vorburger.exec.ManagedProcessException if any.
     */
    public void run(String command, String username, String password, String dbName, boolean force)
            throws ManagedProcessException {
        run(command, username, password, dbName, force, true);
    }

    /**
     * Run.
     *
     * @param command a {@link java.lang.String} object
     * @param username a {@link java.lang.String} object
     * @param password a {@link java.lang.String} object
     * @param dbName a {@link java.lang.String} object
     * @param force a boolean
     * @param verbose a boolean
     * @throws ch.vorburger.exec.ManagedProcessException if any.
     */
    public void run(
            String command,
            String username,
            String password,
            String dbName,
            boolean force,
            boolean verbose)
            throws ManagedProcessException {
        // If resource is created here, it should probably be released here also (as opposed to in
        // protected run method)
        // Also move to try-with-resource syntax to remove closeQuietly deprecation errors.
        try (InputStream from = IOUtils.toInputStream(command, Charset.defaultCharset())) {
            final String logInfoText =
                    verbose
                            ? "command: " + command
                            : "command (" + command.length() / 1_024 + " KiB long)";
            run(logInfoText, from, username, password, dbName, force);
        } catch (IOException ioe) {
            logger.warn(
                    "Issue trying to close source InputStream. Raise warning and continue.", ioe);
        }
    }

    protected void run(
            String logInfoText,
            InputStream fromIS,
            String username,
            String password,
            String dbName,
            boolean force)
            throws ManagedProcessException {
        logger.info("Running a {}", logInfoText);
        try {
            ManagedProcessBuilder builder =
                    new ManagedProcessBuilder(configuration.getExecutable(CLIENT).toFile());
            builder.setOutputStreamLogDispatcher(getOutputStreamLogDispatcher("mysql"));
            builder.setWorkingDirectory(baseDir.toFile());
            builder.addArgument("--default-character-set=utf8");
            if (username != null && !username.isEmpty()) {
                builder.addArgument("-u", username);
            }
            if (password != null && !password.isEmpty()) {
                builder.addArgument("-p", password);
            }
            if (dbName != null && !dbName.isEmpty()) {
                builder.addArgument("-D", dbName);
            }
            if (force) {
                builder.addArgument("-f");
            }
            addSocketOrPortArgument(builder);
            if (fromIS != null) {
                builder.setInputStream(fromIS);
            }
            if (configuration.getProcessListener() != null) {
                builder.setProcessListener(configuration.getProcessListener());
            }
            if (configuration.defaultCharacterSet() != null) {
                builder.addArgument(
                        "--default-character-set=", configuration.defaultCharacterSet());
            }

            ManagedProcess process = builder.build();
            process.start();
            process.waitForExit();
        } catch (Exception e) {
            throw new ManagedProcessException(
                    "An error occurred while running a " + logInfoText, e);
        }
        logger.info("Successfully ran the {}", logInfoText);
    }

    /**
     * CreateDB.
     *
     * @param dbName a {@link java.lang.String} object
     * @throws ch.vorburger.exec.ManagedProcessException if any.
     */
    public void createDB(String dbName) throws ManagedProcessException {
        this.run("create database if not exists `" + dbName + "`;");
    }

    /**
     * CreateDB.
     *
     * @param dbName a {@link java.lang.String} object
     * @param username a {@link java.lang.String} object
     * @param password a {@link java.lang.String} object
     * @throws ch.vorburger.exec.ManagedProcessException if any.
     */
    public void createDB(String dbName, String username, String password)
            throws ManagedProcessException {
        this.run("create database if not exists `" + dbName + "`;", username, password);
    }

    @SuppressWarnings("unused")
    protected OutputStreamLogDispatcher getOutputStreamLogDispatcher(String exec) {
        return new MariaDBOutputStreamLogDispatcher();
    }

    /**
     * Stops the database.
     *
     * @throws ch.vorburger.exec.ManagedProcessException if something fatal went wrong
     */
    public synchronized void stop() throws ManagedProcessException {
        if (mysqldProcess != null && mysqldProcess.isAlive()) {
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
        if (configuration.binariesClassPathLocation() == null) {
            logger.info(
                    "Not unpacking any embedded database (as BinariesClassPathLocation configuration is null)");
            return;
        }

        try {
            Util.extractFromClasspathToDir(configuration.binariesClassPathLocation(), baseDir);
            if (!configuration.isWindows()) {
                Util.forceExecutable(configuration.getExecutable(PRINT_DEFAULTS));
                Util.forceExecutable(configuration.getExecutable(INSTALL_DB));
                Util.forceExecutable(configuration.getExecutable(SERVER));
                Util.forceExecutable(configuration.getExecutable(DUMP));
                Util.forceExecutable(configuration.getExecutable(CLIENT));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error unpacking embedded DB", e);
        }
    }

    /**
     * If the data directory specified in the configuration is a temporary directory, this deletes
     * any previous version. It also makes sure that the directory exists.
     *
     * @throws ch.vorburger.exec.ManagedProcessException if something fatal went wrong
     */
    protected void prepareDirectories() throws ManagedProcessException {
        baseDir = Util.getDirectory(configuration.baseDir());
        libDir = Util.getDirectory(configuration.libDir());
        tmpDir = Util.getDirectory(configuration.tmpDir());
        try {
            Path dataDirPath = configuration.dataDir();
            if (configuration.isDeletingTemporaryBaseAndDataDirsOnShutdown()
                    && Files.exists(dataDirPath)
                    && Util.isTemporaryDirectory(dataDirPath)) {
                PathUtils.deleteDirectory(dataDirPath);
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
        Runtime.getRuntime()
                .addShutdownHook(
                        new DBShutdownHook(
                                threadName,
                                db,
                                () -> mysqldProcess,
                                () -> baseDir,
                                () -> dataDir,
                                () -> tmpDir,
                                configuration));
    }

    // The dump*() methods are intentionally *NOT* made "synchronized",
    // (even though with --lock-tables one could not run two dumps concurrently anyway)
    // because in theory this could cause a long-running dump to deadlock an application
    // wanting to stop() a DB. Let it thus be a caller's responsibility to not dump
    // concurrently (and if she does, it just fails, which is much better than an
    // unexpected deadlock).

    /**
     * DumpXML.
     *
     * @param outputFile a {@link java.nio.file.Path} object
     * @param dbName a {@link java.lang.String} object
     * @param user a {@link java.lang.String} object
     * @param password a {@link java.lang.String} object
     * @return a {@link ch.vorburger.exec.ManagedProcess} object
     * @throws java.io.IOException if any.
     */
    public ManagedProcess dumpXML(Path outputFile, String dbName, String user, String password)
            throws IOException {
        return dump(
                outputFile, Collections.singletonList(dbName), true, true, true, user, password);
    }

    /**
     * DumpSQL.
     *
     * @param outputFile a {@link java.nio.file.Path} object
     * @param dbName a {@link java.lang.String} object
     * @param user a {@link java.lang.String} object
     * @param password a {@link java.lang.String} object
     * @return a {@link ch.vorburger.exec.ManagedProcess} object
     * @throws java.io.IOException if any.
     */
    public ManagedProcess dumpSQL(Path outputFile, String dbName, String user, String password)
            throws IOException {
        return dump(
                outputFile, Collections.singletonList(dbName), true, true, false, user, password);
    }

    @SuppressWarnings("SameParameterValue")
    protected ManagedProcess dump(
            Path outputFile,
            List<String> dbNamesToDump,
            boolean compactDump,
            boolean lockTables,
            boolean asXml,
            String user,
            String password)
            throws IOException {

        ManagedProcessBuilder builder =
                new ManagedProcessBuilder(configuration.getExecutable(DUMP).toFile());

        BufferedOutputStream outputStream =
                new BufferedOutputStream(Files.newOutputStream(outputFile));
        builder.addStdOut(outputStream);
        builder.setOutputStreamLogDispatcher(getOutputStreamLogDispatcher("mysqldump"));
        builder.addArgument("--port=" + configuration.port());
        if (!configuration.isWindows()) {
            builder.addFileArgument("--socket", getAbsoluteSocketFile().toFile());
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
        builder.setProcessListener(
                new ManagedProcessListener() {
                    @Override
                    public void onProcessComplete(int i) {
                        closeOutputStream();
                    }

                    @Override
                    public void onProcessFailed(int i, @NonNull Throwable throwable) {
                        closeOutputStream();
                    }

                    private void closeOutputStream() {
                        try {
                            outputStream.close();
                        } catch (IOException exception) {
                            logger.error(
                                    "Problem while trying to close the stream to the file containing the DB dump",
                                    exception);
                        }
                    }
                });
        return builder.build();
    }
}
