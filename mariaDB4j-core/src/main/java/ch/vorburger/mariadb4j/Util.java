/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2018 Michael Vorburger
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * File utilities.
 *
 * @author Michael Vorburger
 * @author Michael Seaton
 */
public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    private Util() {
    }

    /**
     * Retrieve the directory located at the given path. Checks that path indeed is a reabable
     * directory. If this does not exist, create it (and log having done so).
     *
     * @param path directory(ies, can include parent directories) names, as forward slash ('/')
     *             separated String
     * @return safe File object representing that path name
     * @throws IllegalArgumentException If it is not a directory, or it is not readable
     */
    public static File getDirectory(String path) {
        boolean log = false;
        File dir = new File(path);
        if (!dir.exists()) {
            log = true;
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to create new directory at path: " + path, e);
            }
        }
        String absPath = dir.getAbsolutePath();
        if (absPath.trim().length() == 0) {
            throw new IllegalArgumentException(path + " is empty");
        }
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(path + " is not a directory");
        }
        if (!dir.canRead()) {
            throw new IllegalArgumentException(path + " is not a readable directory");
        }
        if (log) {
            logger.info("Created directory: " + absPath);
        }
        return dir;
    }

    /**
     * Check for temporary directory name.
     *
     * @param directory directory name
     * @return true if the passed directory name starts with the system temporary directory name.
     */
    public static boolean isTemporaryDirectory(String directory) {
        return directory.startsWith(SystemUtils.JAVA_IO_TMPDIR);
    }

    public static void forceExecutable(File executableFile) throws IOException {
        if (executableFile.exists()) {
            if (!executableFile.canExecute()) {
                boolean succeeded = executableFile.setExecutable(true);
                if (!succeeded) {
                    throw new IOException("Failed to do chmod +x " + executableFile.toString()
                            + " using java.io.File.setExecutable, which will be a problem on *NIX...");
                }
                logger.info("chmod +x {} (using java.io.File.setExecutable)", executableFile);
            }
        } else {
            logger.info("chmod +x requested on non-existing file: {}", executableFile);
        }
    }

    /**
     * Extract files from a package on the classpath into a directory.
     *
     * @param packagePath e.g. "com/stuff" (always forward slash not backslash, never dot)
     * @param toDir       directory to extract to
     * @return int the number of files copied
     * @throws java.io.IOException if something goes wrong, including if nothing was found on
     *                             classpath
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
                final URL url = resource.getURL();
                String path = url.toString();
                if (!path.endsWith("/")) { // Skip directories
                    int p = path.lastIndexOf(packagePath) + packagePath.length();
                    path = path.substring(p);
                    final File targetFile = new File(toDir, path);
                    long len = resource.contentLength();
                    if (!targetFile.exists() || targetFile.length() != len) { // Only copy new files
                        tryN(5, 500, () -> FileUtils.copyURLToFile(url, targetFile));
                        counter++;
                    }
                }
            }
        }
        if (counter > 0) {
            Object[] info = { counter, locationPattern, toDir };
            logger.info("Unpacked {} files from {} to {}", info);
        }
        return counter;
    }

    @SuppressWarnings("null") private static void tryN(int n, long msToWait, Procedure<IOException> procedure) throws IOException {
        IOException lastIOException = null;
        int numAttempts = 0;
        while (numAttempts++ < n) {
            try {
                procedure.apply();
                return;
            } catch (IOException e) {
                lastIOException = e;
                logger.warn("Failure {} of {}, retrying again in {}ms", numAttempts, n, msToWait, e);
                try {
                    Thread.sleep(msToWait);
                } catch (InterruptedException interruptedException) {
                    // Ignore
                }
            }
        }
        throw lastIOException;
    }

    private interface Procedure<E extends Throwable> {

        void apply() throws E;
    }
}
