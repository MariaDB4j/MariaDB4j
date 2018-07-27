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

import java.util.List;
import java.util.function.Function;

/**
 * Enables passing in custom options when starting up the database server This is the analog to
 * my.cnf
 */
public interface DBConfiguration {

    /** TCP Port to start DB server on. */
    int getPort();

    /** UNIX Socket to start DB server on (ignored on Windows). */
    String getSocket();

    /**
     * Where from on the classpath should the binaries be extracted to the file system.
     *
     * @return null (not empty) if nothing should be extracted.
     */
    String getBinariesClassPathLocation();

    /** Base directory where DB binaries are expected to be found. */
    String getBaseDir();

    String getLibDir();

    /** Base directory for DB's actual data files. */
    String getDataDir();

    /**
     * Whether to delete the base and data directory on shutdown,
     * if it is in a temporary directory. NB: If you've set the
     * base and data directories to non temporary directories,
     * then they'll never get deleted.
     */
    boolean isDeletingTemporaryBaseAndDataDirsOnShutdown();

    /** Whether running on Windows (some start-up parameters are different). */
    boolean isWindows();

    List<String> getArgs();

    String getOSLibraryEnvironmentVarName();

    /** Whether to to "--skip-grant-tables". */
    boolean isSecurityDisabled();

    String getURL(String dbName);

    static class Impl implements DBConfiguration {

        private final int port;
        private final String socket;
        private final String binariesClassPathLocation;
        private final String baseDir;
        private final String libDir;
        private final String dataDir;
        private final boolean isDeletingTemporaryBaseAndDataDirsOnShutdown;
        private final boolean isWindows;
        private final List<String> args;
        private final String osLibraryEnvironmentVarName;
        private final boolean isSecurityDisabled;
        private final Function<String, String> getURL;

        Impl(int port, String socket, String binariesClassPathLocation, String baseDir, String libDir, String dataDir,
             boolean isWindows, List<String> args, String osLibraryEnvironmentVarName, boolean isSecurityDisabled,
             boolean isDeletingTemporaryBaseAndDataDirsOnShutdown, Function<String, String> getURL) {
            super();
            this.port = port;
            this.socket = socket;
            this.binariesClassPathLocation = binariesClassPathLocation;
            this.baseDir = baseDir;
            this.libDir = libDir;
            this.dataDir = dataDir;
            this.isDeletingTemporaryBaseAndDataDirsOnShutdown = isDeletingTemporaryBaseAndDataDirsOnShutdown;
            this.isWindows = isWindows;
            this.args = args;
            this.osLibraryEnvironmentVarName = osLibraryEnvironmentVarName;
            this.isSecurityDisabled = isSecurityDisabled;
            this.getURL = getURL;
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
    }

}
