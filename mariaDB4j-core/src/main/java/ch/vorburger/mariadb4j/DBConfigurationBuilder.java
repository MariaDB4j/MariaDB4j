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

import static ch.vorburger.mariadb4j.DBConfiguration.Executable.Client;
import static ch.vorburger.mariadb4j.DBConfiguration.Executable.Dump;
import static ch.vorburger.mariadb4j.DBConfiguration.Executable.InstallDB;
import static ch.vorburger.mariadb4j.DBConfiguration.Executable.PrintDefaults;
import static ch.vorburger.mariadb4j.DBConfiguration.Executable.Server;
import static java.util.Objects.requireNonNull;

import ch.vorburger.exec.ManagedProcessListener;
import ch.vorburger.mariadb4j.DBConfiguration.Executable;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.commons.lang3.SystemUtils;

/**
 * Builder for DBConfiguration. Has lot's of sensible default conventions etc.
 */
public class DBConfigurationBuilder {

    protected static final String WINX64 = "winx64";
    protected static final String LINUX = "linux";
    protected static final String OSX = "osx";

    private static final String DEFAULT_DATA_DIR = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/data";

    private static final String DEFAULT_TMP_DIR = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/tmp";

    private String databaseVersion = null;

    // All of the following are just the defaults, which can be overridden
    protected String osDirectoryName = switch (Platform.get()) {
        case LINUX -> LINUX;
        case MAC -> OSX;
        case WINDOWS -> WINX64;
    };
    protected String baseDir = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/base";
    protected String libDir = null;

    protected String dataDir = DEFAULT_DATA_DIR;
    protected String tmpDir = DEFAULT_TMP_DIR;
    protected String socket = null; // see _getSocket()
    protected int port = 0;
    protected boolean isDeletingTemporaryBaseAndDataDirsOnShutdown = true;
    protected boolean isUnpackingFromClasspath = true;
    protected List<String> args = new ArrayList<>();
    private boolean isSecurityDisabled = true;

    private boolean frozen = false;
    private ManagedProcessListener listener;

    protected String defaultCharacterSet = null;
    protected Map<Executable, Supplier<File>> executables = new HashMap<>();

    public static DBConfigurationBuilder newBuilder() {
        return new DBConfigurationBuilder();
    }

    protected DBConfigurationBuilder() {
    }

    protected void checkIfFrozen(String setterName) {
        if (frozen) {
            throw new IllegalStateException("cannot " + setterName + "() anymore after build()");
        }
    }

    public String getBaseDir() {
        return baseDir;
    }

    public DBConfigurationBuilder setBaseDir(String baseDir) {
        checkIfFrozen("setBaseDir");
        this.baseDir = baseDir;
        return this;
    }

    public String getLibDir() {
        if (libDir == null) {
            return baseDir + "/libs";
        }
        return libDir;
    }

    public DBConfigurationBuilder setLibDir(String libDir) {
        checkIfFrozen("setLibDir");
        this.libDir = libDir;
        return this;
    }

    public String getDataDir() {
        return dataDir;
    }

    public DBConfigurationBuilder setDataDir(String dataDir) {
        checkIfFrozen("setDataDir");
        this.dataDir = dataDir;
        return this;
    }

    public String getTmpDir() {
        return tmpDir;
    }

    public DBConfigurationBuilder setTmpDir(String tmpDir) {
        checkIfFrozen("setTmpDir");
        this.tmpDir = tmpDir;
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
     * Defines if the configured data and base directories should be deleted on
     * shutdown.
     * If you've set the base and data directories to non temporary directories
     * using {@link #setBaseDir(String)} or {@link #setDataDir(String)},
     * then they'll also never get deleted anyway.
     *
     * @param doDelete Default value is true, set false to override
     * @return returns this
     */
    public DBConfigurationBuilder setDeletingTemporaryBaseAndDataDirsOnShutdown(boolean doDelete) {
        checkIfFrozen("keepsDataAndBaseDir");
        isDeletingTemporaryBaseAndDataDirsOnShutdown = doDelete;
        return this;
    }

    protected int detectFreePort() {
        try {
            ServerSocket ss = new ServerSocket(0);
            port = ss.getLocalPort();
            ss.setReuseAddress(true);
            ss.close();
            return port;
        } catch (IOException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }

    public String getSocket() {
        return socket;
    }

    public DBConfigurationBuilder setSocket(String socket) {
        checkIfFrozen("setSocket");
        this.socket = socket;
        return this;
    }

    public DBConfiguration build() {
        frozen = true;
        return new DBConfiguration.Impl(_getPort(), _getSocket(), _getBinariesClassPathLocation(), getBaseDir(),
            getLibDir(), _getDataDir(), _getTmpDir(), isWindows(), _getArgs(), _getOSLibraryEnvironmentVarName(),
            isSecurityDisabled(), isDeletingTemporaryBaseAndDataDirsOnShutdown(), this::getURL,
            getDefaultCharacterSet(), _getExecutables(), getProcessListener());
    }

    /**
     * Whether to to "--skip-grant-tables" (defaults to true).
     *
     * @param isSecurityDisabled set isSecurityDisabled value
     * @return returns this
     */
    public DBConfigurationBuilder setSecurityDisabled(boolean isSecurityDisabled) {
        checkIfFrozen("setSecurityDisabled");
        this.isSecurityDisabled = isSecurityDisabled;
        return this;
    }

    public boolean isSecurityDisabled() {
        return isSecurityDisabled;
    }

    public DBConfigurationBuilder addArg(String arg) {
        checkIfFrozen("addArg");
        args.add(arg);
        return this;
    }

    protected String _getDataDir() {
        if (isNull(getDataDir()) || getDataDir().equals(DEFAULT_DATA_DIR)) {
            return DEFAULT_DATA_DIR + File.separator + getPort();
        }
        return getDataDir();
    }

    protected String _getTmpDir() {
        if (isNull(getTmpDir()) || getTmpDir().equals(DEFAULT_TMP_DIR)) {
            return DEFAULT_TMP_DIR + File.separator + getPort();
        }
        return getTmpDir();
    }

    protected boolean isNull(String string) {
        if (string == null) {
            return true;
        }
        String trim = string.trim();
        if (trim.length() == 0 || "null".equalsIgnoreCase(trim)) {
            return true;
        }
        return false;
    }

    protected int _getPort() {
        int port = getPort();
        if (port == 0) {
            port = detectFreePort();
        }
        return port;
    }

    protected String _getSocket() {
        String socket = getSocket();
        if (socket == null) {
            String portStr = String.valueOf(getPort());
            // Use /tmp instead getBaseDir() here, else we too easily hit
            // the "mysqld ERROR The socket file path is too long (> 107)" issue
            socket = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j." + portStr + ".sock";
        }
        return socket;
    }

    public String getDatabaseVersion() {
        return databaseVersion;
    }

    public DBConfigurationBuilder setDatabaseVersion(String databaseVersion) {
        checkIfFrozen("setDatabaseVersion");
        this.databaseVersion = databaseVersion;
        return this;
    }

    protected String _getDatabaseVersion() {
        String databaseVersion = getDatabaseVersion();
        if (databaseVersion == null) {
            if (!OSX.equals(getOS()) && !LINUX.equals(getOS()) && !WINX64.equals(getOS())) {
                throw new IllegalStateException(
                    "OS not directly supported, please use setDatabaseVersion() to set the name "
                        + "of the package that the binaries are in, for: " + SystemUtils.OS_VERSION);
            }
            return "mariadb-11.4.5";
        }
        return databaseVersion;
    }

    protected String getBinariesClassPathLocation() {
        StringBuilder binariesClassPathLocation = new StringBuilder();
        binariesClassPathLocation.append(getClass().getPackage().getName().replace(".", "/"));
        binariesClassPathLocation.append("/").append(_getDatabaseVersion()).append("/");
        binariesClassPathLocation.append(getOS());
        return binariesClassPathLocation.toString();
    }

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

    public boolean isUnpackingFromClasspath() {
        return isUnpackingFromClasspath;
    }

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

    public DBConfigurationBuilder setDefaultCharacterSet(String defaultCharacterSet) {
        checkIfFrozen("setDefaultCharacterSet");
        this.defaultCharacterSet = defaultCharacterSet;
        return this;
    }

    public String getDefaultCharacterSet() {
        return defaultCharacterSet;
    }

    public DBConfigurationBuilder setExecutable(Executable executable, String path) {
        checkIfFrozen("setExecutable");
        executables.put(requireNonNull(executable, "executable"), () -> new File(requireNonNull(path, "path")));
        return this;
    }

    public DBConfigurationBuilder setExecutable(Executable executable, Supplier<File> pathSupplier) {
        checkIfFrozen("setExecutable");
        executables.put(requireNonNull(executable, "executable"), requireNonNull(pathSupplier, "pathSupplier"));
        return this;
    }

    protected Map<Executable, Supplier<File>> _getExecutables() {
        executables.putIfAbsent(PrintDefaults, () -> new File(baseDir, "bin/my_print_defaults" + getExtension()));

        // See https://github.com/MariaDB4j/MariaDB4j/pull/1126/files#r2019771660
        //   re. why we're keeping mysql*.exe but not packaging mariadb*.exe ...

        executables.putIfAbsent(Dump, () -> isWindows()
            ? new File(baseDir, "bin/mysqldump.exe")
            : new File(baseDir, "bin/mariadb-dump"));

        String name = isWindows() ? "mysql" : "mariadb";
        executables.putIfAbsent(Server, () -> new File(baseDir, "bin/" + name + "d" + getExtension()));
        executables.putIfAbsent(Client, () -> new File(baseDir, "bin/" + name + getExtension()));
        executables.putIfAbsent(InstallDB, () -> {
            // It's mysql_install_db.exe (but mariadb-install-db.exe - watch out!) on Windows...
            File bin = new File(baseDir, "bin/mariadb-install-db" + getExtension());
            if (bin.exists())
                return bin;

            bin = new File(baseDir, "bin/mysql_install_db" + getExtension());
            if (bin.exists())
                return bin;

            bin = new File(baseDir, "scripts/" + name + "-install-db" + getExtension());
            if (bin.exists())
                return bin;

            throw new IllegalStateException("Could not find installDB tool...");
        });

        return executables;
    }

    public File getExecutable(Executable executable) {
        return _getExecutables().get(executable).get();
    }

    public boolean isWindows() {
        return Platform.get().equals(Platform.OS.WINDOWS);
    }

    public boolean isMacOS() {
        return Platform.get().equals(Platform.OS.MAC);
    }

    protected String getExtension() {
        return isWindows() ? ".exe" : "";
    }
}
