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
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = DBFactory.PREFIX)
public class DBFactory {
    public final static String PREFIX = "mariadb4j";
    public final static String PORT = "port";
    public final static String SOCKET = "socket";
    public final static String DATA_DIR = "dataDir";
    public final static String TMP_DIR = "tmpDir";
    public final static String BASE_DIR = "baseDir";
    public final static String LIB_DIR = "libDir";
    public final static String UNPACK = "unpack";
    public final static String OS_USER = "osUser";
    public final static String DEFAULT_CHARSET = "defaultCharset";

    private final DBConfigurationBuilder builder = DBConfigurationBuilder.newBuilder();

    private DB db = null;

    @Value("${" + DBFactory.PORT + ":-1}")
    public void setDefaultPort(int port) {
        if (port != -1)
            builder.setPort(port);
    }

    @Value("${" + DBFactory.SOCKET + ":NA}")
    public void setDefaultSocket(String socket) {
        if (!"NA".equals(socket))
            builder.setSocket(socket);
    }

    @Value("${" + DBFactory.DATA_DIR + ":NA}")
    public void setDefaultDataDir(String dataDir) {
        if (!"NA".equals(dataDir))
            builder.setDataDir(dataDir);
    }

    @Value("${" + DBFactory.TMP_DIR + ":NA}")
    public void setDefaultTmpDir(String tmpDir) {
        if (!"NA".equals(tmpDir))
            builder.setTmpDir(tmpDir);
    }

    @Value("${" + DBFactory.BASE_DIR + ":NA}")
    public void setDefaultBaseDir(String baseDir) {
        if (!"NA".equals(baseDir))
            builder.setBaseDir(baseDir);
    }

    @Value("${" + DBFactory.LIB_DIR + ":NA}")
    public void setDefaultLibDir(String libDir) {
        if (!"NA".equals(libDir))
            builder.setLibDir(libDir);
    }

    @Value("${" + DBFactory.UNPACK + ":#{null}}")
    public void setDefaultIsUnpackingFromClasspath(Boolean unpack) {
        if (unpack != null)
            builder.setUnpackingFromClasspath(unpack);
    }

    @Value("${" + DBFactory.OS_USER + ":NA}")
    public void setDefaultOsUser(String osUser) {
        if (!"NA".equals(osUser))
            builder.addArg("--user=" + osUser);
    }

    @Value("${" + DBFactory.DEFAULT_CHARSET + ":NA}")
    public void setDefaultCharacterSet(String charset) {
        if (!Objects.equals(charset, "NA"))
            builder.setDefaultCharacterSet(charset);
    }

    @Bean
    @ConditionalOnMissingBean
    public DB getDB() throws ManagedProcessException {
        if (db == null) {
            db = DB.newEmbeddedDB(builder.build());
            db.start();
        }
        return db;
    }
}
