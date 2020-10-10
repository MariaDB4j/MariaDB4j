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

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * MariaDB4j starter "Service". This is basically just "sugar" - you can of course also use the DB
 * class directly instead of this convenience utility.
 * 
 * <p>This class does not depend on Spring, and is intended for direct "JavaBean" like usage, and may
 * be useful for DI containers such as Guice. When using Spring, then the MariaDB4jSpringService may
 * be of interest. If you're using Spring Boot, then have a look at the MariaDB4jApplication.
 * 
 * <p>The main() could be used typically from an IDE (waits for CR to shutdown..).
 * 
 * @see MariaDB4jSpringService
 * 
 * @author Michael Vorburger
 */
public class MariaDB4jService {

    protected DB db;
    protected DBConfigurationBuilder configBuilder;

    public DB getDB() {
        if (!isRunning())
            throw new IllegalStateException("start() me up first!");
        return db;
    }

    public DBConfigurationBuilder getConfiguration() {
        if (configBuilder == null)
            configBuilder = DBConfigurationBuilder.newBuilder();
        return configBuilder;
    }

    @PostConstruct // note this is from javax.annotation, not a Spring Framework dependency
    public void postConstruct() throws ManagedProcessRuntimeException {
        try {
            start();
        } catch (ManagedProcessException e) {
            throw new ManagedProcessRuntimeException(e);
        }
    }

    public void start() throws ManagedProcessException {
        if (isRunning())
            return;
        db = DB.newEmbeddedDB(getConfiguration().build());
        db.start();
    }

    @PreDestroy // note this is from javax.annotation, not a Spring Framework dependency
    public void preDestroy() throws ManagedProcessRuntimeException {
        try {
            stop();
        } catch (ManagedProcessException e) {
            throw new ManagedProcessRuntimeException(e);
        }
    }

    public void stop() throws ManagedProcessException {
        if (!isRunning())
            return;
        db.stop();
        db = null;
        configBuilder = null;
    }

    public boolean isRunning() {
        return db != null;
    }

    public static void main(String[] args) throws Exception {
        MariaDB4jService service = new MariaDB4jService();
        service.start();

        waitForKeyPressToCleanlyExit();

        // NOTE: In Eclipse, the MariaDB4j Shutdown Hook is not invoked on
        // exit.. so: (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38016)
        service.stop();
    }

    public static void waitForKeyPressToCleanlyExit() throws IOException {
        // NOTE: In Eclipse, System.console() is not available.. so: (@see
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=122429)
        System.out.println("\n\nHit Enter to quit...");
        BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
        d.readLine();
    }
}