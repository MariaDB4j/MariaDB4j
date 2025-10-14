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

import static ch.vorburger.mariadb4j.DBConfiguration.Executable.*;

import static java.util.Objects.requireNonNull;

import ch.vorburger.exec.ManagedProcessListener;
import ch.vorburger.mariadb4j.DBConfiguration.Executable;

import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

/**
 * Builder for DBConfiguration. Has lots of sensible default conventions etc.
 *
 * @author Michael Vorburger
 */
public class DBConfigurationBuilder {

    // TODO The defaulting logic here is too convulted, and should be redone one day...
    //  It should be simple: By default, a unique ephemeral directory should be used (not based on
    // port);
    //  unless the user explicitly sets another directory, in which case that should be used
    // instead.

    protected static final String WINX64 = "winx64";
    protected static final String LINUX = "linux";
    protected static final String OSX = "osx";

    static final String DEFAULT_DATA_DIR = "data";

    static final String DEFAULT_TMP_DIR = "tmp";

    private static final Path SYSTEM_TEMP_DIR = Path.of(SystemUtils.JAVA_IO_TMPDIR);

    private String databaseVersion = null;

    // All the following are just the defaults, which can be overridden
    protected String osDirectoryName =
            switch (Platform.get()) {
                case LINUX -> LINUX;
                case MAC -> OSX;
                case WINDOWS -> WINX64;
            };
    protected Path baseDir = SYSTEM_TEMP_DIR.resolve("MariaDB4j").resolve("base");
    protected Path libDir = null;

    protected Path dataDir = SYSTEM_TEMP_DIR.resolve(DEFAULT_DATA_DIR);
    protected Path tmpDir = SYSTEM_TEMP_DIR.resolve(DEFAULT_TMP_DIR);
    protected String socket = null; // see _getSocket()
    protected int port = 0;
    protected boolean isDeletingTemporaryBaseAndDataDirsOnShutdown = true;
    protected boolean isUnpackingFromClasspath = true;
    protected List<String> args = new ArrayList<>();
    private boolean isSecurityDisabled = true;

    private boolean frozen = false;
    private ManagedProcessListener listener;

    protected String defaultCharacterSet = null;
    protected Map<Executable, Supplier<Path>> executables = new EnumMap<>(Executable.class);

    public static DBConfigurationBuilder newBuilder() {
        return new DBConfigurationBuilder();
    }

    public static DBConfigurationBuilder newBuilder(DBConfigurationBuilder cloneFrom) {
        DBConfigurationBuilder builder = new DBConfigurationBuilder();
        builder.databaseVersion = cloneFrom.databaseVersion;
        builder.osDirectoryName = cloneFrom.osDirectoryName;
        builder.baseDir = cloneFrom.baseDir;
        builder.libDir = cloneFrom.libDir;
        builder.dataDir = cloneFrom.dataDir;
        builder.tmpDir = cloneFrom.tmpDir;
        builder.socket = cloneFrom.socket;
        builder.port = cloneFrom.port;
        builder.isDeletingTemporaryBaseAndDataDirsOnShutdown =
                cloneFrom.isDeletingTemporaryBaseAndDataDirsOnShutdown;
        builder.isUnpackingFromClasspath = cloneFrom.isUnpackingFromClasspath;
        builder.args = new ArrayList<>(cloneFrom.args);
        builder.isSecurityDisabled = cloneFrom.isSecurityDisabled;
        builder.frozen = cloneFrom.frozen;
        builder.listener = cloneFrom.listener;
        builder.defaultCharacterSet = cloneFrom.defaultCharacterSet;
        builder.executables = new EnumMap<>(cloneFrom.executables);
        return builder;
    }

    protected DBConfigurationBuilder() {}

    protected void checkIfFrozen(String setterName) {
        if (frozen)
            throw new IllegalStateException("cannot " + setterName + "() anymore after build()");
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public String path() {
        return "MariaDB4j/" + java.util.UUID.randomUUID() + "-" + port;
    }

    public DBConfigurationBuilder setBaseDir(Path baseDir) {
        checkIfFrozen("setBaseDir");
        this.baseDir = baseDir;
        return this;
    }

    public Path getLibDir() {
        if (libDir == null) libDir = baseDir.resolve("libs");
        return libDir;
    }

    public DBConfigurationBuilder setLibDir(Path libDir) {
        checkIfFrozen("setLibDir");
        this.libDir = libDir;
        return this;
    }

    public Path getDataDir() {
        return dataDir;
    }

    public DBConfigurationBuilder setDataDir(Path dataDir) {
        checkIfFrozen("setDataDir");
        this.dataDir = dataDir;
        return this;
    }

    public Path getTmpDir() {
        return tmpDir;
    }

    @SuppressWarnings("UnusedReturnValue")
    public DBConfigurationBuilder setTmpDir(Path tmpDir) {
        checkIfFrozen("setTmpDir");
        this.tmpDir =
                Objects.requireNonNullElseGet(
                        tmpDir, () -> SYSTEM_TEMP_DIR.resolve(path()).resolve(DEFAULT_TMP_DIR));
        return this;
    }

    public int getPort() {
        return port;
    }

    /**
     * Sets the port number.
     *
     * @param port port number, or 0 to use detectFreePort()
     * @return this
     */
    public DBConfigurationBuilder setPort(int port) {
        checkIfFrozen("setPort");
        this.port = port;
        return this;
    }

    /**
     * Set a custom process listener to listen to DB start/shutdown events.
     *
     * @param listener custom listener
     * @return this
     */
    @SuppressWarnings("unused")
    public DBConfigurationBuilder setProcessListener(ManagedProcessListener listener) {
        this.listener = listener;
        return this;
    }

    public ManagedProcessListener getProcessListener() {
        return listener;
    }

    public boolean isDeletingTemporaryBaseAndDataDirsOnShutdown() {
        return isDeletingTemporaryBaseAndDataDirsOnShutdown;
    }

    /**
     * Defines if the configured data and base directories should be deleted on shutdown. If you've
     * set the base and data directories to non-temporary directories using {@link
     * #setBaseDir(Path)} or {@link #setDataDir(Path)}, then they'll also never get deleted anyway.
     *
     * @param doDelete Default value is true, set false to override
     * @return returns this
     */
    @SuppressWarnings("UnusedReturnValue")
    public DBConfigurationBuilder setDeletingTemporaryBaseAndDataDirsOnShutdown(boolean doDelete) {
        checkIfFrozen("keepsDataAndBaseDir");
        isDeletingTemporaryBaseAndDataDirsOnShutdown = doDelete;
        return this;
    }

    public static int detectFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to detect a free port", e);
        }
    }

    public String getSocket() {
        return socket;
    }

    @SuppressWarnings("UnusedReturnValue")
    public DBConfigurationBuilder setSocket(String socket) {
        checkIfFrozen("setSocket");
        this.socket = socket;
        return this;
    }

    public DBConfiguration build() {
        if (dataDir == null || tmpDir == null) {
            this.baseDir = SYSTEM_TEMP_DIR.resolve(path()).resolve("base");
        }
        frozen = true;
        return new DbConfigurationImpl(
                _getPort(),
                _getSocket(),
                _getBinariesClassPathLocation(),
                getBaseDir(),
                getLibDir(),
                _getDataDir(),
                _getTmpDir(),
                isWindows(),
                _getArgs(),
                _getOSLibraryEnvironmentVarName(),
                isSecurityDisabled(),
                isDeletingTemporaryBaseAndDataDirsOnShutdown(),
                this::getURL,
                getDefaultCharacterSet(),
                _getExecutables(),
                getProcessListener());
    }

    /**
     * Whether to "--skip-grant-tables" (defaults to true).
     *
     * @param isSecurityDisabled set isSecurityDisabled value
     * @return returns this
     */
    @SuppressWarnings("UnusedReturnValue")
    public DBConfigurationBuilder setSecurityDisabled(boolean isSecurityDisabled) {
        checkIfFrozen("setSecurityDisabled");
        this.isSecurityDisabled = isSecurityDisabled;
        return this;
    }

    public boolean isSecurityDisabled() {
        return isSecurityDisabled;
    }

    @SuppressWarnings("UnusedReturnValue")
    public DBConfigurationBuilder addArg(String arg) {
        checkIfFrozen("addArg");
        args.add(arg);
        return this;
    }

    protected Path _getDataDir() {
        if (getDataDir() == null
                || getDataDir().equals(SYSTEM_TEMP_DIR.resolve(DEFAULT_DATA_DIR))) {
            return SYSTEM_TEMP_DIR.resolve(DEFAULT_DATA_DIR).resolve(String.valueOf(_getPort()));
        }
        return getDataDir();
    }

    protected Path _getTmpDir() {
        if (getTmpDir() == null || getTmpDir().equals(SYSTEM_TEMP_DIR.resolve(DEFAULT_TMP_DIR))) {
            return SYSTEM_TEMP_DIR.resolve(DEFAULT_TMP_DIR).resolve(String.valueOf(_getPort()));
        }
        return getTmpDir();
    }

    protected int _getPort() {
        if (port == 0) port = detectFreePort();
        return port;
    }

    protected String _getSocket() {
        String resolvedSocket = getSocket();
        if (resolvedSocket == null) {
            String portStr = String.valueOf(getPort());
            // Use /tmp instead getBaseDir() here, else we too easily hit
            // the "mysqld ERROR The socket file path is too long (> 107)" issue
            resolvedSocket = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j." + portStr + ".sock";
        }
        return resolvedSocket;
    }

    public String getDatabaseVersion() {
        return databaseVersion;
    }

    @SuppressWarnings("unused")
    public DBConfigurationBuilder setDatabaseVersion(String databaseVersion) {
        checkIfFrozen("setDatabaseVersion");
        this.databaseVersion = databaseVersion;
        return this;
    }

    protected String _getDatabaseVersion() {
        String resolvedDatabaseVersion = getDatabaseVersion();
        if (resolvedDatabaseVersion == null) {
            if (!OSX.equals(getOS()) && !LINUX.equals(getOS()) && !WINX64.equals(getOS())) {
                throw new IllegalStateException(
                        "OS not directly supported, please use setDatabaseVersion() to set the name "
                                + "of the package that the binaries are in, for: "
                                + SystemUtils.OS_VERSION);
            }
            return "mariadb-11.4.5";
        }
        return resolvedDatabaseVersion;
    }

    protected String getBinariesClassPathLocation() {
        return getClass().getPackage().getName().replace(".", "/")
                + "/"
                + _getDatabaseVersion()
                + "/"
                + getOS();
    }

    @SuppressWarnings("UnusedReturnValue")
    public DBConfigurationBuilder setOS(String osDirectoryName) {
        checkIfFrozen("setOS");
        this.osDirectoryName = osDirectoryName;
        return this;
    }

    public String getOS() {
        return osDirectoryName;
    }

    protected String _getOSLibraryEnvironmentVarName() {
        return switch (Platform.get()) {
            case LINUX -> "LD_LIBRARY_PATH";
            case MAC -> "DYLD_FALLBACK_LIBRARY_PATH";
            case WINDOWS -> "PATH";
        };
    }

    protected String _getBinariesClassPathLocation() {
        if (isUnpackingFromClasspath) {
            return getBinariesClassPathLocation();
        }
        return null; // see ch.vorburger.mariadb4j.DB.unpackEmbeddedDb()
    }

    @SuppressWarnings("unused")
    public boolean isUnpackingFromClasspath() {
        return isUnpackingFromClasspath;
    }

    @SuppressWarnings("UnusedReturnValue")
    public DBConfigurationBuilder setUnpackingFromClasspath(boolean isUnpackingFromClasspath) {
        checkIfFrozen("setUnpackingFromClasspath");
        this.isUnpackingFromClasspath = isUnpackingFromClasspath;
        return this;
    }

    public String getURL(String databaseName) {
        return "jdbc:mariadb://localhost:" + getPort() + "/" + databaseName;
    }

    public List<String> _getArgs() {
        return args;
    }

    @SuppressWarnings("UnusedReturnValue")
    public DBConfigurationBuilder setDefaultCharacterSet(String defaultCharacterSet) {
        checkIfFrozen("setDefaultCharacterSet");
        this.defaultCharacterSet = defaultCharacterSet;
        return this;
    }

    public String getDefaultCharacterSet() {
        return defaultCharacterSet;
    }

    @SuppressWarnings("UnusedReturnValue")
    public DBConfigurationBuilder setExecutable(Executable executable, String path) {
        checkIfFrozen("setExecutable");
        executables.put(
                requireNonNull(executable, "executable"),
                () -> Path.of(requireNonNull(path, "path")));
        return this;
    }

    @SuppressWarnings("unused")
    public DBConfigurationBuilder setExecutable(
            Executable executable, Supplier<Path> pathSupplier) {
        checkIfFrozen("setExecutable");
        executables.put(
                requireNonNull(executable, "executable"),
                requireNonNull(pathSupplier, "pathSupplier"));
        return this;
    }

    protected Map<Executable, Supplier<Path>> _getExecutables() {
        executables.putIfAbsent(
                PRINT_DEFAULTS,
                () -> baseDir.resolve("bin").resolve("my_print_defaults" + getExtension()));

        // See https://github.com/MariaDB4j/MariaDB4j/pull/1126/files#r2019771660
        //   re. why we're keeping mysql*.exe but not packaging mariadb*.exe ...

        executables.putIfAbsent(
                DUMP,
                () ->
                        baseDir.resolve("bin")
                                .resolve(isWindows() ? "mysqldump.exe" : "mariadb-dump"));

        String name = isWindows() ? "mysql" : "mariadb";
        executables.putIfAbsent(
                SERVER, () -> baseDir.resolve("bin").resolve(name + "d" + getExtension()));
        executables.putIfAbsent(
                CLIENT, () -> baseDir.resolve("bin").resolve(name + getExtension()));
        executables.putIfAbsent(
                INSTALL_DB,
                () -> {
                    // It's mysql_install_db.exe (but mariadb-install-db.exe - watch out!) on
                    // Windows...
                    Path bin =
                            baseDir.resolve("bin").resolve("mariadb-install-db" + getExtension());
                    if (Files.exists(bin)) return bin;

                    bin = baseDir.resolve("bin").resolve("mysql_install_db" + getExtension());
                    if (Files.exists(bin)) return bin;

                    bin = baseDir.resolve("scripts").resolve(name + "-install-db" + getExtension());
                    if (Files.exists(bin)) return bin;

                    throw new IllegalStateException("Could not find installDB tool...");
                });

        return executables;
    }

    public Path getExecutable(Executable executable) {
        return _getExecutables().get(executable).get();
    }

    public boolean isWindows() {
        return Platform.OS.WINDOWS.equals(Platform.get());
    }

    public boolean isMacOS() {
        return Platform.OS.MAC.equals(Platform.get());
    }

    protected String getExtension() {
        return isWindows() ? ".exe" : "";
    }
}
