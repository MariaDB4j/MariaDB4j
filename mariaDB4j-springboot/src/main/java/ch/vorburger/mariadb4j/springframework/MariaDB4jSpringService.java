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

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfiguration;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * MariaDB4j Lifecyle suitable for use in Spring Framework-based applications.
 *
 * <p>This lets end-users override configuration via the Spring Values mariaDB4j.port,
 * mariaDB4j.socket, mariaDB4j.dataDir, mariaDB4j.baseDir; so e.g. via -D or (if using Spring Boot)
 * main() command line arguments.
 *
 * <p>See <tt>MariaDB4jService</tt> for a similar class which can be used outside of Spring.
 *
 * @author Michael Vorburger
 */
@Configuration
public class MariaDB4jSpringService implements Lifecycle {

    /** Constant <code>PORT="mariaDB4j.port"</code>. */
    public static final String PORT = "mariaDB4j.port";

    /** Constant <code>SOCKET="mariaDB4j.socket"</code>. */
    public static final String SOCKET = "mariaDB4j.socket";

    /** Constant <code>DATA_DIR="mariaDB4j.dataDir"</code>. */
    public static final String DATA_DIR = "mariaDB4j.dataDir";

    /** Constant <code>TMP_DIR="mariaDB4j.tmpDir"</code>. */
    public static final String TMP_DIR = "mariaDB4j.tmpDir";

    /** Constant <code>BASE_DIR="mariaDB4j.baseDir"</code>. */
    public static final String BASE_DIR = "mariaDB4j.baseDir";

    /** Constant <code>LIB_DIR="mariaDB4j.libDir"</code>. */
    public static final String LIB_DIR = "mariaDB4j.libDir";

    /** Constant <code>UNPACK="mariaDB4j.unpack"</code>. */
    public static final String UNPACK = "mariaDB4j.unpack";

    /** Constant <code>OS_USER="mariaDB4j.osUser"</code>. */
    public static final String OS_USER = "mariaDB4j.osUser";

    /** Constant <code>DEFAULT_CHARSET="mariaDB4j.defaultCharset"</code>. */
    public static final String DEFAULT_CHARSET = "mariaDB4j.defaultCharset";

    private final DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();

    private DB db = null;
    private DBConfiguration configuration = null;
    private ManagedProcessException lastException;

    @Value("${" + MariaDB4jSpringService.PORT + ":-1}")
    public void setDefaultPort(int port) {
        if (port != -1) builder.setPort(port);
    }

    @Value("${" + MariaDB4jSpringService.SOCKET + ":NA}")
    public void setDefaultSocket(String socket) {
        if (!"NA".equals(socket)) builder.setSocket(socket);
    }

    @Value("${" + MariaDB4jSpringService.DATA_DIR + ":NA}")
    public void setDefaultDataDir(String dataDir) {
        if (!"NA".equals(dataDir)) builder.setDataDir(dataDir);
    }

    @Value("${" + MariaDB4jSpringService.TMP_DIR + ":NA}")
    public void setDefaultTmpDir(String tmpDir) {
        if (!"NA".equals(tmpDir)) builder.setTmpDir(tmpDir);
    }

    @Value("${" + MariaDB4jSpringService.BASE_DIR + ":NA}")
    public void setDefaultBaseDir(String baseDir) {
        if (!"NA".equals(baseDir)) builder.setBaseDir(baseDir);
    }

    @Value("${" + MariaDB4jSpringService.LIB_DIR + ":NA}")
    public void setDefaultLibDir(String libDir) {
        if (!"NA".equals(libDir)) builder.setLibDir(libDir);
    }

    @Value("${" + MariaDB4jSpringService.UNPACK + ":#{null}}")
    public void setDefaultIsUnpackingFromClasspath(Boolean unpack) {
        if (unpack != null) builder.setUnpackingFromClasspath(unpack);
    }

    @Value("${" + MariaDB4jSpringService.OS_USER + ":NA}")
    public void setDefaultOsUser(String osUser) {
        // TODO Support setDefaultOsUser() directly on Builder
        if (!"NA".equals(osUser)) builder.addArg("--user=" + osUser);
    }

    @Value("${" + MariaDB4jSpringService.DEFAULT_CHARSET + ":NA}")
    public void setDefaultCharacterSet(String charset) {
        if (!Objects.equals(charset, "NA")) builder.setDefaultCharacterSet(charset);
    }

    @Bean
    @ConditionalOnMissingBean
    public DB mariaDB4j() throws ManagedProcessException {
        if (db == null) {
            configuration = builder.build();
            db = DB.newEmbeddedDB(configuration);
            db.start();
        }
        return db;
    }

    /** {@inheritDoc} */
    @Override
    public void start() {
        try {
            mariaDB4j();
        } catch (ManagedProcessException e) {
            lastException = e;
            throw new IllegalStateException("MariaDB4jSpringService start() failed", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void stop() {
        try {
            db.stop();
            db = null;
        } catch (ManagedProcessException e) {
            lastException = e;
            throw new IllegalStateException("MariaDB4jSpringService stop() failed", e);
        }
    }

    @Override
    public boolean isRunning() {
        return db != null;
    }

    public ManagedProcessException getLastException() {
        return lastException;
    }

    public DBConfiguration getConfiguration() {
        if (configuration == null) throw new IllegalStateException("Not yet started!");
        return configuration;
    }
}
