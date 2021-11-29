/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2016 Michael Vorburger
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

import ch.vorburger.exec.OutputStreamLogDispatcher;
import ch.vorburger.exec.OutputStreamType;
import org.slf4j.event.Level;

/**
 * OutputStreamLogDispatcher for MariaDB. See <a href="https://github.com/vorburger/MariaDB4j/issues/27">issue #27</a>.
 * 
 * @author Michael Vorburger
 */
public class MariaDBOutputStreamLogDispatcher extends OutputStreamLogDispatcher {

    @Override
    public Level dispatch(OutputStreamType type, String line) {
        if (type == OutputStreamType.STDOUT)
            return Level.INFO;
        else { // STDERR
            if (line.contains("ERROR") || line.contains("error"))
                return Level.ERROR;
            else
                return Level.INFO;
        }
    }

}
