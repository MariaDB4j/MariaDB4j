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

package ch.vorburger.mariaDB4j;

import ch.vorburger.mariaDB4j.utils.DBSingleton;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

/**
 * Base class to run a MariaDB4j
 * Based on https://raw.githubusercontent.com/spring-projects/spring-boot/master/spring-boot-project/spring-boot-tools/spring-boot-maven-plugin/src/main/java/org/springframework/boot/maven/AbstractRunMojo.java
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author David Liu
 * @author Daniel Young
 * @author Dmytro Nosan
 * @author William Dutton
 * @see StartMojo
 */
public abstract class AbstractRunMojo extends AbstractMojo {

    /**
     * The Maven project.
     * @since 1.0
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter()
    private int port = -1;
    @Parameter()
    private String socket;
    @Parameter()
    private String binariesClassPathLocation;
    @Parameter()
    private String baseDir;
    @Parameter()
    private String libDir;
    @Parameter()
    private String dataDir;

    @Parameter(defaultValue = "test")
    protected String databaseName;

    /**
     * Skip the execution.
     */
    @Parameter(defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.skip) {
            getLog().debug("skipping run as per configuration.");
            return;
        }
        run();
    }

    private void run()
            throws MojoExecutionException, MojoFailureException {
        runWithMavenJvm(resolveConfigurationBuilder());
    }

    private DBConfigurationBuilder resolveConfigurationBuilder() {
        DBConfigurationBuilder configurationBuilder = DBConfigurationBuilder.newBuilder();

        if (port != -1) {
            configurationBuilder.setPort(port);
        }
        if (socket != null) {
            configurationBuilder.setSocket(socket);
        }
        if (baseDir != null) {
            configurationBuilder.setBaseDir(baseDir);
        }
        if (libDir != null) {
            configurationBuilder.setLibDir(libDir);
        }
        if (dataDir != null) {
            configurationBuilder.setDataDir(dataDir);
        }
        DBSingleton.setConfigurationBuilder(configurationBuilder);
        return configurationBuilder;
    }

    /**
     * Run with the current VM, using the specified arguments.
     * @param configurationBuilder builder of MariaDB4j
     * @throws MojoExecutionException in case of MOJO execution errors
     * @throws MojoFailureException in case of MOJO failures
     */
    protected abstract void runWithMavenJvm(DBConfigurationBuilder configurationBuilder)
            throws MojoExecutionException, MojoFailureException;

}