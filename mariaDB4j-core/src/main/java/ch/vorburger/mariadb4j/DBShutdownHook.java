package ch.vorburger.mariadb4j;

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
import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * During shutdown, the classloader doesn't provide access to the majority of the classes.
 * As such, we put all the code required for the shutdown in a single class and don't use any external library code.
 *
 * <p>For the same reason this class implements {@code FileVisitor}.
 * Normally we would use an additional class for that (anonymous or (static) inner), but the classloader doesn't find
 * that class.
 *
 * <p>Everything outside the {@link #run()} method is a partial copy from {@link org.apache.commons.io.FileUtils}
 * implementation to delete files.
 *
 * <p>See https://github.com/vorburger/MariaDB4j/issues/488.
 */
class DBShutdownHook extends Thread implements FileVisitor<Path> {

    private static final Logger logger = LoggerFactory.getLogger(DB.class);

    private final DB db;
    private final Supplier<ManagedProcess> mysqldProcessSupplier;
    private final Supplier<File> dataDirSupplier;
    private final Supplier<File> baseDirSupplier;
    private final DBConfiguration configuration;
    private final LinkOption[] linkOptions = {};

    public DBShutdownHook(String threadName, DB db, Supplier<ManagedProcess> mysqldProcessSupplier, Supplier<File> baseDirSupplier,
            Supplier<File> dataDirSupplier, DBConfiguration configuration) {
        super(threadName);
        this.db = db;
        this.mysqldProcessSupplier = mysqldProcessSupplier;
        this.baseDirSupplier = baseDirSupplier;
        this.dataDirSupplier = dataDirSupplier;
        this.configuration = configuration;
    }

    private boolean deleteQuietly(File file) {
        if (file == null) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }
        } catch (Exception ignore) {
            // Quiet.
        }

        try {
            return file.delete();
        } catch (Exception ignore) {
            return false;
        }
    }

    private void cleanDirectory(File directory) throws IOException {
        File[] files = listFiles(directory, null);
        List<Exception> causeList = new ArrayList<>();
        for (File file : files) {
            try {
                forceDelete(file);
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

    private void forceDelete(File file) throws IOException {
        Objects.requireNonNull(file, "file");

        try {
            delete(file.toPath());
        } catch (IOException exception) {
            throw new IOException("Cannot delete file: " + file, exception);
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

    private Path setReadOnly(Path path, boolean readOnly) throws IOException {
        List<Exception> causeList = new ArrayList<>(2);
        DosFileAttributeView fileAttributeView = Files.getFileAttributeView(path, DosFileAttributeView.class, linkOptions);
        if (fileAttributeView != null) {
            try {
                fileAttributeView.setReadOnly(readOnly);
                return path;
            } catch (IOException exception) {
                causeList.add(exception);
            }
        }

        PosixFileAttributeView posixFileAttributeView = Files.getFileAttributeView(path, PosixFileAttributeView.class, linkOptions);
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
        throw new IOException(String.format("No DosFileAttributeView or PosixFileAttributeView for '%s' (linkOptions=%s)", path,
                Arrays.toString(linkOptions)));
    }

    private File[] listFiles(File directory, FileFilter fileFilter) throws IOException {
        requireDirectoryExists(directory, "directory");
        File[] files = fileFilter == null ? directory.listFiles() : directory.listFiles(fileFilter);
        if (files == null) {
            throw new IOException("Unknown I/O error listing contents of directory: " + directory);
        }
        return files;
    }

    private File requireDirectoryExists(File directory, String name) {
        requireExists(directory, name);
        requireDirectory(directory, name);
        return directory;
    }

    private File requireExists(File file, String fileParamName) {
        Objects.requireNonNull(file, fileParamName);
        if (!file.exists()) {
            throw new IllegalArgumentException("File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
        }
        return file;
    }

    private File requireDirectory(File directory, String name) {
        Objects.requireNonNull(directory, name);
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is not a directory: '" + directory + "'");
        }
        return directory;
    }

    private boolean isEmptyDirectory(Path directory) throws IOException {
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory);
        Throwable throwable = null;

        boolean hasNext;
        try {
            hasNext = !directoryStream.iterator().hasNext();
        } catch (Throwable exception) {
            throwable = exception;
            throw exception;
        } finally {
            if (directoryStream != null) {
                if (throwable != null) {
                    try {
                        directoryStream.close();
                    } catch (Throwable exception) {
                        throwable.addSuppressed(exception);
                    }
                } else {
                    directoryStream.close();
                }
            }

        }

        return hasNext;
    }

    @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
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

    @Override public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        Objects.requireNonNull(file);
        throw exc;
    }

    @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (isEmptyDirectory(dir)) {
            Files.deleteIfExists(dir);
        }
        Objects.requireNonNull(dir);
        if (exc != null) {
            throw exc;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override public void run() {
        ManagedProcess mysqldProcess = mysqldProcessSupplier.get();
        File dataDir = dataDirSupplier.get();
        File baseDir = baseDirSupplier.get();
        // ManagedProcess DestroyOnShutdown ProcessDestroyer does
        // something similar, but it shouldn't hurt to better be save
        // than sorry and do it again ourselves here as well.
        try {
            // Shut up and don't log if it was already stop() before
            if (mysqldProcess != null && mysqldProcess.isAlive()) {
                logger.info("cleanupOnExit() ShutdownHook now stopping database");
                db.stop();
            }
        } catch (ManagedProcessException e) {
            logger.warn("cleanupOnExit() ShutdownHook: An error occurred while stopping the database", e);
        }

        if (dataDir.exists() && configuration.isDeletingTemporaryBaseAndDataDirsOnShutdown()
                && Util.isTemporaryDirectory(dataDir.getAbsolutePath())) {
            logger.info("cleanupOnExit() ShutdownHook quietly deleting temporary DB data directory: " + dataDir);
            deleteQuietly(dataDir);
        }
        if (baseDir.exists() && configuration.isDeletingTemporaryBaseAndDataDirsOnShutdown()
                && Util.isTemporaryDirectory(baseDir.getAbsolutePath())) {
            logger.info("cleanupOnExit() ShutdownHook quietly deleting temporary DB base directory: " + baseDir);
            deleteQuietly(baseDir);
        }
    }
}
