/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2018 Bojan Vukasovic
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
package ch.vorburger.mariadb4j.springboot.autoconfigure;

import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MariaDBUrl {
    private static final String REGEX = "^jdbc:mariadb://(?<host>[^:/]+)(?::(?<port>[0-9]+))?/(?<db>[^\\\\/?%*:|\"<>.]{0,64})$";
    private static final Pattern pattern = Pattern.compile(REGEX);
    private final Matcher matcher;
    private final boolean matches;

    MariaDBUrl(String url){
        Assert.notNull(url, "spring jdbc cannot be null");
        matcher = pattern.matcher(url);
        matches = matcher.matches();
    }

    String getHost(){
        String host = matches ? matcher.group("host") : null;
        return host == null ? "NA" : host;
    }

    int getPort(){
        String port = matches ? matcher.group("port") : null;
        return port == null ? -1 : Integer.parseInt(port);
    }

    String getDb(){
        return matches ? matcher.group("db") : null;
    }

    String urlWithPort(int port){
        return "jdbc:mariadb://" + getHost() + ":" + port + "/" + getDb();
    }
}
