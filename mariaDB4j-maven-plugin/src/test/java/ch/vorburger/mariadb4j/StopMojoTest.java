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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.utils.DBSingleton;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * StopMojoTest mocking database testing function.
 *
 * @author William Dutton
 */
public class StopMojoTest {

    StopMojo stopMojo;

    @Mock
    Log mockLog;

    @Mock
    DB mockDb;

    @Captor
    ArgumentCaptor<String> logCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        DBSingleton.setDB(mockDb);
        stopMojo = new StopMojo();
        stopMojo.setLog(mockLog);
    }

    @Test
    public void stopWithSkipEnabledDoesNotStopDatabase() throws Exception {
        stopMojo.setSkip(true);
        stopMojo.execute();

        verifyNoMoreInteractions(mockDb);
        verify(mockLog, times(1)).debug(logCaptor.capture());
        verifyNoMoreInteractions(mockLog);

        assertEquals("skipping stop as per configuration.", logCaptor.getValue());
    }

    @Test
    public void stopCallsDbSingleton() throws Exception {
        stopMojo.execute();

        verify(mockDb).stop();
        verify(mockLog, times(1)).info(logCaptor.capture());
        verifyNoMoreInteractions(mockLog);

        assertEquals("Stopping MariaDB4j...", logCaptor.getValue());
    }

    @Test
    public void stopCallsDbSingletonAndHandlesManagedProcessException() throws Exception {
        DB mockDb = mock(DB.class);
        DBSingleton.setDB(mockDb);

        Exception exception = new ManagedProcessException("error");
        doThrow(exception).when(mockDb).stop();

        try {
            stopMojo.execute();
            fail("Should have thrown exception");
        } catch (MojoExecutionException e) {
            // expected
            assertTrue(e.getMessage().contains("MariaDB4j Database. Could not stop gracefull"));
        }

        verify(mockDb).stop();
        verify(mockLog, times(1)).info(logCaptor.capture());
        verifyNoMoreInteractions(mockLog);

        List<String> logMessages = logCaptor.getAllValues();

        assertEquals("Stopping MariaDB4j...", logMessages.get(0));
    }
}
