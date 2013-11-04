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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * File utilities
 * @author Michael Vorburger
 * @author Michael Seaton
 */
public class Util {

	private static final Logger logger = LoggerFactory.getLogger(Util.class);

	/**
	 * Retrieve the directory located at the given path.
	 * If this does not exist, create it
	 * If it is not a directory, or it is not readable, throw an Exception
	 */
	public static File getDirectory(String path) {
		File dir = new File(path);
		if (!dir.exists()) {
			try {
				FileUtils.forceMkdir(dir);
			}
			catch (IOException e) {
				throw new IllegalArgumentException("Unable to create new directory at path: " + path, e);
			}
		}
		if (dir.getAbsolutePath().trim().length() == 0) {
			throw new IllegalArgumentException(path + " is empty");
		}
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException(path + " is not a directory");
		}
		if (!dir.canRead()) {
			throw new IllegalArgumentException(path + " is not a readable directory");
		}
		return dir;
	}

	/**
	 * @return true if the passed directory is within the system temporary directory
	 */
	public static boolean isTemporaryDirectory(String directory) {
		return directory.startsWith(SystemUtils.JAVA_IO_TMPDIR);
	}

	public static void forceExecutable(File executableFile) throws IOException {
		if (executableFile.exists() && !executableFile.canExecute()) {
			boolean succeeded = executableFile.setExecutable(true);
			if (succeeded) {
				logger.info("chmod +x " + executableFile.toString() + " (using java.io.File.setExecutable)");
			}
			else {
				throw new IOException("Failed to do chmod +x " + executableFile.toString() + " using java.io.File.setExecutable, which will be a problem on *NIX...");
			}
		}
	}

	/**
	 * Extract files from a package on the classpath into a directory.
	 * @param packagePath e.g. "com/stuff" (always forward slash not backslash, never dot)
	 * @param toDir directory to extract to
	 * @return int the number of files copied
	 * @throws java.io.IOException if something goes wrong, including if nothing was found on classpath
	 */
	public static int extractFromClasspathToFile(String packagePath, File toDir) throws IOException {
		String locationPattern = "classpath*:" + packagePath + "/**";
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resourcePatternResolver.getResources(locationPattern);
		if (resources.length == 0) {
			throw new IOException("Nothing found at " + locationPattern);
		}
		int counter = 0;
		for (Resource resource : resources) {
			if (resource.isReadable()) { // Skip hidden or system files
				URL url = resource.getURL();
				String path = url.toString();
				if (!path.endsWith("/")) { // Skip directories
					int p = path.lastIndexOf(packagePath) + packagePath.length();
					path = path.substring(p);
					File targetFile = new File(toDir, path);
					long len = resource.contentLength();
					if (!targetFile.exists() || targetFile.length() != len) { // Only copy new files
						FileUtils.copyURLToFile(url, targetFile);
						counter++;
					}
				}
			}
		}
		logger.info("Unpacked {} files from {} to {}", new Object[] { counter, locationPattern, toDir });
		return counter;
	}
}
