/*
 * Copyright (c) 2012 Michael Vorburger
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
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

	private String databaseVersion = SystemUtils.IS_OS_MAC ? "mariadb-5.5.34" : "mariadb-5.5.33a";
	
	// all these are just some defaults
	private String baseDir = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/base";
	private String dataDir = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/data";
	private String socket = null; // see _getSocket()
	private int port = 3306;

	private boolean frozen = false;
	
	public static DBConfigurationBuilder newBuilder() {
		return new DBConfigurationBuilder();
	}
	
	protected DBConfigurationBuilder() {
	}

	protected void checkIfFrozen(String setterName) {
		if (frozen)
			throw new IllegalStateException("cannot " + setterName + "() anymore after start()");
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
	 */
	public DBConfigurationBuilder setPort(int port) {
		checkIfFrozen("setPort");
	    if (port == 0) {
	    	detectFreePort();
	    } else {
	    	this.port = port;
	    }
	    return this;
	}

	public void detectFreePort() {
		try {
			ServerSocket ss = new ServerSocket(0);
			setPort(ss.getLocalPort());
			ss.setReuseAddress(true);
			ss.close();
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
		return new DBConfiguration.Impl(getPort(), _getSocket(), getBinariesClassPathLocation(), getBaseDir(), getDataDir());
	}

	protected String _getSocket() {
		String socket = getSocket();
		if (socket == null) {
	    	String portStr = String.valueOf(getPort());
	    	socket = getBaseDir() + "/mysql." + portStr + ".sock";
		}
		return socket;
	}

	protected String getBinariesClassPathLocation() {
		StringBuilder binariesClassPathLocation = new StringBuilder();
		binariesClassPathLocation.append(getClass().getPackage().getName().replace(".", "/"));
		binariesClassPathLocation.append("/").append(databaseVersion).append("/");
		binariesClassPathLocation.append(SystemUtils.IS_OS_WINDOWS ? "win32" : SystemUtils.IS_OS_MAC ? "osx" : "linux");
		return binariesClassPathLocation.toString();
	}

	public String getURL(String databaseName) {
		return "jdbc:mysql://localhost:" + this.getPort() + "/" + databaseName;
	}
	
	// getUID() + getPWD() ?
	
}