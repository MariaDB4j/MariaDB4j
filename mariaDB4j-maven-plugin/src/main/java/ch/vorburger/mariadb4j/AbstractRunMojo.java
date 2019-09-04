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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;

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
 * @author mike10004
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

    @Parameter
    private String[] args;

    /**
     * if baseDir is set outside java.io.tmpdir, it won't be deleted.
     */
    @Parameter()
    private String baseDir;
    /**
     * if libDir is set outside java.io.tmpdir, it won't be deleted
     */
    @Parameter()
    private String libDir;
    /**
     * if dataDir is set outside java.io.tmpdir, it won't be deleted.
     */
    @Parameter()
    private String dataDir;

    @Parameter(defaultValue = "test")
    protected String databaseName;


    /**
     * scriptCharset set this if you scripts are not UTF-8
     */
    @Parameter(defaultValue = "UTF-8")
    private String scriptCharset;

    /**
     * Path to scripts to run on the database once started
     */
    @Parameter
    private File[] scripts;

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

        if (port != -1 && port != 0) {
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
        if (args != null && args.length != 0) {
            for (String arg : args) {
                configurationBuilder.addArg(arg);
            }
        }
        DBSingleton.setConfigurationBuilder(configurationBuilder);
        return configurationBuilder;
    }

    private Charset getScriptCharset() {
        return Optional.ofNullable(scriptCharset).map(Charset::forName).orElse(StandardCharsets.UTF_8);
    }

    public void runScripts(DB db, String dbName) throws ManagedProcessException, IOException {
        if (this.scripts != null) {
            if (getLog().isInfoEnabled()) {
                getLog().info("Going to run scripts: " + Arrays.asList(this.scripts));
            }
            Charset charset = getScriptCharset();
            for (File scriptFile : this.scripts) {
                //awesome http://www.adam-bien.com/roller/abien/entry/java_8_reading_a_file
                //Though we should have in db to pass a file or inputstream so we don't overload memory. So
                //TODO: add new function to db public void source(File resource, String username, String password, String dbName)
                String scriptText = new String(Files.readAllBytes(scriptFile.toPath()), charset);
                db.run(scriptText, null, null, dbName);

            }
            getLog().info("Successfully run scripts");
        }
    }

    /**
     * Run with the current VM, using the specified arguments.
     * @param configurationBuilder builder of MariaDB4j
     * @throws MojoExecutionException in case of MOJO execution errors
     * @throws MojoFailureException in case of MOJO failures
     */
    protected abstract void runWithMavenJvm(DBConfigurationBuilder configurationBuilder)
            throws MojoExecutionException, MojoFailureException;

    public String getDatabaseName() {
        return databaseName;
    }
}