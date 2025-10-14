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

import java.nio.file.Path;
import java.util.List;

/**
 * Enables passing in custom options when starting up the database server. This is similar to
 * MySQL/MariaDB's my.cnf configuration file.
 *
 * @author Michael Vorburger
 */
public interface DBConfiguration {

    /**
     * TCP Port to start DB server on.
     *
     * @return returns port value
     */
    int port();

    /**
     * UNIX Socket to start DB server on (ignored on Windows).
     *
     * @return returns socket value
     */
    String socket();

    /**
     * Where from on the classpath should the binaries be extracted to the file system.
     *
     * @return null (not empty) if nothing should be extracted.
     */
    String binariesClassPathLocation();

    /**
     * Base directory where DB binaries are expected to be found.
     *
     * @return returns base directory value
     */
    Path baseDir();

    /**
     * Base directory where DB binaries' linked libraries are expected to be found.
     *
     * @return returns lib directory value
     */
    Path libDir();

    /**
     * Base directory for DB's actual data files.
     *
     * @return returns data directory value
     */
    Path dataDir();

    /**
     * Directory for DB's temporary files.
     *
     * @return returns temporary directory value
     */
    Path tmpDir();

    /**
     * Whether to delete the base and data directory on shutdown, if it is in a temporary directory.
     * NB: If you've set the base and data directories to non-temporary directories, then they'll
     * never get deleted.
     *
     * @return returns value of isDeletingTemporaryBaseAndDataDirsOnShutdown
     */
    boolean isDeletingTemporaryBaseAndDataDirsOnShutdown();

    /**
     * Whether running on Windows (some start-up parameters are different).
     *
     * @return returns boolean isWindows
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isWindows();

    List<String> args();

    String getOSLibraryEnvironmentVarName();

    /**
     * Returns an instance of ManagedProcessListener class.
     *
     * @return Process callback when DB process is killed or is completed
     */
    ManagedProcessListener getProcessListener();

    /**
     * Whether to "--skip-grant-tables".
     *
     * @return returns boolean isSecurityDisabled value
     */
    boolean isSecurityDisabled();

    String getURL(String dbName);

    String defaultCharacterSet();

    Path getExecutable(Executable executable);

    enum Executable {
        INSTALL_DB,
        SERVER,
        CLIENT,
        DUMP,
        PRINT_DEFAULTS
    }
}
