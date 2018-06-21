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
import ch.vorburger.mariadb4j.utils.DBholder;
import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;


/**
 * Stop a MariaDB4j database that has been started by the "start" goal.
 * Typically invoked once a test suite has completed.
 *
 * @author William Dutton
 * @since 1.0.0
 */
@Mojo(name = "stop", requiresProject = true, defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class StopMojo extends AbstractMojo {

    /**
     * The Maven project.
     *
     * @since 1.4.1
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Skip the execution.
     *
     * @since 1.3.2
     */
    @Parameter(property = "mariadb4j.stop.skip", defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.skip) {
            getLog().debug("skipping stop as per configuration.");
            return;
        }
        getLog().info("Stopping application...");
        try {
            stop();
        } catch (IOException ex) {
            // The response won't be received as the server has died - ignoring
            getLog().debug("Service is not reachable anymore (" + ex.getMessage() + ")");
        }
    }

    private void stop() throws IOException, MojoFailureException, MojoExecutionException {
        doStop();
    }

    private void doStop()
            throws IOException, MojoExecutionException {
        try {
            DBholder.getDB().stop();
        } catch (ManagedProcessException ex) {
            throw new MojoExecutionException(
                    "MariaDB4j Database. Could not stop gracefully",
                    ex);
        }
    }

}
