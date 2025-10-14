/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2021 Michael Vorburger
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

import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessException;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * During shutdown, the classloader doesn't provide access to the majority of the classes. As such,
 * we put all the code required for the shutdown in a single class and don't use any external
 * library code.
 *
 * <p>For the same reason this class implements {@code FileVisitor}. Normally we would use an
 * additional class for that (anonymous or (static) inner), but the classloader doesn't find that
 * class.
 *
 * <p>Everything outside the {@link #run()} method is a partial copy from {@link
 * org.apache.commons.io.FileUtils} implementation to delete files.
 *
 * <p>See <a href="https://github.com/MariaDB4j/MariaDB4j/issues/488">Issue #488</a>.
 */
class DBShutdownHook extends Thread implements FileVisitor<Path> {

    private static final Logger logger = LoggerFactory.getLogger(DBShutdownHook.class);

    private final DB db;
    private final Supplier<ManagedProcess> mysqldProcessSupplier;
    private final Supplier<Path> dataDirSupplier;
    private final Supplier<Path> baseDirSupplier;
    private final Supplier<Path> tmpDirSupplier;
    private final DBConfiguration configuration;
    private final LinkOption[] linkOptions = {};

    /**
     * Constructor.
     *
     * @param threadName a {@link java.lang.String} object
     * @param db a {@link ch.vorburger.mariadb4j.DB} object
     * @param mysqldProcessSupplier a {@link java.util.function.Supplier} object
     * @param baseDirSupplier a {@link java.util.function.Supplier} object
     * @param tmpDirSupplier a {@link java.util.function.Supplier} object
     * @param dataDirSupplier a {@link java.util.function.Supplier} object
     * @param configuration a {@link ch.vorburger.mariadb4j.DBConfiguration} object
     */
    public DBShutdownHook(
            String threadName,
            DB db,
            Supplier<ManagedProcess> mysqldProcessSupplier,
            Supplier<Path> baseDirSupplier,
            Supplier<Path> tmpDirSupplier,
            Supplier<Path> dataDirSupplier,
            DBConfiguration configuration) {
        super(threadName);
        this.db = db;
        this.mysqldProcessSupplier = mysqldProcessSupplier;
        this.baseDirSupplier = baseDirSupplier;
        this.dataDirSupplier = dataDirSupplier;
        this.tmpDirSupplier = tmpDirSupplier;
        this.configuration = configuration;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean deleteQuietly(Path path) {
        if (path == null) return false;
        try {
            if (Files.isDirectory(path)) cleanDirectory(path);
        } catch (Exception ignore) {
            // Quiet.
        }

        try {
            Files.delete(path);
            return true;
        } catch (IOException ignore) {
            return false;
        }
    }

    private void cleanDirectory(Path directory) throws IOException {
        Path[] paths = listFiles(directory, null);
        List<Exception> causeList = new ArrayList<>();
        for (Path path : paths) {
            try {
                forceDelete(path);
            } catch (IOException exception) {
                causeList.add(exception);
            }
        }

        if (!causeList.isEmpty()) {
            IOException ioException = new IOException(directory.toString());
            causeList.forEach(ioException::addSuppressed);
            throw ioException;
        }
    }

    private void forceDelete(Path path) throws IOException {
        Objects.requireNonNull(path, "path");

        try {
            delete(path);
        } catch (IOException exception) {
            throw new IOException("Cannot delete file: " + path, exception);
        }
    }

    private void delete(Path path) throws IOException {
        if (Files.isDirectory(path, linkOptions)) {
            deleteDirectory(path);
        } else {
            deleteFile(path);
        }
    }

    private void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, this);
    }

    private void deleteFile(Path file) throws IOException {
        if (Files.isDirectory(file, linkOptions)) {
            throw new NoSuchFileException(file.toString());
        }
        boolean exists = Files.exists(file, linkOptions);
        if (exists) {
            setReadOnly(file, false);
        }

        Files.deleteIfExists(file);
    }

    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    private Path setReadOnly(Path path, boolean readOnly) throws IOException {
        List<Exception> causeList = new ArrayList<>(2);
        DosFileAttributeView fileAttributeView =
                Files.getFileAttributeView(path, DosFileAttributeView.class, linkOptions);
        if (fileAttributeView != null) {
            try {
                fileAttributeView.setReadOnly(readOnly);
                return path;
            } catch (IOException exception) {
                causeList.add(exception);
            }
        }

        PosixFileAttributeView posixFileAttributeView =
                Files.getFileAttributeView(path, PosixFileAttributeView.class, linkOptions);
        if (posixFileAttributeView != null) {
            PosixFileAttributes readAttributes = posixFileAttributeView.readAttributes();
            Set<PosixFilePermission> permissions = readAttributes.permissions();
            permissions.remove(PosixFilePermission.OWNER_WRITE);
            permissions.remove(PosixFilePermission.GROUP_WRITE);
            permissions.remove(PosixFilePermission.OTHERS_WRITE);

            try {
                return Files.setPosixFilePermissions(path, permissions);
            } catch (IOException exception) {
                causeList.add(exception);
            }
        }

        if (!causeList.isEmpty()) {
            IOException ioException = new IOException(path.toString());
            causeList.forEach(ioException::addSuppressed);
            throw ioException;
        }
        throw new IOException(
                String.format(
                        "No DosFileAttributeView or PosixFileAttributeView for '%s' (linkOptions=%s)",
                        path, Arrays.toString(linkOptions)));
    }

    @SuppressWarnings("SameParameterValue")
    private Path[] listFiles(Path directory, DirectoryStream.Filter<Path> filter)
            throws IOException {
        requireDirectoryExists(directory, "directory");
        try (DirectoryStream<Path> stream =
                (filter == null)
                        ? Files.newDirectoryStream(directory)
                        : Files.newDirectoryStream(directory, filter)) {
            List<Path> files = new ArrayList<>();
            for (Path path : stream) {
                files.add(path);
            }
            return files.toArray(Path[]::new);
        } catch (IOException e) {
            throw new IOException(
                    "Unknown I/O error listing contents of directory: " + directory, e);
        }
    }

    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    private Path requireDirectoryExists(Path directory, String name) {
        requireExists(directory, name);
        requireDirectory(directory, name);
        return directory;
    }

    @SuppressWarnings("UnusedReturnValue")
    private Path requireExists(Path path, String fileParamName) {
        Objects.requireNonNull(path, fileParamName);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(
                    "File system element for parameter '"
                            + fileParamName
                            + "' does not exist: '"
                            + path
                            + "'");
        }
        return path;
    }

    @SuppressWarnings("UnusedReturnValue")
    private Path requireDirectory(Path directory, String name) {
        Objects.requireNonNull(directory, name);
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException(
                    "Parameter '" + name + "' is not a directory: '" + directory + "'");
        }
        return directory;
    }

    private boolean isEmptyDirectory(Path directory) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            return !directoryStream.iterator().hasNext();
        }
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public FileVisitResult preVisitDirectory(Path dir, @NonNull BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public FileVisitResult visitFile(Path file, @NonNull BasicFileAttributes attrs)
            throws IOException {
        if (Files.exists(file, linkOptions)) {
            setReadOnly(file, false);
            Files.deleteIfExists(file);
        }

        if (Files.isSymbolicLink(file)) {
            try {
                Files.delete(file);
            } catch (NoSuchFileException ignore) {
                // Ignore.
            }
        }
        return FileVisitResult.CONTINUE;
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        Objects.requireNonNull(file);
        throw exc;
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (isEmptyDirectory(dir)) {
            Files.deleteIfExists(dir);
        }
        Objects.requireNonNull(dir);
        if (exc != null) {
            throw exc;
        }
        return FileVisitResult.CONTINUE;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        ManagedProcess mysqldProcess = mysqldProcessSupplier.get();
        // ManagedProcess DestroyOnShutdown ProcessDestroyer does
        // something similar, but it shouldn't hurt to better be safe
        // than sorry and do it again ourselves here as well.
        try {
            // Shut up and don't log if it was already stop() before
            if (mysqldProcess != null && mysqldProcess.isAlive()) {
                logger.info("cleanupOnExit() ShutdownHook now stopping database");
                db.stop();
            }
        } catch (ManagedProcessException e) {
            logger.warn(
                    "cleanupOnExit() ShutdownHook: An error occurred while stopping the database",
                    e);
        }
        if (configuration.isDeletingTemporaryBaseAndDataDirsOnShutdown()) {
            Path dataDir = dataDirSupplier.get();
            if (Files.exists(dataDir) && Util.isTemporaryDirectory(dataDir.toAbsolutePath())) {
                logger.info(
                        "cleanupOnExit() ShutdownHook quietly deleting temporary DB data directory: {}",
                        dataDir);
                deleteQuietly(dataDir);
            }
            Path baseDir = baseDirSupplier.get();
            if (Files.exists(baseDir) && Util.isTemporaryDirectory(baseDir.toAbsolutePath())) {
                logger.info(
                        "cleanupOnExit() ShutdownHook quietly deleting temporary DB base directory: {}",
                        baseDir);
                deleteQuietly(baseDir);
            }
        }
        Path tmpDir = tmpDirSupplier.get();
        if (Files.exists(tmpDir) && Util.isTemporaryDirectory(tmpDir.toAbsolutePath())) {
            logger.info(
                    "cleanupOnExit() ShutdownHook quietly deleting temporary DB tmp directory: {}",
                    tmpDir);
            deleteQuietly(tmpDir);
        }
    }
}
