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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;

/**
 * Builder for DBConfiguration. Has lot's of sensible default conventions etc.
 */
public class DBConfigurationBuilder {

    protected static final String WIN32 = "win32";
    protected static final String LINUX = "linux";
    protected static final String OSX = "osx";

    private static final String DEFAULT_DATA_DIR = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/data";

    private String databaseVersion = null;

    // all these are just some defaults
    protected String osDirectoryName = SystemUtils.IS_OS_WINDOWS ? WIN32
            : SystemUtils.IS_OS_MAC ? OSX : LINUX;
    protected String baseDir = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/base";
    protected String libDir = null;

    protected String dataDir = DEFAULT_DATA_DIR;
    protected String socket = null; // see _getSocket()
    protected int port = 0;
    protected boolean isDeletingTemporaryBaseAndDataDirsOnShutdown = true;
    protected boolean isUnpackingFromClasspath = true;
    protected List<String> args = new ArrayList<String>();
    private boolean isSecurityDisabled = true;

    private boolean frozen = false;

    public static DBConfigurationBuilder newBuilder() {
        return new DBConfigurationBuilder();
    }

    protected DBConfigurationBuilder() {}

    protected void checkIfFrozen(String setterName) {
        if (frozen)
            throw new IllegalStateException("cannot " + setterName + "() anymore after build()");
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
        if (libDir == null)
            return baseDir + "/libs";
        else
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

    public boolean isDeletingTemporaryBaseAndDataDirsOnShutdown() {
        return isDeletingTemporaryBaseAndDataDirsOnShutdown;
    }

    /**
     * Defines if the configured data and base directories should be deleted on shutdown.
     * If you've set the base and data directories to non temporary directories
     * using {@link #setBaseDir(String)} or {@link #setDataDir(String)},
     * then they'll also never get deleted anyway.
     */
    public DBConfigurationBuilder setDeletingTemporaryBaseAndDataDirsOnShutdown(boolean doDelete) {
        checkIfFrozen("keepsDataAndBaseDir");
        this.isDeletingTemporaryBaseAndDataDirsOnShutdown = doDelete;
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
        return new DBConfiguration.Impl(_getPort(), _getSocket(), _getBinariesClassPathLocation(), getBaseDir(), getLibDir(), _getDataDir(),
                WIN32.equals(getOS()), _getArgs(), _getOSLibraryEnvironmentVarName(), isSecurityDisabled(), 
                isDeletingTemporaryBaseAndDataDirsOnShutdown(), this::getURL);
    }

    /**
     * Whether to to "--skip-grant-tables" (defaults to true).
     */
    public void setSecurityDisabled(boolean isSecurityDisabled) {
        this.isSecurityDisabled = isSecurityDisabled;
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
        if (isNull(getDataDir()) || getDataDir().equals(DEFAULT_DATA_DIR))
            return DEFAULT_DATA_DIR + SystemUtils.FILE_SEPARATOR + getPort();
        else
            return getDataDir();
    }

    protected boolean isNull(String string) {
        if (string == null)
            return true;
        String trim = string.trim();
        if (trim.length() == 0)
            return true;
        if (trim.equalsIgnoreCase("null"))
            return true;
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

    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    protected String _getDatabaseVersion() {
        String databaseVersion = getDatabaseVersion();
        if (databaseVersion == null) {
            if (OSX.equals(getOS()))
                databaseVersion = "mariadb-10.2.11";
            else if (LINUX.equals(getOS()))
                databaseVersion = "mariadb-10.2.11";
            else if (WIN32.equals(getOS()))
                databaseVersion = "mariadb-10.2.11";
            else
                throw new IllegalStateException(
                        "OS not directly supported, please use setDatabaseVersion() to set the name "
                                + "of the package that the binaries are in, for: "
                                + SystemUtils.OS_VERSION);
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

    public void setOS(String osDirectoryName) {
        this.osDirectoryName = osDirectoryName;
    }

    public String getOS() {
        return osDirectoryName;
    }

    protected String _getOSLibraryEnvironmentVarName() {
        return SystemUtils.IS_OS_WINDOWS ? "PATH"
                : SystemUtils.IS_OS_MAC ? "DYLD_FALLBACK_LIBRARY_PATH "
                        : "LD_LIBRARY_PATH";
    }

    protected String _getBinariesClassPathLocation() {
        if (isUnpackingFromClasspath)
            return getBinariesClassPathLocation();
        else
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
        return "jdbc:mysql://localhost:" + this.getPort() + "/" + databaseName;
    }

    public List<String> _getArgs() {
        return args;
    }

}
