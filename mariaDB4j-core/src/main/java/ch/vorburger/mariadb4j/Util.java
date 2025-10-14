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

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Objects;
import java.util.Set;

/**
 * File utilities.
 *
 * @author Michael Vorburger
 * @author Michael Seaton
 */
public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    private Util() {}

    /**
     * Retrieve the directory located at the given path. Checks that path indeed is a readable
     * directory. If this does not exist, create it (and log having done so).
     *
     * @param dir directory(ies, can include parent directories) names, as forward slash ('/')
     *     separated String
     * @return safe Path object representing that path name
     * @throws java.lang.IllegalArgumentException If it is not a directory, or it is not readable
     */
    public static Path getDirectory(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to create new directory at path: " + dir, e);
        }

        if (dir.toString().trim().isEmpty()) throw new IllegalArgumentException(dir + " is empty");
        if (!Files.isDirectory(dir))
            throw new IllegalArgumentException(dir + " is not a directory");
        if (!Files.isReadable(dir))
            throw new IllegalArgumentException(dir + " is not a readable directory");

        logger.info("Ensured directory exists: {}", dir.toAbsolutePath());
        return dir;
    }

    /**
     * Check for temporary directory name.
     *
     * @param directory directory name
     * @return true if the passed directory name starts with the system temporary directory name.
     */
    public static boolean isTemporaryDirectory(Path directory) {
        if (directory == null) return false;
        Path tmp = Path.of(SystemUtils.JAVA_IO_TMPDIR).toAbsolutePath().normalize();
        Path dir = directory.toAbsolutePath().normalize();
        return dir.startsWith(tmp);
    }

    public static void forceExecutable(Path path) throws IOException {
        if (!Files.exists(path)) {
            logger.info("chmod +x requested on non-existing file: {}", path);
            return;
        }
        if (Files.isExecutable(path)) return;
        if (Files.getFileStore(path).supportsFileAttributeView(PosixFileAttributeView.class)) {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(path, perms);
            return;
        }
        if (!path.toFile().setExecutable(true))
            throw new IOException(
                    "Failed to do chmod +x "
                            + path
                            + " using java.io.File.setExecutable, which will be a problem on *NIX...");
        logger.info("chmod +x {} (using java.io.File.setExecutable fallback)", path);
    }

    /**
     * Extract files from a package on the classpath into a directory.
     *
     * @param packagePath e.g. "com/stuff" (always forward slash not backslash, never dot)
     * @param toDir directory to extract to
     * @return int the number of files copied
     * @throws java.io.IOException if something goes wrong, including if nothing was found on
     *     classpath
     */
    public static int extractFromClasspathToDir(String packagePath, Path toDir) throws IOException {
        String locationPattern = "classpath*:" + packagePath + "/**";
        Resource[] resources =
                new PathMatchingResourcePatternResolver().getResources(locationPattern);
        if (resources.length == 0) throw new IOException("Nothing found at " + locationPattern);
        int counter = 0;
        for (Resource resource : resources) {
            if (!resource.isReadable()) continue;
            URL url = resource.getURL();
            String urlString = url.toString();
            if (urlString.endsWith("/")) continue;
            String relative =
                    urlString.substring(urlString.lastIndexOf(packagePath) + packagePath.length());
            Path target = toDir.resolve(relative.substring(relative.startsWith("/") ? 1 : 0));
            if (Files.exists(target) && Files.size(target) == resource.contentLength()) continue;
            tryN(
                    5,
                    500,
                    () -> {
                        PathUtils.createParentDirectories(target);
                        PathUtils.copy(
                                url::openStream, target, StandardCopyOption.REPLACE_EXISTING);
                    });
            counter++;
        }
        if (counter > 0)
            logger.info("Unpacked {} files from {} to {}", counter, locationPattern, toDir);
        return counter;
    }

    @SuppressWarnings("SameParameterValue")
    private static void tryN(int totalAttempts, long msToWait, Procedure<IOException> procedure)
            throws IOException {
        IOException lastIOException = null;
        for (int attempt = 1; attempt <= totalAttempts; attempt++) {
            try {
                procedure.apply();
                return;
            } catch (IOException ioException) {
                lastIOException = ioException;
                if (attempt == totalAttempts) break;
                logger.warn(
                        "Failure {} of {}, retrying again in {}ms",
                        attempt,
                        totalAttempts,
                        msToWait,
                        ioException);
                try {
                    Thread.sleep(msToWait);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    InterruptedIOException interruptedIOException =
                            new InterruptedIOException("Interrupted while waiting to retry");
                    interruptedIOException.initCause(ie);
                    throw interruptedIOException;
                }
            }
        }
        throw Objects.requireNonNull(lastIOException);
    }

    private interface Procedure<E extends Throwable> {

        void apply() throws E;
    }
}
