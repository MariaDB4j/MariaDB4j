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

import ch.vorburger.exec.ClasspathUnpacker;
import ch.vorburger.exec.FileUtils2;
import ch.vorburger.exec.Platform;
import ch.vorburger.exec.UnknownPlatformException;

/**
 * Factory for DB.
 * 
 * Just for Convenience.
 * 
 * @author Michael Vorburger
 */
public abstract class DBFactory {

	protected static final String BUNDLED_DB = "mariadb-5.3.5";
	
	private static final Logger logger = LoggerFactory.getLogger(DBFactory.class);

	/**
	 * You need to only give the path to your data directory here; this
	 * automatically unpacks a MariaDB (or MySQL®) to a temporary basedir.
	 */
	public static DB newEmbeddedDB(File datadir) throws UnknownPlatformException, IOException {
		return new DB(unpackEmbeddedDB(), datadir);
	}
	
	/**
	 * You don't need to give the path to your data directory here, as it
	 * automatically picks a temporary data directory (and it automatically
	 * unpacks a MariaDB (or MySQL®) to a temporary basedir if needed).
	 * 
	 * On startup, a new temporary database is automatically created in the
	 * temporary data directory.  On JVM Exit this data dir is automatically deleted.
	 * 
	 * This is useful e.g. for integration tests.
	 */
	public static DB newEmbeddedTemporaryDB() throws UnknownPlatformException, IOException {
		final File datadir = new File(getTempDirectory(), "mariaDB4j/tempDBs/" + System.nanoTime());
		
		final DB db = newEmbeddedDB(datadir);
		db.setAutoInstallDB(true);
		
		// Simply datadir.deleteOnExit(); doesn't work, this seems to:
		String threadName = "Shutdown Hook Deletion Thread for Temporary DB " + datadir;
		Runtime.getRuntime().addShutdownHook(new Thread(threadName) {
			@Override
		    public void run() {
		    	db.stop();
		    	try {
					FileUtils.deleteDirectory(datadir);
				} catch (IOException e) {
					logger.error("Can't clean-up " + datadir, e);
				}
		    }
		});
		
		return db;
	}

// TODO RAMDisk stuff...
//	/**
//	 * This one tries to, if possible, place the temporary DB datadir on a RAM Disk.
//	 */
//	public static DB newEmbeddedTemporaryDB_onRamDisk() throws UnknownPlatformException, IOException {
//		throw new UnsupportedOperationException();
//	}
	
	// ---
	
	protected static File unpackEmbeddedDB() throws IOException, UnknownPlatformException {
		return unpackEmbeddedDB(BUNDLED_DB);
	}

	protected static File unpackEmbeddedDB(String nameAndVersion) throws IOException, UnknownPlatformException {
		return unpackEmbeddedDB(nameAndVersion, getTemporaryBaseDir());
	}

	protected static File unpackEmbeddedDB(String nameAndVersion, File rootDir) throws IOException {
		String suffix = nameAndVersion + '/' + Platform.is().getCode();
		String packagePath = DB.class.getPackage().getName().replace('.', '/') + '/' + suffix;
		File dir = new File(rootDir, suffix);
		ClasspathUnpacker.extract(packagePath, dir);
		forceAuxiliaryExecutables(dir);
		return dir;
	}

	/**
	 * To chmod +x all Linux scripts.
	 * This is only for things called indirectly (e.g. bin/my_print_defaults),
	 * the ManagedProcess class already does this for executables (e.g. bin/mysql_install_db).
	 * @param dir the directory into which the DB has just been unpacked
	 * @throws IOException
	 */
	protected static void forceAuxiliaryExecutables(File dir) throws IOException {
		FileUtils2.forceExecutable(new File(dir, "bin/my_print_defaults"));
		// Needed because if mysql_install_db (which is a shell script
		// and not an executable on MariaDB v5.3.5 under Linux) is called
		// BEFORE mysqld is executed in DB (which is normal), +x isn't set yet
		FileUtils2.forceExecutable(new File(dir, "bin/mysqld"));
	}

	protected static File getTempDirectory() {
		return FileUtils.getTempDirectory();
	}


	protected static File getTemporaryBaseDir() {
		return new File(getTempDirectory(), "mariaDB4j/base");
	}

}
