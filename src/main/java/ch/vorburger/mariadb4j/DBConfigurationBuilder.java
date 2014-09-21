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

import org.apache.commons.lang3.SystemUtils;

/**
 * Builder for DBConfiguration.
 * Has lot's of sensible default conventions etc.
 */
public class DBConfigurationBuilder {

    private static final String DEFAULT_DATA_DIR = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/data";

    private String  databaseVersion = null;
	
	// all these are just some defaults
	private String baseDir = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/base";
	private String dataDir = DEFAULT_DATA_DIR;
	private String socket = null; // see _getSocket()
	private int port = 0;

	private boolean frozen = false;
	
	public static DBConfigurationBuilder newBuilder() {
		return new DBConfigurationBuilder();
	}
	
	protected DBConfigurationBuilder() {
	}

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
	 * @param port port number, or 0 to use detectFreePort()
	 * @return this 
	 */
	public DBConfigurationBuilder setPort(int port) {
		checkIfFrozen("setPort");
    	this.port = port;
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
        return new DBConfiguration.Impl(_getPort(), _getSocket(), getBinariesClassPathLocation(), getBaseDir(), _getDataDir());
	}

    protected String _getDataDir() {
        if (isNull(getDataDir()) || getDataDir().equals(DEFAULT_DATA_DIR))
            return DEFAULT_DATA_DIR + SystemUtils.FILE_SEPARATOR + getPort();
        else
            return getDataDir();
    }

    protected boolean isNull(String string) {
        if (string == null) return true;
        String trim = string.trim();
        if (trim.length() == 0) return true;
        if (trim.equalsIgnoreCase("null")) return true;
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
            if (SystemUtils.IS_OS_MAC)
                databaseVersion = "mariadb-5.5.34";
            else if (SystemUtils.IS_OS_LINUX)
                databaseVersion = "mariadb-5.5.33a";
            else if (SystemUtils.IS_OS_WINDOWS)
                databaseVersion = "mariadb-10.0.13";
            else
                throw new IllegalStateException("OS not directly supported, please use setDatabaseVersion() to set the name of the package that the binaries are in, for: " + SystemUtils.OS_VERSION);
        }
        return databaseVersion;
	}

	protected String getBinariesClassPathLocation() {
		StringBuilder binariesClassPathLocation = new StringBuilder();
		binariesClassPathLocation.append(getClass().getPackage().getName().replace(".", "/"));
        binariesClassPathLocation.append("/").append(_getDatabaseVersion()).append("/");
		binariesClassPathLocation.append(SystemUtils.IS_OS_WINDOWS ? "win32" : SystemUtils.IS_OS_MAC ? "osx" : "linux");
		return binariesClassPathLocation.toString();
	}

	public String getURL(String databaseName) {
		return "jdbc:mysql://localhost:" + this.getPort() + "/" + databaseName;
	}
	
	// getUID() + getPWD() ?
	
}