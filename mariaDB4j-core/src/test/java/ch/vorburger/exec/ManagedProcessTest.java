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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests ManagedProcess.
 * 
 * @author Michael Vorburger
 */
public class ManagedProcessTest {

    @Test
    public void testBasics() throws Exception {
        ManagedProcess p = new ManagedProcessBuilder("someExec").build();
        assertThat(p.isAlive(), is(false));
        try {
            p.destroy();
            Assert.fail("ManagedProcess.destroy() should have thrown a ManagedProcessException here");
        } catch (@SuppressWarnings("unused") ManagedProcessException e) {
            // as expected
        }
        try {
            p.exitValue();
            Assert.fail("ManagedProcess.exitValue() should have thrown a ManagedProcessException here");
        } catch (@SuppressWarnings("unused") ManagedProcessException e) {
            // as expected
        }
        // Commented out because this is a flaky not reliable test, because it's thread scheduling
        // timing dependent :( see long comment inside start() impl. for why this is so
        // try {
        // p.start();
        // Assert.fail("ManagedProcess.start() should have thrown a ManagedProcessException here");
        // } catch (ManagedProcessException e) {
        // // as expected
        // }
        try {
            p.waitForExit();
            Assert.fail("ManagedProcess.waitForExit() should have thrown a ManagedProcessException here");
        } catch (@SuppressWarnings("unused") ManagedProcessException e) {
            // as expected
        }
        try {
            p.waitForExitMaxMs(1234);
            Assert.fail("ManagedProcess.waitForExitMaxMs(1234) should have thrown a ManagedProcessException here");
        } catch (@SuppressWarnings("unused") ManagedProcessException e) {
            // as expected
        }
        try {
            p.waitForExitMaxMsOrDestroy(1234);
            Assert.fail("ManagedProcess.waitForExitMaxMsOrDestroy(1234) should have thrown a ManagedProcessException here");
        } catch (@SuppressWarnings("unused") ManagedProcessException e) {
            // as expected
        }
    }

    @Test(expected = ManagedProcessException.class)
    public void waitForMustFailIfNeverStarted() throws Exception {
        ManagedProcess p = new ManagedProcessBuilder("someExec").build();
        p.waitForExit();
    }

    @Test
    public void testWaitForSeenMessageIfAlreadyTerminated() throws Exception {
        SomeSelfTerminatingExec exec = someSelfTerminatingExec();
        ManagedProcess p = exec.proc;
        // this process should have terminated itself faster than in 1s (1000ms),
        // but this should not cause this to hang, but must return silently:
        p.startAndWaitForConsoleMessageMaxMs(exec.msgToWaitFor, 1000);
    }

    @Test(expected = ManagedProcessException.class)
    public void testWaitForWrongMessageIfAlreadyTerminated() throws Exception {
        ManagedProcess p = someSelfTerminatingExec().proc;
        // this process should have terminated itself faster than in 1s (1000ms),
        // but this should not cause this to hang, but must throw an ManagedProcessException
        p.startAndWaitForConsoleMessageMaxMs("some console message which will never appear", 1000);
    }

    @Test
    public void testSelfTerminatingExec() throws Exception {
        SomeSelfTerminatingExec exec = someSelfTerminatingExec();
        ManagedProcess p = exec.proc;

        assertThat(p.isAlive(), is(false));
        p.startAndWaitForConsoleMessageMaxMs(exec.msgToWaitFor, 1000);
        // can't assertThat(p.isAlive(), is(true)); - if p finishes too fast, this fails -
        // unreliable
        // test :(

        p.waitForExit();
        p.exitValue(); // just making sure it works, don't check, as Win/NIX diff.
        assertThat(p.isAlive(), is(false));

        // It's NOT OK to call destroy() on a process which already terminated
        try {
            p.destroy();
            Assert.fail("Should have thrown an ManagedProcessException");
        } catch (@SuppressWarnings("unused") ManagedProcessException e) {
            // as expected
        }

        String recentConsoleOutput = p.getConsole();
        assertTrue(recentConsoleOutput.length() > 10);
        assertTrue(recentConsoleOutput.contains("\n"));
        System.out.println("Recent (default) 50 lines of console output:");
        System.out.println(recentConsoleOutput);
    }

    static class SomeSelfTerminatingExec {

        ManagedProcess proc;
        String msgToWaitFor;
    }

    protected SomeSelfTerminatingExec someSelfTerminatingExec() throws ManagedProcessException {
        SomeSelfTerminatingExec r = new SomeSelfTerminatingExec();
        if (SystemUtils.IS_OS_WINDOWS) {
            r.proc = new ManagedProcessBuilder("cmd.exe").addArgument("/C").addArgument("dir").addArgument("/X").build();
            r.msgToWaitFor = "bytes free";
        } else if (SystemUtils.IS_OS_SOLARIS) {
            r.proc = new ManagedProcessBuilder("true").addArgument("--version").build();
            r.msgToWaitFor = "true (GNU coreutils)";
        } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC) {
            r.proc = new ManagedProcessBuilder("echo").addArgument("\"Lorem ipsum dolor sit amet, consectetur adipisci elit, "
                    + " sed eiusmod tempor incidunt ut \nlabore et dolore magna aliqua.\"").build();
            r.msgToWaitFor = "incidunt";
        } else {
            throw new ManagedProcessException("Unexpected Platform, improve the test dude...");
        }

        return r;
    }

    @Test
    public void testMustTerminateExec() throws Exception {
        ManagedProcessBuilder pb;
        if (SystemUtils.IS_OS_WINDOWS) {
            pb = new ManagedProcessBuilder("notepad.exe");
        } else {
            pb = new ManagedProcessBuilder("sleep");
            pb.addArgument("30s");
        }

        ManagedProcess p = pb.build();
        assertThat(p.isAlive(), is(false));
        p.start();
        assertThat(p.isAlive(), is(true));
        p.waitForExitMaxMsOrDestroy(200);
        assertThat(p.isAlive(), is(false));
        // can not: p.exitValue();
    }

}
