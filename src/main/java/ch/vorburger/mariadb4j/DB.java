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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessBuilder;

/**
 * MariaDB (or MySQL®) Controller.
 * 
 * You need to give the path to a previously unpacked MariaDB (or MySQL®), as
 * well as your data directory, here.
 * 
 * @see EmbeddedDB
 * 
 * @author Michael Vorburger
 */
public class DB {

	private static final Logger logger = LoggerFactory.getLogger(DB.class);

	protected final File basedir;
	protected final File bindir;
	protected final File datadir;
	
	protected final ManagedProcess mysqld;
	protected final ManagedProcess mysql_install;

	protected boolean autoShutdown = true;
	protected boolean autoInstallDB = false; // false by default here, but make it true by default in some subclasses
	protected boolean autoCheck = true;

	// TOOD Use Builder or Factory instead of Constructor? Many more options to come... DBOptions object? 
	public DB(File basedir, File datadir) throws IOException {
		super();
		
		checkExistingReadableDirectory(basedir, "basedir");
		this.basedir = basedir;
		this.bindir = new File(basedir, "bin");
		
		checkNonNull(datadir, "datadir");
		this.datadir = datadir;
		
		mysqld = new ManagedProcessBuilder(cmd("mysqld")).addArgument("--datadir").addArgument(datadir).build();
		mysql_install = new ManagedProcessBuilder(cmd("mysql_install_db")).addArgument("--datadir").addArgument(datadir).build();
	}

	public DB(String basedir, String datadir) throws IOException {
		this(new File(basedir), new File(datadir));
	}

	/**
	 * mysql_install_db.
	 * This DESTROYS the datadir, in case it exists already.
	 */
	public void installDB() throws IllegalStateException, IOException {
		FileUtils.deleteDirectory(datadir);
		FileUtils.forceMkdir(datadir);
		
		mysql_install.start();
		mysql_install.waitForSuccess();
	}

	public void start() throws IOException {
		if (!datadir.exists()) {
			if (autoInstallDB) {
				logger.info("Starting DB and DataDir {} does not exist, so going to creating it as autoInstall is true", datadir);
				installDB();
			}
			else {
				logger.warn("Starting DB and DataDir {} does not exist, this isn't not going to end well... you might want to set autoInstall = true?", datadir);
			}
		}
		
		mysqld.start();
		
		// TODO What follows is typically for launching a "daemon" - refactor to re-use how?
			
		// TODO Wait for an "OK" message instead of waiting
		// mysqld.waitFor("some-OK-message-to-determine");
		// Just "give it a sec"...
		try {
			// 300ms is somewhat arbitrary
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// Ignore
		}
		// ... to see if we immediately terminated?
		if (!mysqld.isAlive()) {
			throw new IOException(mysqld.getConsole() + "Starting DB failed; it already exited with: " + mysqld.exitValue()); // TODO msg
		}

		// TODO ping the port to make sure it's up?
		
		mysqld.setDestroyOnShutdown(autoShutdown);
// destroyOnShutdow will just kill mysqld process; if there is a better way later, use our own shutdown hook  instead: (and mysqld.setDestroyOnShutdown(false)) 
//		if (autoShutdown) {
//			String threadName = "Shutdown Hook Thread for DB " + mysqld.toString();
//			Runtime.getRuntime().addShutdownHook(new Thread(threadName) {
//				@Override
//			    public void run() {
//			    	stopOnShutdown();
//			    }
//			});
//		}
	}

//	private void stopOnShutdown() {
//		stop();
//	}
	
	public void stop() {
		// TODO Can (should?) we do better than just kill the mysqld process?! 
		// There is probably something we can send through SQL, but then we need the driver...
		// How do other folks send SIGTERM rather than the SIGKILL from Java?!?
		// Does it make any difference?
		if (mysqld.isAlive()) {
			mysqld.destroy();
		} else {
			logger.debug("DB is asked to stop(), but actually already isn't running anymore - suspicious or OK?"); 
		}
	}

	public void check() {
		throw new UnsupportedOperationException();
	}

	// ----
	// TODO document port, autoInstall & autoCheck etc. properly...
	
	/**
	 * @throws IllegalStateException
	 *             if not yet started and automatic port search is active
	 * @return TCP/IP port
	 */
	// TODO may be this should be part of a more general new DBOptions class instead?
	public int getPort() throws IllegalStateException {
		// TODO automatically search free TCP/IP port
		throw new UnsupportedOperationException();
	}

	// TODO may be this should be part of a more general new DBOptions class instead?
	public DB setPort(int port) {
		throw new UnsupportedOperationException();
		//return this;
	}

	public boolean isAutoInstallDB() {
		return autoInstallDB;
	}

	public DB setAutoInstallDB(boolean autocreate) {
		this.autoInstallDB = autocreate;
		return this;
	}

	public boolean isAutoCheck() {
		return autoCheck;
	}

	public DB setAutoCheck(boolean autoCheck) {
		this.autoCheck = autoCheck;
		return this;
	}

	public boolean isAutoShutdown() {
		return autoShutdown;
	}

	public void setAutoShutdown(boolean autoShutdown) {
		this.autoShutdown = autoShutdown;
	}

	// ---
	
	protected File cmd(String cmdName) {
		return new File(bindir, cmdName);
	}
	
	protected void checkExistingReadableDirectory(File dir, String name) {
		checkNonNull(dir, name);
		if (!dir.isDirectory())
			throw new IllegalArgumentException(name + " is not a directory: " + dir.toString());
		if (!dir.canRead())
			throw new IllegalArgumentException(name + " can not be read: " + dir.toString());
	}

	protected void checkNonNull(File dir, String name) {
		if (dir == null)
			throw new IllegalArgumentException(name + " == null");
		if (dir.getAbsolutePath().trim().length() == 0)
			throw new IllegalArgumentException(name + " is empty");
	}

}
