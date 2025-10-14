/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2014 Michael Vorburger
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

import static ch.vorburger.mariadb4j.TestUtil.buildTempDB;
import static ch.vorburger.mariadb4j.TestUtil.configureTempDB;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.vorburger.exec.ManagedProcess;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Simulating starting MariaDB4j on all supported platforms.
 *
 * <p>This detects the recurring issue of some mariaDB startup script not being where it's expected
 * to be and breaking a platform when upgrading the binaries or making code changes.
 *
 * @author Michael Vorburger
 */
class StartSimulatedForAllPlatformsTest {

    @SuppressWarnings("try") // TODO Replace platform with _ when Java 22+
    @Test
    void simulatedStartWin64(@TempDir Path tempDir) throws IOException {
        try (@SuppressWarnings("unused")
                var platform = Platform.simulate(Platform.OS.WINDOWS)) {
            checkPlatformStart(tempDir, DBConfigurationBuilder.WINX64);
        }
    }

    @SuppressWarnings("try") // TODO Replace platform with _ when Java 22+
    @Test
    void simulatedStartLinux(@TempDir Path tempDir) throws IOException {
        try (@SuppressWarnings("unused")
                var platform = Platform.simulate(Platform.OS.LINUX)) {
            checkPlatformStart(tempDir, DBConfigurationBuilder.LINUX);
        }
    }

    @SuppressWarnings("try") // TODO Replace platform with _ when Java 22+
    @Test
    void simulatedStartOSX(@TempDir Path tempDir) throws IOException {
        try (@SuppressWarnings("unused")
                var platform = Platform.simulate(Platform.OS.MAC)) {
            checkPlatformStart(tempDir, DBConfigurationBuilder.OSX);
        }
    }

    void checkPlatformStart(Path tempDir, String platform) throws IOException {
        DBConfigurationBuilder config = configureTempDB(tempDir);
        config.setOS(platform);
        config.setBaseDir(config.getBaseDir().resolve(platform));

        DB db = buildTempDB(tempDir, config, DB::new);
        db.prepareDirectories();
        db.unpackEmbeddedDb();

        ManagedProcess installProc = db.createDBInstallProcess();
        checkManagedProcessExists(installProc);

        ManagedProcess startProc = db.startPreparation();
        checkManagedProcessExists(startProc);
    }

    void checkManagedProcessExists(ManagedProcess proc) {
        File installProcFile = proc.getExecutableFile();
        assertTrue(installProcFile.exists(), "Does not exist: " + installProcFile);
        assertTrue(installProcFile.isFile(), "Is not a File: " + installProcFile);
    }
}
