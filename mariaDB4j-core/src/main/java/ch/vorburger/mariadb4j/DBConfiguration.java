/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2014 Michael Vorburger
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

import ch.vorburger.exec.ManagedProcessListener;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Enables passing in custom options when starting up the database server.
 * This is similar to MySQL/MariaDB's my.cnf configuration file.
 *
 * @author Michael Vorburger
 */
public interface DBConfiguration {

    /**
     * TCP Port to start DB server on.
     *
     * @return returns port value
     **/
    int getPort();

    /**
     * UNIX Socket to start DB server on (ignored on Windows).
     *
     * @return returns socket value
     **/
    String getSocket();

    /**
     * Where from on the classpath should the binaries be extracted to the file system.
     *
     * @return null (not empty) if nothing should be extracted.
     */
    String getBinariesClassPathLocation();

    /**
     * Base directory where DB binaries are expected to be found.
     *
     * @return returns base directory value
     **/
    String getBaseDir();

    String getLibDir();

    /**
     * Base directory for DB's actual data files.
     *
     * @return returns data directory value
     **/
    String getDataDir();

    /**
     * Directory for DB's temporary files.
     *
     * @return returns temporary directory value
     **/
    String getTmpDir();

    /**
     * Whether to delete the base and data directory on shutdown,
     * if it is in a temporary directory. NB: If you've set the
     * base and data directories to non temporary directories,
     * then they'll never get deleted.
     *
     * @return returns value of isDeletingTemporaryBaseAndDataDirsOnShutdown
     */
    boolean isDeletingTemporaryBaseAndDataDirsOnShutdown();

    /**
     * Whether running on Windows (some start-up parameters are different).
     *
     * @return returns boolean isWindows
     **/
    boolean isWindows();

    List<String> getArgs();

    String getOSLibraryEnvironmentVarName();

    /**
     * Returns an instance of ManagedProcessListener class.
     *
     * @return Process callback when DB process is killed or is completed
     */
    ManagedProcessListener getProcessListener();

    /**
     * Whether to to "--skip-grant-tables".
     *
     * @return returns boolean isSecurityDisabled value
     **/
    boolean isSecurityDisabled();

    String getURL(String dbName);

    String getDefaultCharacterSet();

    File getExecutable(Executable executable);

    enum Executable {
        InstallDB, Server, Client, Dump, PrintDefaults
    }

    class Impl implements DBConfiguration {

        private final int port;
        private final String socket;
        private final String binariesClassPathLocation;
        private final String baseDir;
        private final String libDir;
        private final String dataDir;
        private final String tmpDir;
        private final boolean isDeletingTemporaryBaseAndDataDirsOnShutdown;
        private final boolean isWindows;
        private final List<String> args;
        private final String osLibraryEnvironmentVarName;
        private final String defaultCharacterSet;
        private final ManagedProcessListener listener;
        private final boolean isSecurityDisabled;
        private final Function<String, String> getURL;
        private final Map<Executable, Supplier<File>> executables;

        Impl(int port, String socket, String binariesClassPathLocation, String baseDir, String libDir, String dataDir,
                String tmpDir,
                boolean isWindows, List<String> args, String osLibraryEnvironmentVarName, boolean isSecurityDisabled,
                boolean isDeletingTemporaryBaseAndDataDirsOnShutdown, Function<String, String> getURL,
                String defaultCharacterSet,
                Map<Executable, Supplier<File>> executables, ManagedProcessListener listener) {
            this.port = port;
            this.socket = socket;
            this.binariesClassPathLocation = binariesClassPathLocation;
            this.baseDir = baseDir;
            this.libDir = libDir;
            this.dataDir = dataDir;
            this.tmpDir = tmpDir;
            this.isDeletingTemporaryBaseAndDataDirsOnShutdown = isDeletingTemporaryBaseAndDataDirsOnShutdown;
            this.isWindows = isWindows;
            this.args = args;
            this.osLibraryEnvironmentVarName = osLibraryEnvironmentVarName;
            this.isSecurityDisabled = isSecurityDisabled;
            this.getURL = getURL;
            this.defaultCharacterSet = defaultCharacterSet;
            this.listener = listener;
            this.executables = executables;
        }

        @Override
        public int getPort() {
            return port;
        }

        @Override
        public String getSocket() {
            return socket;
        }

        @Override
        public String getBinariesClassPathLocation() {
            return binariesClassPathLocation;
        }

        @Override
        public String getBaseDir() {
            return baseDir;
        }

        @Override
        public String getLibDir() {
            return libDir;
        }

        @Override
        public String getDataDir() {
            return dataDir;
        }

        @Override
        public String getTmpDir() {
            return tmpDir;
        }

        @Override
        public boolean isDeletingTemporaryBaseAndDataDirsOnShutdown() {
            return isDeletingTemporaryBaseAndDataDirsOnShutdown;
        }

        @Override
        public boolean isWindows() {
            return isWindows;
        }

        @Override
        public List<String> getArgs() {
            return args;
        }

        @Override
        public String getOSLibraryEnvironmentVarName() {
            return osLibraryEnvironmentVarName;
        }

        @Override
        public boolean isSecurityDisabled() {
            return isSecurityDisabled;
        }

        @Override
        public String getURL(String dbName) {
            return getURL.apply(dbName);
        }

        @Override
        public ManagedProcessListener getProcessListener() {
            return listener;
        }

        @Override
        public String getDefaultCharacterSet() {
            return defaultCharacterSet;
        }

        @Override
        public File getExecutable(Executable executable) {
            return executables.getOrDefault(executable, () -> {
                throw new IllegalArgumentException(executable.name());
            }).get();
        }
    }
}
