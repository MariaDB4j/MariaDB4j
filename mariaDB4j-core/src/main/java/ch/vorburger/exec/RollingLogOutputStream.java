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

import java.util.Collection;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.exec.LogOutputStream;

/**
 * Rolling Process Output Buffer.
 * 
 * @author Michael Vorburger
 */
// intentionally package local for now
class RollingLogOutputStream extends LogOutputStream {

    private final Collection<String> ringBuffer;

    @SuppressWarnings("unchecked")
    RollingLogOutputStream(int maxLines) {
        ringBuffer = new CircularFifoQueue(maxLines);
    }

    @Override
    protected synchronized void processLine(String line, @SuppressWarnings("unused") int level) {
        ringBuffer.add(line);
    }

    /**
     * Returns recent lines (up to maxLines from constructor).
     * 
     * <p>The implementation is relatively expensive here; the design is intended for many
     * processLine() calls and few getRecentLines().
     * 
     * @return recent Console output
     */
    public synchronized String getRecentLines() {
        StringBuilder sb = new StringBuilder();
        for (String line : ringBuffer) {
            if (sb.length() > 0)
                sb.append('\n');
            sb.append(line);
        }
        return sb.toString();
    }

}
