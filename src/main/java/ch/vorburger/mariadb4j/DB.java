/*
 * Copyright (c) 2012-2014 Michael Vorburger
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessBuilder;
import ch.vorburger.exec.ManagedProcessException;

/**
 * Provides capability to install, start, and use an embedded database.
 * @author Michael Vorburger
 * @author Michael Seaton
 */
public class DB {
	private static final Logger logger = LoggerFactory.getLogger(DB.class);

	protected final DBConfiguration config;

	private File baseDir;
	private File dataDir;
	private ManagedProcess mysqldProcess;

	protected DB(DBConfiguration config) {
		this.config = config;
	}

	/**
	 * This factory method is the mechanism for constructing a new embedded database for use
	 * This method automatically installs the database and prepares it for use
	 * @param config Configuration of the embedded instance
	 * @return a new DB instance
	 */
	public static DB newEmbeddedDB(DBConfiguration config) throws ManagedProcessException {
		DB db = new DB(config);
		db.prepareDirectories();
		db.unpackEmbeddedDb();
		db.install();
		return db;
	}

	/**
	 * This factory method is the mechanism for constructing a new embedded database for use
	 * This method automatically installs the database and prepares it for use with default configuration,
	 * allowing only for specifying port
	 * @param port the port to start the embedded database on
	 * @return a new DB instance
	 */
	public static DB newEmbeddedDB(int port) throws ManagedProcessException {
		DBConfigurationBuilder config = new DBConfigurationBuilder();
		config.setPort(port);
		config.setSocket(config.getBaseDir() + "/mysql" + port + ".sock");
		return newEmbeddedDB(config.build());
	}

	/**
	 * Installs the database to the location specified in the configuration
	 */
	protected void install() throws ManagedProcessException {
		logger.info("Installing a new embedded database to: " + baseDir);
		try {
			ManagedProcessBuilder builder = new ManagedProcessBuilder(baseDir.getAbsolutePath() + "/bin/mysql_install_db");
			builder.addFileArgument("--datadir", dataDir).setWorkingDirectory(baseDir);
			if (!SystemUtils.IS_OS_WINDOWS) {
				builder.addFileArgument("--basedir", baseDir);
				builder.addArgument("--no-defaults");
				builder.addArgument("--force");
				builder.addArgument("--skip-name-resolve");
				builder.addArgument("--verbose");
			}
			ManagedProcess mysqlInstallProcess = builder.build();
			mysqlInstallProcess.start();
			mysqlInstallProcess.waitForExit();
		}
		catch (Exception e) {
			throw new ManagedProcessException("An error occurred while installing the database", e);
		}
		logger.info("Installation complete.");
	}

	/**
	 * Starts up the database, using the data directory and port specified in the configuration
	 */
	public void start() throws ManagedProcessException {
		logger.info("Starting up the database...");
		try {
			ManagedProcessBuilder builder = new ManagedProcessBuilder(baseDir.getAbsolutePath() + "/bin/mysqld");
			builder.addArgument("--no-defaults");  // *** THIS MUST COME FIRST ***
			builder.addArgument("--console");
			builder.addArgument("--skip-grant-tables");
			builder.addArgument("--max_allowed_packet=64M");
			builder.addFileArgument("--basedir", baseDir).setWorkingDirectory(baseDir);
			builder.addFileArgument("--datadir", dataDir);
			builder.addArgument("--port=" + config.getPort());
			if (!SystemUtils.IS_OS_WINDOWS) {
				builder.addArgument("--socket=" + config.getSocket());
			}
            logger.info("mysqld executable: " + builder.getExecutable());
			mysqldProcess = builder.build();
			mysqldProcess.start();
			mysqldProcess.waitForConsoleMessage("mysqld: ready for connections.");
			mysqldProcess.setDestroyOnShutdown(true);
			cleanupOnExit();
		}
		catch (Exception e) {
            logger.error("failed to start mysqld", e);
			throw new ManagedProcessException("An error occurred while starting the database", e);
		}
		logger.info("Database startup complete.");
	}

	/**
	 * @return a new Connection to this database
	 * @throws SQLException if any errors occur getting the connection
	 */
	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://localhost:"+config.getPort() + "/test", "root", "");
	}

	public void source(String resource) throws ManagedProcessException {
		source(resource, null, null, null);
	}
	
	/**
	 * Takes in a string that represents a resource on the classpath and sources it via mysql
	 * @param resource the resource to source
	 */
	public void source(String resource, String username, String password, String dbName) throws ManagedProcessException {
		logger.info("Sourcing a script located at: " + resource);
		try {
			InputStream from = getClass().getClassLoader().getResourceAsStream(resource);

			ManagedProcessBuilder builder = new ManagedProcessBuilder(new File(baseDir, "bin/mysql"));
			builder.setWorkingDirectory(baseDir);
			if (username != null)
				builder.addArgument("-u" + username);
			if (password != null)
				builder.addArgument("-p" + password);
			if (dbName != null)
				builder.addArgument("-D" + dbName);
			builder.addArgument("--socket=" + config.getSocket());
			builder.setInputStream(from);
			ManagedProcess process = builder.build();
			process.start();
			process.waitForExit();
		}
		catch (Exception e) {
			throw new ManagedProcessException("An error occurred while sourcing a file", e);
		}
		logger.info("File sourcing successful.");
	}

	/**
	 * Stops the database
	 */
	public void stop() throws ManagedProcessException {
		logger.info("Stopping the database...");
		if (mysqldProcess.isAlive()) {
			mysqldProcess.destroy();
			logger.info("Database stopped.");
		}
		else {
			logger.info("Database was already stopped.");
		}
	}

	/**
	 * Based on the current OS, unpacks the appropriate version of MariaDB to the
	 * file system based on the configuration
	 */
	protected void unpackEmbeddedDb() {
		if (config.getBinariesClassPathLocation() == null) {
			logger.info("Not unpacking any embedded database (as BinariesClassPathLocation configuration is null)");
			return;
		}
		
		logger.info("Unpacking the embedded database...");
		try {
			Util.extractFromClasspathToFile(config.getBinariesClassPathLocation(), baseDir);
			if (!SystemUtils.IS_OS_WINDOWS) {
				Util.forceExecutable(new File(baseDir, "bin/my_print_defaults"));
				Util.forceExecutable(new File(baseDir, "bin/mysql_install_db"));
				Util.forceExecutable(new File(baseDir, "bin/mysqld"));
				Util.forceExecutable(new File(baseDir, "bin/mysql"));
			}
		}
		catch (IOException e) {
			throw new RuntimeException("Error unpacking embedded db", e);
		}
		logger.info("Database successfully unpacked to " + baseDir.getAbsolutePath());
	}

	/**
	 * If the data directory specified in the configuration is a temporary directory,
	 * this deletes any previous version.  It also makes sure that the directory exists.
	 */
	protected void prepareDirectories() throws ManagedProcessException {
		logger.info("Preparing base directory...");
		baseDir = Util.getDirectory(config.getBaseDir() + SystemUtils.FILE_SEPARATOR + config.getPort());
		logger.info("Base directory prepared.");

		logger.info("Preparing data directory...");
		try {
			String dataDirPath = config.getDataDir() + SystemUtils.FILE_SEPARATOR + config.getPort();
			if (Util.isTemporaryDirectory(dataDirPath)) {
				FileUtils.deleteDirectory(new File(dataDirPath));
			}
			dataDir = Util.getDirectory(dataDirPath);
			logger.info("Data directory prepared.");
		}
		catch (Exception e) {
			throw new ManagedProcessException("An error occurred while preparing the data directory", e);
		}
	}

	/**
	 * Adds a shutdown hook to ensure that when the JVM exits, the database is stopped, and any
	 * temporary data directories are cleaned up.
	 */
	protected void cleanupOnExit() {
		String threadName = "Shutdown Hook Deletion Thread for Temporary DB " + config.getDataDir();
		final DB db = this;
		Runtime.getRuntime().addShutdownHook(new Thread(threadName) {
			@Override
			public void run() {
				try {
					db.stop();
				}
				catch (ManagedProcessException e) {
					logger.info("An error occurred while stopping the database", e);
				}
				try {
					if (Util.isTemporaryDirectory(dataDir.getAbsolutePath())) {
						FileUtils.deleteDirectory(dataDir);
					}
					if (Util.isTemporaryDirectory(baseDir.getAbsolutePath())) {
						FileUtils.deleteDirectory(baseDir);
					}
				}
				catch (IOException e) {
					logger.info("An error occurred while deleting the data directory", e);
				}
			}
		});
	}
}
