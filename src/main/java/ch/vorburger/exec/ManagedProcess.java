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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.vorburger.mariadb4j.DB;

/**
 * Managed OS Process (Executable, Program, Command).
 * 
 * @see http://commons.apache.org/exec/ but this is Java 1.5 (ProcessBuilder based) compliant, and simpler.  Could be switched later, if there is any need.
 * 
 * @author Michael Vorburger
 */
public class ManagedProcess {
	
	private static final Logger logger = LoggerFactory.getLogger(DB.class);

	// TODO suck output... in a rolling buffer? @see my
	// https://github.com/mifos/head/blob/master/war-test-exec/src/test/java/org/mifos/server/wartestexec/MifosExecutableWARBasicTest.java)
	
	// TODO Needs timeout management? with a waitFor(long ms) (again @see my
	// https://github.com/mifos/head/blob/master/war-test-exec/src/test/java/org/mifos/server/wartestexec/MifosExecutableWARBasicTest.java)

	private final ProcessBuilder pb;
	private final ManagedProcessOutputListener output;
	
	private Process proc = null;
	Integer exitValue = null;

	public ManagedProcess(ProcessBuilder pb) {
		pb.redirectErrorStream(true); // TODO remove once stdout/stderr are properly separately managed below...
		output = new ManagedProcessOutputListener() {
			@Override
			public void writeStdOut(int c) {
				System.out.write(c);
			}
		};
		this.pb = pb;
	}

	public ManagedProcess(ManagedProcessBuilder mpb) {
		this(mpb.getProcessBuilder());
	}
	
	public void start() throws IOException, IllegalStateException {
		if (isAlive()) {
			throw new IllegalStateException(procName() + " is still running, use another ManagedProcess instance to launch another one");
		}
		if (logger.isInfoEnabled())
			logger.info("Starting {}", procName());
		proc = pb.start();
		registerThreads();
	}

	private void registerThreads() {
		final InputStream stdout = proc.getInputStream();
		// TODO Buffer Stream!
		// TODO How/when will we close this stream/s?
		Thread stdOutThread = new Thread("Thread to manage stdout of " + procName()) {
			@Override
			public void run() {
				// TODO use: byte[] buffer = new byte[1024];
				while(true) {
					try {
						int nextByte = stdout.read();
						output.writeStdOut(nextByte);
					} catch (IOException e) {
						logger.warn("Unexpected IOException from stdout.read()", e);
					}
				}
			}
		};
		stdOutThread.setDaemon(true);
		stdOutThread.start();

// TODO properly manage stdout/stderr separately 
//		InputStream stderr = proc.getErrorStream();
	}

	/**
	 * Kills the Process.
	 * 
	 * If it has already exited by itself before, just returns it exit value.
	 * Callers might want to use isRunning() to distinguish.
	 * 
	 * @return the exit value of the process
	 * @throws IllegalStateException if the Process was already explicitly stopped (destroy() already called) 
	 */
	public int destroy() throws IllegalStateException {
		if (logger.isInfoEnabled())
			logger.info("About to stop {}", procName());
		if (proc == null) {
			throw new IllegalStateException(procName() + " was already stopped");
		}
		proc.destroy();
		try {
			// NOTE: We MUST waitFor() after destroy() - on some platforms at least, such as Windows
			exitValue = proc.waitFor();
		} catch (InterruptedException e) {
			throw new RuntimeException("Huh?! This should normally never happen here..." + procName(), e);
		}
		proc = null;
		if (logger.isInfoEnabled())
			logger.info("Successfully stopped {}, exit value = {}", procName(), exitValue);
		return exitValue;
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
		if (proc == null)
			return false;
		try {
			exitValue = proc.exitValue();
			return false;
		} catch (IllegalThreadStateException e) {
			return true;
		}
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
		if (exitValue == null) {
			throw new IllegalStateException(procName() + " hasn't run yet - no exit value available");
		}
		return exitValue;
	}

	// TODO public int waitFor() throws IllegalStateException;
	// TODO public int waitFor(long maxWaitUntilDestroyTimeout) throws IllegalStateException;

	// ... must throw exception if proc terminates with something else than expected message
	// TODO public int waitFor(String consoleMessage, maxWaitUntilDestroyTimeout) throws IllegalStateException;
	
	private String procName() {
		return "Program \""
				+ pb.command()
				+ "\""
				+ (pb.directory() == null ? "" : " (in working directory \""
						+ pb.directory().getAbsolutePath() + "\")");
	}

}
