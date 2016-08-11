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
package ch.vorburger.exec;

import org.slf4j.event.Level;

/**
 * Dispatcher of STDOUT vs STDIN output to slf4j logger levels.
 * 
 * <p>This allows to customize a ManagedProcess' default behavior
 * of sending STDOUT to INFO and STDERR to ERROR.  In particular,
 * this can be used to tune "noisy" processes which output too
 * much stuff to STDERR which isn't really meant for an error
 * log in a Java application, by filtering based on actual
 * output line content (useful e.g. if the process uses it's
 * own text to distinguish log levels in it's output).
 * 
 * @author Michael Vorburger
 */
public class OutputStreamLogDispatcher {

    @SuppressWarnings("unused")
    public Level dispatch(OutputStreamType type, String line) {
        switch (type) {
            case STDOUT:
                return Level.INFO;
            default: // STDERR
                return Level.ERROR;
        }
    }

}
