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
 * Enables passing in custom options when starting up the database server
 * This is the analog to my.cnf
 */
public class Configuration {
	public static final String DB_PORT_PROPERTY = "mariadb4j.port";

	private String databaseVersion = SystemUtils.IS_OS_MAC ? "mariadb-5.5.34" : "mariadb-5.5.33a";
	private String baseDir = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/base";
	private String dataDir = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/data";
	private String socket = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/mysql.sock";

	private int port = 3306;

	public Configuration() {
		System.setProperty(DB_PORT_PROPERTY, String.valueOf(port));
	}

	public String getDatabaseVersion() {
		return databaseVersion;
	}

	public void setDatabaseVersion(String databaseVersion) {
		this.databaseVersion = databaseVersion;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public String getDataDir() {
		return dataDir;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
	    if (port == 0) {
	    	detectFreePort();
	    } else {
	    	this.port = port;
	    	String portStr = String.valueOf(port);
	    	socket = SystemUtils.JAVA_IO_TMPDIR + "/MariaDB4j/mysql." + portStr + ".sock";
	    	System.setProperty(DB_PORT_PROPERTY, portStr);
	    }
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

	public void setSocket(String socket) {
		this.socket = socket;
	}
}
