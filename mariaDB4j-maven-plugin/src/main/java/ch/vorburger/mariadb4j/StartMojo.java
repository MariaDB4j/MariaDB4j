/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2018 the original author or authors.
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

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.utils.DBSingleton;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.IOException;

/**
 * Start a MariaDBj4 database. Contrary to the {@code run} goal, this does not block and
 * allows other goal to operate on the application. This goal is typically used in
 * integration test scenario where the application is started before a test suite and
 * stopped after.
 *
 * @author William Dutton
 * @since 1.0.0
 * @see StopMojo
 */
@Mojo(name = "start", requiresProject = true,
        defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST,
        requiresDependencyResolution = ResolutionScope.TEST)
public class StartMojo extends AbstractRunMojo {

    @Override
    protected void runWithMavenJvm(DBConfigurationBuilder configurationBuilder) throws MojoExecutionException {
        try {
            DB db = DB.newEmbeddedDB(configurationBuilder.build());
            DBSingleton.setDB(db);
            db.start();


            if (!databaseName.equals("test")) {
                // mysqld out-of-the-box already has a DB named "test"
                // in case we need another DB, here's how to create it first
               db.createDB(databaseName);
            }
            this.runScripts(db, databaseName);

            getLog().warn("Database started and is configured on " + DBSingleton.getConfigurationBuilder().getURL(databaseName));
        } catch (ManagedProcessException ex) {
            throw new MojoExecutionException(
                    "Could not setup, start database", ex);
        } catch (IOException ex) {
            throw new MojoExecutionException(
                    "Could execute scripts after database started", ex);
        }
    }

}
