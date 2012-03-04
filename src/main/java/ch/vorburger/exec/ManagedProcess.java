/*
 * Copyright (c) 2012 Michael Vorburger
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */
package ch.vorburger.exec;

import java.io.File;
import java.io.IOException;

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

import ch.vorburger.exec.SLF4jLogOutputStream.Type;

/**
 * Managed OS Process (Executable, Program, Command).
 * Created by {@link ManagedProcessBuilder#build()}.
 * 
 * Intended for controlling external "tools", often "daemons", which produce some text-based control output. 
 * In this form not yet suitable for programs returning binary data via stdout (but could be extended).
 * Currently it does not yet offer stdin support (but could if needed).
 *
 * Does reasonably extensive logging about what it's doing (contrary to Apache Commons Exec), 
 * including logging the processes stdout & stderr, into SLF4J (not the System.out.Console). 
 *
 * @see Internally based on http://commons.apache.org/exec/ but intentionally not exposing this; could be switched later, if there is any need.
 * 
 * @author Michael Vorburger
 */
public class ManagedProcess {

	private static final Logger logger = LoggerFactory.getLogger(ManagedProcess.class);

	private final CommandLine commandLine;
	private final Executor executor = new DefaultExecutor();
	private final DefaultExecuteResultHandler resultHandler = new LoggingExecuteResultHandler();
	private final ExecuteWatchdog watchDog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
	private final ProcessDestroyer shutdownHookProcessDestroyer = new LoggingShutdownHookProcessDestroyer();

	private boolean isAlive = false;
	private boolean destroyOnShutdown = true;
	private String procShortName;
	private int consoleBufferMaxLines = 50;
	private RollingLogOutputStream console;
	private MultiOutputStream stdouts;
	private MultiOutputStream stderrs;
	
	/**
	 * Package local constructor.
	 * 
	 * Keep ch.vorburger.exec's API separate from Apache Commons Exec, so it COULD be replaced.
	 * 
	 * @see ManagedProcessBuilder#build()
	 * 
	 * @param commandLine Apache Commons Exec CommandLine 
	 * @param directory Working directory, or null
	 */
	ManagedProcess(CommandLine commandLine, File directory) {
		this.commandLine = commandLine;
		if (directory != null) {
			executor.setWorkingDirectory(directory);
		}
		executor.setWatchdog(watchDog);
	}

	/**
	 * Starts the Process.
	 * 
	 * This method always immediately returns (i.e. launches the process asynchronously).
	 * Use the different waitFor... methods if you want to "block" on the spawned process.
	 * 
	 * @throws IOException if it couldn't be started
	 * @throws IllegalStateException if it's already started
	 */
	public void start() throws IOException, IllegalStateException {
		if (isAlive) {
			throw new IllegalStateException(procLongName() + " is still running, use another ManagedProcess instance to launch another one");
		}
		if (logger.isInfoEnabled())
			logger.info("Starting {}", procLongName());
		
		stdouts = new MultiOutputStream();
		stderrs = new MultiOutputStream();
		PumpStreamHandler outputHandler = new PumpStreamHandler(stdouts, stderrs);
		executor.setStreamHandler(outputHandler);
		
		String pid = procShortName();
		stdouts.addOutputStream(new SLF4jLogOutputStream(logger, pid, Type.stdout));
		stderrs.addOutputStream(new SLF4jLogOutputStream(logger, pid, Type.stderr));
		
		if (consoleBufferMaxLines > 0) {
			console = new RollingLogOutputStream(consoleBufferMaxLines);
			stdouts.addOutputStream(console);
			stderrs.addOutputStream(console);
		}			
		
		if (destroyOnShutdown) {
			executor.setProcessDestroyer(shutdownHookProcessDestroyer);
		}
		
		executor.execute(commandLine, resultHandler);
		isAlive = true;
	}

	/**
	 * Kills the Process.
	 * 
	 * @throws IllegalStateException if the Process was already explicitly stopped (destroy() already called) 
	 */
// TODO Clarify/test/document behaviour if proc terminated by itself
//	 * If it has already exited by itself before, just returns it exit value.
//	 * Callers might want to use isRunning() to distinguish.
//	 * 
// TODO There isn't really an exit value on destroy(), is there? (* @return the exit value of the process)
	public void /*int*/ destroy() throws IllegalStateException {
		// 
		// if destroy() is ever giving any trouble, the org.openqa.selenium.os.ProcessUtils may be of interest
		//
		if (!isAlive) {
			throw new IllegalStateException(procLongName() + " was already stopped (or never started)");
		}
		if (logger.isDebugEnabled())
			logger.debug("Going to destroy {}", procLongName());

		watchDog.destroyProcess();
		
//		try {
//			// NOTE: We MUST waitFor() after destroy() - on some platforms at least, such as Windows
//			Process proc;
//			int exitValue = proc.waitFor();
//		} catch (InterruptedException e) {
//			throw new RuntimeException("Huh?! This should normally never happen here..." + procLongName(), e);
//		}

		// TODO There isn't really an exit value on destroy(), is there? 
//		int exitValue = exitValue();
		if (logger.isInfoEnabled())
			logger.info("Successfully destroyed {}", procLongName());
//			logger.info("Successfully stopped {}, exit value = {}", procName(), exitValue);
		
		isAlive = false;
//		return exitValue;
	}


	// Java Doc shamelessly copy/pasted from java.lang.Thread#isAlive() :
    /**
     * Tests if this process is alive. 
     * A process is alive if it has been started and has not yet terminated. 
     *
     * @return  <code>true</code> if this process is alive;
     *          <code>false</code> otherwise.
     */
	public boolean isAlive() {
		// NOPE: return !resultHandler.hasResult();
		return isAlive;
	}
	
    /**
     * Returns the exit value for the subprocess.
     *
     * @return  the exit value of the subprocess represented by this 
     *          <code>Process</code> object. by convention, the value 
     *          <code>0</code> indicates normal termination.
     * @exception  IllegalStateException  if the subprocess represented 
     *             by this <code>ManagedProcess</code> object has not yet terminated.
     */
	public int exitValue() throws IllegalStateException {
		return resultHandler.getExitValue();
	}

	public void waitFor() /*throws IllegalStateException*/ {
		try {
			logger.info("Thread is now going to wait for this process to terminate itself: {}", procLongName());
			resultHandler.waitFor();
		} catch (InterruptedException e) {
			throw new RuntimeException("Huh?! This should normally never happen here..." + procLongName(), e);
		}
	}

	public void waitFor(long maxWaitUntilDestroyTimeout) /*throws IllegalStateException*/ {
		try {
			logger.info("Thread is now going to wait max. {}ms for process to terminate itself: {}", maxWaitUntilDestroyTimeout, procLongName());
			resultHandler.waitFor(maxWaitUntilDestroyTimeout);
		} catch (InterruptedException e) {
			throw new RuntimeException("Huh?! This should normally never happen here..." + procLongName(), e);
		}
	}
	
	public void waitForAndDestroy(long maxWaitUntilDestroyTimeout) /*throws IllegalStateException*/ {
		waitFor(maxWaitUntilDestroyTimeout);
		if (isAlive()) {
			logger.info("Process didn't exit within max. {}ms, so going to destroy it now: {}", maxWaitUntilDestroyTimeout, procLongName());
			destroy();
		}
	}

	// ... must throw exception if proc terminates with something else than expected message
	public void waitFor(String messageInConsole) /*throws IllegalStateException*/ {
		// Code review comments most welcome; I'm not 100% sure the thread concurrency time is right; is there a chance a console message may be "missed" here, and we block forever?
		if (getConsole().contains(messageInConsole)) {
			logger.info("Asked to wait for \"{}\" from {}, but already seen it recently in Console, so returning immediately", messageInConsole, procLongName());
			return;
		}
		
		CheckingConsoleOutputStream checkingConsoleOutputStream = new CheckingConsoleOutputStream(messageInConsole);
		stdouts.addOutputStream(checkingConsoleOutputStream);
		stderrs.addOutputStream(checkingConsoleOutputStream);
		
		final int SLEEP_TIME_MS = 50;
		logger.info("Thread is now going to wait for \"{}\" to appear in Console output of process {}", messageInConsole, procLongName());
        while (!checkingConsoleOutputStream.hasSeenIt()) {
            try {
				Thread.sleep(SLEEP_TIME_MS);
			} catch (InterruptedException e) {
				throw new RuntimeException("Huh?! This should normally never happen here..." + procLongName(), e);
			}
        }
		stdouts.removeOutputStream(checkingConsoleOutputStream);
		stderrs.removeOutputStream(checkingConsoleOutputStream);
	}

	// ---
	
	public boolean isDestroyOnShutdown() {
		return destroyOnShutdown;
	}
	
	public ManagedProcess setDestroyOnShutdown(boolean flag) {
		this.destroyOnShutdown = flag;
		return this;
	}

	public void setConsoleBufferMaxLines(int consoleBufferMaxLines) {
		this.consoleBufferMaxLines = consoleBufferMaxLines;
	}

	public int getConsoleBufferMaxLines() {
		return consoleBufferMaxLines;
	}

	public String getConsole() {
		return console.getRecentLines();
	}
	

	// ---
	
	private String procShortName() {
		// could later be extended to some sort of fake numeric PID, e.g. "mysqld-1", from a static Map<String execName, Integer id)
		if (procShortName == null) {
			File exec = new File(commandLine.getExecutable());
			procShortName = exec.getName();
		}
		return procShortName;
	}
	
	private String procLongName() {
		return "Program " + commandLine.toString() 
				+ (executor.getWorkingDirectory() == null ? "" : 
					" (in working directory " + executor.getWorkingDirectory().getAbsolutePath() + ")");
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
