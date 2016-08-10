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

/**
 * OutputStream which watches out for the occurrence of a keyword (String).
 * 
 * <p>Used to watch check the console output of a daemon ManagedProcess for some "started up OK" kind
 * of message.
 * 
 * @author Michael Vorburger
 */
// intentionally package local for now
class CheckingConsoleOutputStream extends LogOutputStream {

    private final String watchOutFor;
    private boolean seenIt = false;

    CheckingConsoleOutputStream(String watchOutFor) {
        if (watchOutFor.contains("\n")) {
            throw new IllegalArgumentException("Cannot handle newlines (CR) ...");
        }
        this.watchOutFor = watchOutFor;
    }

    @Override
    protected void processLine(String line, @SuppressWarnings("unused") int level) {
        if (line.contains(watchOutFor)) {
            seenIt = true;
        }
    }

    public boolean hasSeenIt() {
        return seenIt;
    }

}
