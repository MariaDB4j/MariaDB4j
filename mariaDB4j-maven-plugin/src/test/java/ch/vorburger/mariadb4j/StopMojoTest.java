/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2018 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.utils.DBSingleton;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * StopMojoTest mocking database testing function.
 *
 * @author William Dutton
 */
public class StopMojoTest {

    StopMojo stopMojo;
    MockLog mockLog;
    MockDB mockDb;

    @BeforeEach
    public void setUp() {
        mockDb = new MockDB();
        DBSingleton.setDB(mockDb);
        stopMojo = new StopMojo();
        mockLog = new MockLog();
        stopMojo.setLog(mockLog);
    }

    @Test
    public void stopWithSkipEnabledDoesNotStopDatabase() throws Exception {
        stopMojo.setSkip(true);
        stopMojo.execute();

        assertEquals(0, mockDb.stopCallCount);
        assertEquals(1, mockLog.debugMessages.size());
        assertEquals("skipping stop as per configuration.", mockLog.debugMessages.get(0));
        assertEquals(0, mockLog.infoMessages.size());
        assertEquals(0, mockLog.warnMessages.size());
        assertEquals(0, mockLog.errorMessages.size());
    }

    @Test
    public void stopCallsDbSingleton() throws Exception {
        stopMojo.execute();

        assertEquals(1, mockDb.stopCallCount);
        assertEquals(1, mockLog.infoMessages.size());
        assertEquals("Stopping MariaDB4j...", mockLog.infoMessages.get(0));
    }

    @Test
    public void stopCallsDbSingletonAndHandlesManagedProcessException() throws Exception {
        mockDb.throwExceptionOnStop = true;

        try {
            stopMojo.execute();
            fail("Should have thrown exception");
        } catch (MojoExecutionException e) {
            // expected
            assertTrue(e.getMessage().contains("MariaDB4j Database. Could not stop gracefully"));
        }

        assertEquals(1, mockDb.stopCallCount);
        assertEquals(1, mockLog.infoMessages.size());
        assertEquals("Stopping MariaDB4j...", mockLog.infoMessages.get(0));
    }

    private static class MockDB extends DB {
        int stopCallCount = 0;
        boolean throwExceptionOnStop = false;

        MockDB() {
            super(null);
        }

        @Override
        public synchronized void stop() throws ManagedProcessException {
            stopCallCount++;
            if (throwExceptionOnStop) {
                throw new ManagedProcessException("error");
            }
        }
    }

    private static class MockLog implements Log {
        List<String> debugMessages = new ArrayList<>();
        List<String> infoMessages = new ArrayList<>();
        List<String> warnMessages = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        @Override
        public void debug(CharSequence content) {
            debugMessages.add(content.toString());
        }

        @Override
        public void debug(CharSequence content, Throwable error) {}

        @Override
        public void debug(Throwable error) {}

        @Override
        public void info(CharSequence content) {
            infoMessages.add(content.toString());
        }

        @Override
        public void info(CharSequence content, Throwable error) {}

        @Override
        public void info(Throwable error) {}

        @Override
        public void warn(CharSequence content) {
            warnMessages.add(content.toString());
        }

        @Override
        public void warn(CharSequence content, Throwable error) {}

        @Override
        public void warn(Throwable error) {}

        @Override
        public void error(CharSequence content) {
            errorMessages.add(content.toString());
        }

        @Override
        public void error(CharSequence content, Throwable error) {}

        @Override
        public void error(Throwable error) {}

        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public boolean isWarnEnabled() {
            return true;
        }

        @Override
        public boolean isErrorEnabled() {
            return true;
        }
    }
}
