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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.vorburger.exec.CommandBuilder;

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
	protected final File datadir;
	
	//protected final CommandBuilder mysqld = null; // new CommandBuilder().setExecutable("mysqld");
	protected final ProcessBuilder mysqld;

	// false by default here, but make it true by default in some subclasses
	protected boolean autoInstall = false;
	protected boolean autoCheck = true;

	public DB(File basedir, File datadir) {
		super();
		checkExistingReadableDirectory(basedir, "basedir");
		this.basedir = basedir;
		checkNonNull(datadir, "datadir");
		this.datadir = datadir;
		
		mysqld = new ProcessBuilder("mysqld", "--datadir", datadir.getAbsolutePath()).directory(basedir);
	}

	public DB(String basedir, String datadir) {
		this(new File(basedir), new File(datadir));
	}

	protected void checkExistingReadableDirectory(File dir, String name) {
		checkNonNull(dir, name);
		if (!dir.isDirectory())
			throw new IllegalArgumentException(name + " is not a directory");
		if (!dir.canRead())
			throw new IllegalArgumentException(name + " can not be read");
	}

	private void checkNonNull(File dir, String name) {
		if (dir == null)
			throw new IllegalArgumentException(name + " == null");
		if (dir.getAbsolutePath().trim().length() == 0)
			throw new IllegalArgumentException(name + " is empty");
	}

	public void installDB() {
		throw new UnsupportedOperationException();
	}

	public void check() {
		throw new UnsupportedOperationException();
	}

	public void start() {
		if (!datadir.exists()) {
			if (autoInstall) {
				logger.info("Starting DB and DataDir {} does not exist, so going to creating it as autoInstall is true", datadir);
				installDB();
			}
			else {
				logger.warn("Starting DB and DataDir {} does not exist, this isn't not going to end well... you might want to set autoInstall = true?", datadir);
			}
		}
		throw new UnsupportedOperationException();
	}

	public void stop() {
		throw new UnsupportedOperationException();
	}

	// TODO document port, autoInstall & autoCheck etc. properly...
	
	/**
	 * @throws IllegalStateException
	 *             if not yet started and automatic port search is active
	 * @return TCP/IP port
	 */
	public int getPort() throws IllegalStateException {
		// TODO automatically search free TCP/IP port
		throw new UnsupportedOperationException();
	}

	public void setPort(int port) {
		throw new UnsupportedOperationException();
	}

	public boolean isAutoInstall() {
		return autoInstall;
	}

	public void setAutoInstall(boolean autocreate) {
		this.autoInstall = autocreate;
	}

	public boolean isAutoCheck() {
		return autoCheck;
	}

	public void setAutoCheck(boolean autoCheck) {
		this.autoCheck = autoCheck;
	}
}
