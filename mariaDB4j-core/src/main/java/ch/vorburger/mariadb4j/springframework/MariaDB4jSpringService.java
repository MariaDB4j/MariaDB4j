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
package ch.vorburger.mariadb4j.springframework;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.MariaDB4jService;

/**
 * MariaDB4jService extension suitable for use in Spring Framework-based applications.
 * 
 * <p>Other than implementing {@link Lifecycle} to get auto-started, this class allows applications
 * using it to programmatically set a default port/socket/data- &amp; base directory in their
 * {@link Configuration}, yet let end-users override those via the Spring Values mariaDB4j.port,
 * mariaDB4j.socket, mariaDB4j.dataDir, mariaDB4j.baseDir; so e.g. via -D or (if using Spring Boot)
 * main() command line arguments.
 *
 * <p>This Service is intentionally NOT annotated as a {@link Service} {@link Component}, because we
 * don't want it to be auto-started by component scan without explicit declaration in a @Configuration
 * (or XML)
 * 
 * @author Michael Vorburger
 */
public class MariaDB4jSpringService extends MariaDB4jService implements Lifecycle {

    public final static String PORT = "mariaDB4j.port";
    public final static String SOCKET = "mariaDB4j.socket";
    public final static String DATA_DIR = "mariaDB4j.dataDir";
    public final static String BASE_DIR = "mariaDB4j.baseDir";
    public final static String LIB_DIR = "mariaDB4j.libDir";
    public final static String UNPACK = "mariaDB4j.unpack";

    protected ManagedProcessException lastException;

    @Value("${" + PORT + ":-1}")
    public void setDefaultPort(int port) {
        if (port != -1)
            getConfiguration().setPort(port);
    }

    @Value("${" + SOCKET + ":NA}")
    public void setDefaultSocket(String socket) {
        if (!"NA".equals(socket))
            getConfiguration().setSocket(socket);
    }

    @Value("${" + DATA_DIR + ":NA}")
    public void setDefaultDataDir(String dataDir) {
        if (!"NA".equals(dataDir))
            getConfiguration().setDataDir(dataDir);
    }

    @Value("${" + BASE_DIR + ":NA}")
    public void setDefaultBaseDir(String baseDir) {
        if (!"NA".equals(baseDir))
            getConfiguration().setBaseDir(baseDir);
    }

    @Value("${" + LIB_DIR + ":NA}")
    public void setDefaultLibDir(String libDir) {
        if (!"NA".equals(libDir))
            getConfiguration().setLibDir(libDir);
    }

    @Value("${" + UNPACK + ":#{null}}")
    public void setDefaultIsUnpackingFromClasspath(Boolean unpack) {
        if (unpack != null)
            getConfiguration().setUnpackingFromClasspath(unpack);
    }

    @Override
    public void start() { // no throws ManagedProcessException
        try {
            super.start();
        } catch (ManagedProcessException e) {
            lastException = e;
            throw new IllegalStateException("MariaDB4jSpringService start() failed", e);
        }
    }

    @Override
    public void stop() { // no throws ManagedProcessException
        try {
            super.stop();
        } catch (ManagedProcessException e) {
            lastException = e;
            throw new IllegalStateException("MariaDB4jSpringService stop() failed", e);
        }
    }

    public ManagedProcessException getLastException() {
        return lastException;
    }

}