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
package ch.vorburger.exec;

import org.apache.commons.exec.LogOutputStream;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * OutputStream which logs to SLF4j.
 * 
 * <p>With many thanks to
 * http://stackoverflow.com/questions/5499042/writing-output-error-to-log-files-using
 * -pumpstreamhandler
 * 
 * @author Michael Vorburger
 */
// intentionally package local
class SLF4jLogOutputStream extends LogOutputStream {

    private final OutputStreamLogDispatcher dispatcher;
    private final Logger logger;
    private final OutputStreamType type;
    private final String pid;

    protected SLF4jLogOutputStream(Logger logger, String pid, OutputStreamType type, OutputStreamLogDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.logger = logger;
        this.type = type;
        this.pid = pid;
    }

    @Override
    protected void processLine(String line, @SuppressWarnings("unused") int level) {
        Level logLevel = dispatcher.dispatch(type, line);
        switch (logLevel) {
            case TRACE:
                logger.trace("{}: {}", pid, line);
                break;

            case DEBUG:
                logger.debug("{}: {}", pid, line);
                break;

            case INFO:
                logger.info("{}: {}", pid, line);
                break;

            case WARN:
                logger.warn("{}: {}", pid, line);
                break;

            case ERROR:
                logger.error("{}: {}", pid, line);
                break;

            default:
                // That's impossible, shut up Checkstyle.
        }
    }

}
