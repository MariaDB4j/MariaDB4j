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

import static org.junit.Assert.assertTrue;

import ch.vorburger.exec.ManagedProcess;
import ch.vorburger.exec.ManagedProcessException;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Simulating starting MariaDB4j on all supported platforms.
 *
 * <p>This detects the recurring issue of some mariaDB startup script not being where it's expected to
 * be and breaking a platform when upgrading the binaries or making code changes.
 *
 * @author Michael Vorburger
 */
public class StartSimulatedForAllPlatformsTest {

    @Test public void simulatedStartWinx64() throws Exception {
        checkPlatformStart(DBConfigurationBuilder.WINX64);
    }

    @Test public void simulatedStartLinux() throws Exception {
        checkPlatformStart(DBConfigurationBuilder.LINUX);
    }

    // Commented out for 10.6.12 binary addition
    // With the decommission of bintray in 2021 and the move of OCI images to GitHub Content Repo
    // as docker images, it was impossible to unpack a version for DBs/.
    // See additional commentary in the PR.
//    @Test public void simulatedStartOSX() throws Exception {
//        checkPlatformStart(DBConfigurationBuilder.OSX);
//    }

    void checkPlatformStart(String platform) throws ManagedProcessException, IOException {
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setOS(platform);
        configBuilder.setBaseDir(configBuilder.getBaseDir() + "/" + platform);
        DBConfiguration config = configBuilder.build();

        DB db = new DB(config);
        db.prepareDirectories();
        db.unpackEmbeddedDb();

        ManagedProcess installProc = db.createDBInstallProcess();
        checkManagedProcessExists(installProc);

        ManagedProcess startProc = db.startPreparation();
        checkManagedProcessExists(startProc);

        // This is super important.. without this, the test is useless,
        // as it will not catch platform specific problems, because the files
        // from previous platform test will still be available
        FileUtils.deleteDirectory(new File(config.getBaseDir()));
    }

    void checkManagedProcessExists(ManagedProcess proc) {
        File installProcFile = proc.getExecutableFile();
        assertTrue("Does not exist: " + installProcFile.toString(), installProcFile.exists());
        assertTrue("Is not a File: " + installProcFile.toString(), installProcFile.isFile());
    }
}
