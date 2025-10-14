/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2025 Michael Vorburger
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
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

record DbConfigurationImpl(
        int port,
        String socket,
        String binariesClassPathLocation,
        Path baseDir,
        Path libDir,
        Path dataDir,
        Path tmpDir,
        boolean isWindows,
        List<String> args,
        String osLibraryEnvironmentVarName,
        boolean isSecurityDisabled,
        boolean isDeletingTemporaryBaseAndDataDirsOnShutdown,
        UnaryOperator<String> getURL,
        String defaultCharacterSet,
        Map<Executable, Supplier<Path>> executables,
        ManagedProcessListener listener)
        implements DBConfiguration {

    public DbConfigurationImpl(
            int port,
            String socket,
            String binariesClassPathLocation,
            Path baseDir,
            Path libDir,
            Path dataDir,
            Path tmpDir,
            boolean isWindows,
            List<String> args,
            String osLibraryEnvironmentVarName,
            boolean isSecurityDisabled,
            boolean isDeletingTemporaryBaseAndDataDirsOnShutdown,
            UnaryOperator<String> getURL,
            String defaultCharacterSet,
            Map<Executable, Supplier<Path>> executables,
            ManagedProcessListener listener) {
        this.port = port;
        this.socket = socket;
        this.binariesClassPathLocation = binariesClassPathLocation;
        this.baseDir = baseDir;
        this.libDir = libDir;
        this.dataDir = dataDir;
        this.tmpDir = tmpDir;
        this.isDeletingTemporaryBaseAndDataDirsOnShutdown =
                isDeletingTemporaryBaseAndDataDirsOnShutdown;
        this.isWindows = isWindows;
        this.args = args;
        this.osLibraryEnvironmentVarName = osLibraryEnvironmentVarName;
        this.isSecurityDisabled = isSecurityDisabled;
        this.getURL = getURL;
        this.defaultCharacterSet = defaultCharacterSet;
        this.listener = listener;
        this.executables = Map.copyOf(executables);
    }

    @Override
    public String getOSLibraryEnvironmentVarName() {
        return osLibraryEnvironmentVarName;
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
    public Path getExecutable(Executable executable) {
        return executables
                .getOrDefault(
                        executable,
                        () -> {
                            throw new IllegalArgumentException(executable.name());
                        })
                .get();
    }
}
