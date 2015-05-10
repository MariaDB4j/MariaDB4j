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

    /** Base directory for DB's actual data files. */
    String getDataDir();

    /** Whether running on Windows (some start-up parameters are different). */
    boolean isWindows();

    List<String> getArgs();

    static class Impl implements DBConfiguration {

        private final int port;
        private final String socket;
        private final String binariesClassPathLocation;
        private final String baseDir;
        private final String dataDir;
        private final boolean isWindows;
        private final List<String> args;

        Impl(int port, String socket, String binariesClassPathLocation, String baseDir,
                String dataDir, boolean isWindows, List<String> args) {
            super();
            this.port = port;
            this.socket = socket;
            this.binariesClassPathLocation = binariesClassPathLocation;
            this.baseDir = baseDir;
            this.dataDir = dataDir;
            this.isWindows = isWindows;
            this.args = args;
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
        public String getDataDir() {
            return dataDir;
        }

        @Override
        public boolean isWindows() {
            return isWindows;
        }

        @Override
        public List<String> getArgs() {
            return args;
        }
    }

}
