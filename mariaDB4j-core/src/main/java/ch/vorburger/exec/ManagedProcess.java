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

import static ch.vorburger.exec.OutputStreamType.STDERR;
import static ch.vorburger.exec.OutputStreamType.STDOUT;

import java.io.*;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.ProcessDestroyer;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.vorburger.mariadb4j.Util;

/**
 * Managed OS Process (Executable, Program, Command). Created by
 * {@link ManagedProcessBuilder#build()}.
 * 
 * <p>Intended for controlling external "tools", often "daemons", which produce some text-based control
 * output. In this form not yet suitable for programs returning binary data via stdout (but could be
 * extended).
 *
 * <p>Does reasonably extensive logging about what it's doing (contrary to Apache Commons Exec),
 * including logging the processes stdout &amp; stderr, into SLF4J (not the System.out.Console).
 *
 * @see Executor Internally based on http://commons.apache.org/exec/ but intentionally not exposing
 *      this; could be switched later, if there is any need.
 * 
 * @author Michael Vorburger
 */
public class ManagedProcess {

    private static final Logger logger = LoggerFactory.getLogger(ManagedProcess.class);
    private static final int INVALID_EXITVALUE = Executor.INVALID_EXITVALUE;

    private final CommandLine commandLine;
    private final Executor executor = new DefaultExecutor();
    private final DefaultExecuteResultHandler resultHandler = new LoggingExecuteResultHandler();
    private final ExecuteWatchdog watchDog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
    private final ProcessDestroyer shutdownHookProcessDestroyer = new LoggingShutdownHookProcessDestroyer();
    private final Map<String, String> environment;
    private final InputStream input;
    private final boolean destroyOnShutdown;
    private final int consoleBufferMaxLines;
    private final OutputStreamLogDispatcher outputStreamLogDispatcher;

    private boolean isAlive = false;
    private String procShortName;
    private RollingLogOutputStream console;
    private MultiOutputStream stdouts;
    private MultiOutputStream stderrs;

    /**
     * Package local constructor.
     * 
     * <p>Keep ch.vorburger.exec's API separate from Apache Commons Exec, so it COULD be replaced.
     * 
     * @see ManagedProcessBuilder#build()
     * 
     * @param commandLine Apache Commons Exec CommandLine
     * @param directory Working directory, or null
     * @param environment Environment Variable.
     */
    ManagedProcess(CommandLine commandLine, File directory, Map<String, String> environment,
            InputStream input, boolean destroyOnShutdown, int consoleBufferMaxLines, OutputStreamLogDispatcher outputStreamLogDispatcher) {
        this.commandLine = commandLine;
        this.environment = environment;
        if (input != null) {
            this.input = buffer(input);
        } else {
            this.input = null; // this is safe/OK/expected; PumpStreamHandler constructor handles
                               // this as
                               // expected
        }
        if (directory != null) {
            executor.setWorkingDirectory(directory);
        }
        executor.setWatchdog(watchDog);
        this.destroyOnShutdown = destroyOnShutdown;
        this.consoleBufferMaxLines = consoleBufferMaxLines;
        this.outputStreamLogDispatcher = outputStreamLogDispatcher;
        stdouts = new MultiOutputStream();
        stderrs = new MultiOutputStream();
    }

    /**
     * Package local constructor.
     *
     * <p>Keep ch.vorburger.exec's API separate from Apache Commons Exec, so it COULD be replaced.
     *
     * @see ManagedProcessBuilder#build()
     *
     * @param commandLine Apache Commons Exec CommandLine
     * @param directory Working directory, or null
     * @param environment Environment Variable.
     */
    ManagedProcess(CommandLine commandLine, File directory, Map<String, String> environment,
                   InputStream input, boolean destroyOnShutdown, int consoleBufferMaxLines,
                   OutputStreamLogDispatcher outputStreamLogDispatcher, List<OutputStream> stdOuts,
                   List<OutputStream> stderr) {
        this(commandLine, directory, environment, input, destroyOnShutdown, consoleBufferMaxLines,
                outputStreamLogDispatcher);

        for (OutputStream stdOut : stdOuts) {
            stdouts.addOutputStream(stdOut);
        }

        for (OutputStream stde : stderr) {
            stderrs.addOutputStream(stde);
        }
    }

    // stolen from commons-io IOUtiles (@since v2.5)
    protected BufferedInputStream buffer(final InputStream inputStream) {
        // reject null early on rather than waiting for IO operation to fail
        if (inputStream == null) { // not checked by BufferedInputStream
            throw new NullPointerException("inputStream == null");
        }
        return inputStream instanceof BufferedInputStream ? (BufferedInputStream) inputStream
                : new BufferedInputStream(inputStream);
    }

    /**
     * Starts the Process.
     * 
     * <p>This method always immediately returns (i.e. launches the process asynchronously). Use the
     * different waitFor... methods if you want to "block" on the spawned process.
     * 
     * @throws ManagedProcessException if the process could not be started
     */
    public synchronized void start() throws ManagedProcessException {
        startPreparation();
        startExecute();
    }

    protected synchronized void startPreparation() throws ManagedProcessException {
        if (isAlive()) {
            throw new ManagedProcessException(
                    procLongName()
                            + " is still running, use another ManagedProcess instance to launch another one");
        }
        if (logger.isInfoEnabled())
            logger.info("Starting {}", procLongName());


        PumpStreamHandler outputHandler = new PumpStreamHandler(stdouts, stderrs, input);
        executor.setStreamHandler(outputHandler);

        String pid = procShortName();
        stdouts.addOutputStream(new SLF4jLogOutputStream(logger, pid, STDOUT, outputStreamLogDispatcher));
        stderrs.addOutputStream(new SLF4jLogOutputStream(logger, pid, STDERR, outputStreamLogDispatcher));

        if (consoleBufferMaxLines > 0) {
            console = new RollingLogOutputStream(consoleBufferMaxLines);
            stdouts.addOutputStream(console);
            stderrs.addOutputStream(console);
        }

        if (destroyOnShutdown) {
            executor.setProcessDestroyer(shutdownHookProcessDestroyer);
        }

        if (commandLine.isFile()) {
            try {
                Util.forceExecutable(getExecutableFile());
            } catch (Exception e) {
                throw new ManagedProcessException("Unable to make command executable", e);
            }
        } else {
            logger.debug(commandLine.getExecutable()
                    + " is not a java.io.File, so it won't be made executable (which MAY be a problem on *NIX, but not for sure)");
        }
    }

    public File getExecutableFile() {
        return new File(commandLine.getExecutable());
    }

    protected synchronized void startExecute() throws ManagedProcessException {
        try {
            executor.execute(commandLine, environment, resultHandler);
        } catch (IOException e) {
            throw new ManagedProcessException("Launch failed: " + commandLine, e);
        }
        isAlive = true;

        // We now must give the system a say 100ms chance to run the background
        // thread now, otherwise the resultHandler in checkResult() won't work.
        //
        // This is admittedly not ideal, but to do better would require significant
        // changes to DefaultExecutor, so that its execute() would "fail fast" and
        // throw an Exception immediately if process start-up fails by doing the
        // launch in the current thread, and then spawns a separate thread only
        // for the waitFor().
        //
        // As DefaultExecutor doesn't seem to have been written with extensibility
        // in mind, and rewriting it to start gain 100ms (at the start of every process..)
        // doesn't seem to be worth it for now, I'll leave it like this, for now.
        //
        try {
            this.wait(100); // better than Thread.sleep(100); -- thank you, FindBugs
        } catch (InterruptedException e) {
            throw handleInterruptedException(e);
        }
        checkResult();
    }

    /**
     * Starts the Process and waits (blocks) until the process prints a certain message.
     * 
     * <p>You should be sure that the process either prints this message at some point, or otherwise
     * exits on it's own. This method will otherwise be slow, but never block forever, as it will
     * "give up" and always return after max. maxWaitUntilReturning ms.
     * 
     * @param messageInConsole text to wait for in the STDOUT/STDERR of the external process
     * @param maxWaitUntilReturning maximum time to wait, in milliseconds, until returning, if
     *            message wasn't seen
     * @return true if message was seen in console; false if message didn't occur and we're
     *         returning due to max. wait timeout
     * @throws ManagedProcessException for problems such as if the process already exited (without
     *             the message ever appearing in the Console)
     */
    public boolean startAndWaitForConsoleMessageMaxMs(String messageInConsole,
            long maxWaitUntilReturning) throws ManagedProcessException {
        startPreparation();

        CheckingConsoleOutputStream checkingConsoleOutputStream = new CheckingConsoleOutputStream(
                messageInConsole);
        if (stdouts != null && stderrs != null) {
            stdouts.addOutputStream(checkingConsoleOutputStream);
            stderrs.addOutputStream(checkingConsoleOutputStream);
        }

        long timeAlreadyWaited = 0;
        final int SLEEP_TIME_MS = 50;
        logger.info(
                "Thread will wait for \"{}\" to appear in Console output of process {} for max. "
                        + maxWaitUntilReturning + "ms", messageInConsole, procLongName());

        startExecute();

        try {
            while (!checkingConsoleOutputStream.hasSeenIt() && isAlive()) {
                try {
                    Thread.sleep(SLEEP_TIME_MS);
                } catch (InterruptedException e) {
                    throw handleInterruptedException(e);
                }
                timeAlreadyWaited += SLEEP_TIME_MS;
                if (timeAlreadyWaited > maxWaitUntilReturning) {
                    logger.warn("Timed out waiting for \"\"{}\"\" after {}ms (returning false)",
                            messageInConsole, maxWaitUntilReturning);
                    return false;
                }
            }

            // If we got out of the while() loop due to !isAlive() instead of messageInConsole, then
            // throw
            // the same exception as above!
            if (!checkingConsoleOutputStream.hasSeenIt()) {
                throw new ManagedProcessException(getUnexpectedExitMsg(messageInConsole));
            } else {
                return true;
            }
        } finally {
            if (stdouts != null && stderrs != null) {
                stdouts.removeOutputStream(checkingConsoleOutputStream);
                stderrs.removeOutputStream(checkingConsoleOutputStream);
            }
        }
    }

    protected String getUnexpectedExitMsg(String messageInConsole) {
        return "Asked to wait for \"" + messageInConsole + "\" from " + procLongName()
                + ", but it already exited! (without that message in console)"
                + getLastConsoleLines();
    }

    protected ManagedProcessException handleInterruptedException(InterruptedException e)
            throws ManagedProcessException {
        // TODO Not sure how to best handle this... opinions welcome (see also below)
        final String message = "Huh?! InterruptedException should normally never happen here..."
                + procLongName();
        logger.error(message, e);
        return new ManagedProcessException(message, e);
    }

    protected void checkResult() throws ManagedProcessException {
        if (resultHandler.hasResult()) {
            // We already terminated (or never started)
            ExecuteException e = resultHandler.getException();
            if (e != null) {
                logger.error(procLongName() + " failed");
                throw new ManagedProcessException(procLongName() + " failed, exitValue="
                        + exitValue() + getLastConsoleLines(), e);
            }
        }
    }

    /**
     * Kills the Process. If you expect that the process may not be running anymore, use if (
     * {@link #isAlive()}) around this. If you expect that the process should still be running at
     * this point, call as is - and it will tell if it had nothing to destroy.
     * 
     * @throws ManagedProcessException if the Process is already stopped (either because destroy()
     *             already explicitly called, or it terminated by itself, or it was never started)
     */
    public void destroy() throws ManagedProcessException {
        //
        // if destroy() is ever giving any trouble, the org.openqa.selenium.os.ProcessUtils may be
        // of
        // interest
        //
        if (!isAlive) {
            throw new ManagedProcessException(procLongName()
                    + " was already stopped (or never started)");
        }
        if (logger.isDebugEnabled())
            logger.debug("Going to destroy {}", procLongName());

        watchDog.destroyProcess();

        try {
            // Safer to waitFor() after destroy()
            resultHandler.waitFor();
        } catch (InterruptedException e) {
            throw handleInterruptedException(e);
        }

        if (logger.isInfoEnabled())
            logger.info("Successfully destroyed {}", procLongName());

        isAlive = false;
    }

    // Java Doc shamelessly copy/pasted from java.lang.Thread#isAlive() :
    /**
     * Tests if this process is alive. A process is alive if it has been started and has not yet
     * terminated.
     *
     * @return <code>true</code> if this process is alive; <code>false</code> otherwise.
     */
    public boolean isAlive() {
        // NOPE: return !resultHandler.hasResult();
        return isAlive;
    }

    /**
     * Returns the exit value for the subprocess.
     *
     * @return the exit value of the subprocess represented by this <code>Process</code> object. by
     *         convention, the value <code>0</code> indicates normal termination.
     * @exception ManagedProcessException if the subprocess represented by this
     *                <code>ManagedProcess</code> object has not yet terminated.
     */
    public int exitValue() throws ManagedProcessException {
        try {
            return resultHandler.getExitValue();
        } catch (IllegalStateException e) {
            throw new ManagedProcessException("Exit Value not (yet) available for "
                    + procLongName(), e);
        }
    }

    /**
     * Waits for the process to terminate.
     * 
     * <p>Returns immediately if the process is already stopped (either because destroy() was already
     * explicitly called, or it terminated by itself).
     * 
     * <p>Note that if the process was attempted to be started but that start failed (may be because
     * the executable could not be found, or some underlying OS error) then it throws a
     * ManagedProcessException.
     * 
     * <p>It also throws a ManagedProcessException if {@link #start()} was never even called.
     * 
     * @return exit value (or INVALID_EXITVALUE if {@link #destroy()} was used)
     * @throws ManagedProcessException see above
     */
    public int waitForExit() throws ManagedProcessException {
        logger.info("Thread is now going to wait for this process to terminate itself: {}",
                procLongName());
        return waitForExitMaxMsWithoutLog(-1);
    }

    /**
     * Like {@link #waitForExit()}, but waits max. maxWaitUntilReturning, then returns (even if
     * still running, taking no action).
     * 
     * @param maxWaitUntilReturning Time to wait
     * @return exit value, or INVALID_EXITVALUE if the timeout was reached, or if {@link #destroy()}
     *         was used
     * @throws ManagedProcessException see above
     */
    public int waitForExitMaxMs(long maxWaitUntilReturning) throws ManagedProcessException {
        logger.info("Thread is now going to wait max. {}ms for process to terminate itself: {}",
                maxWaitUntilReturning, procLongName());
        return waitForExitMaxMsWithoutLog(maxWaitUntilReturning);
    }

    protected int waitForExitMaxMsWithoutLog(long maxWaitUntilReturning)
            throws ManagedProcessException {
        assertWaitForIsValid();
        try {
            if (maxWaitUntilReturning != -1) {
                resultHandler.waitFor(maxWaitUntilReturning);
                checkResult();
                if (!isAlive())
                    return exitValue();
                return INVALID_EXITVALUE;
            }
            resultHandler.waitFor();
            checkResult();
            return exitValue();

        } catch (InterruptedException e) {
            throw handleInterruptedException(e);
        }
    }

    /**
     * Like {@link #waitForExit()}, but waits max. maxWaitUntilReturning, then destroys if still
     * running, and returns.
     * 
     * @param maxWaitUntilDestroyTimeout Time to wait
     * @throws ManagedProcessException see above
     */
    public void waitForExitMaxMsOrDestroy(long maxWaitUntilDestroyTimeout)
            throws ManagedProcessException {
        waitForExitMaxMs(maxWaitUntilDestroyTimeout);
        if (isAlive()) {
            logger.info("Process didn't exit within max. {}ms, so going to destroy it now: {}",
                    maxWaitUntilDestroyTimeout, procLongName());
            destroy();
        }
    }

    protected void assertWaitForIsValid() throws ManagedProcessException {
        if (!isAlive() && !resultHandler.hasResult()) {
            throw new ManagedProcessException("Asked to waitFor " + procLongName()
                    + ", but it was never even start()'ed!");
        }
    }

    // ---

    public String getConsole() {
        if (console != null)
            return console.getRecentLines();
        else
            return "";
    }

    public String getLastConsoleLines() {
        return ", last " + consoleBufferMaxLines + " lines of console:\n" + getConsole();
    }

    // ---

    private String procShortName() {
        // could later be extended to some sort of fake numeric PID, e.g. "mysqld-1", from a static
        // Map<String execName, Integer id)
        if (procShortName == null) {
            File exec = getExecutableFile();
            procShortName = exec.getName();
        }
        return procShortName;
    }

    private String procLongName() {
        return "Program "
                + commandLine.toString()
                + (executor.getWorkingDirectory() == null ? "" : " (in working directory "
                        + executor.getWorkingDirectory().getAbsolutePath() + ")");
    }

    // ---

    public class LoggingExecuteResultHandler extends DefaultExecuteResultHandler {

        @Override
        public void onProcessComplete(int exitValue) {
            super.onProcessComplete(exitValue);
            logger.info(procLongName() + " just exited, with value " + exitValue);
            isAlive = false;
        }

        @Override
        public void onProcessFailed(ExecuteException e) {
            super.onProcessFailed(e);
            if (!watchDog.killedProcess()) {
                logger.error(procLongName() + " failed unexpectedly", e);
            }
            isAlive = false;
        }
    }

    public static class LoggingShutdownHookProcessDestroyer extends ShutdownHookProcessDestroyer {

        @Override
        public void run() {
            logger.info("Shutdown Hook: JVM is about to exit! Going to kill destroyOnShutdown processes...");
            super.run();
        }
    }

}
