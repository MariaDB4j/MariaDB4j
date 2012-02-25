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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.vorburger.mariadb4j.DB;

/**
 * OS Process (Executable, Program, Command)
 * 
 * @see http://commons.apache.org/exec/ but this is Java 1.5 (ProcessBuilder based) compliant, and simpler.  Could be switched later, if there is any need.
 * 
 * @author Michael Vorburger
 */
public class RunningProcess {
	// TODO rename to Program?
	
	private static final Logger logger = LoggerFactory.getLogger(DB.class);

	// TODO Directory, Environment
	// TODO suck output... in a rolling buffer? @see my
	// https://github.com/mifos/head/blob/master/war-test-exec/src/test/java/org/mifos/server/wartestexec/MifosExecutableWARBasicTest.java)
	// TODO Needs timeout management? (again @see my
	// https://github.com/mifos/head/blob/master/war-test-exec/src/test/java/org/mifos/server/wartestexec/MifosExecutableWARBasicTest.java)

	private final ProcessBuilder pb;
	private Process proc = null;

//	/**
//	 * Helper to create a ProcessBuilder with redirectErrorStream = true (instead of default false), and the environment cleaned
//	 * 
//	 * @return ProcessBuilder
//	 */
//	public static ProcessBuilder newProcessBuilder() {
//		ProcessBuilder pb = new ProcessBuilder();
//		pb.redirectErrorStream(true);
//		pb.environment().clear();
//		return pb;
//	}
	
	public RunningProcess(ProcessBuilder pb) {
		pb.redirectErrorStream(true);
		this.pb = pb;
	}
	
	// package-local, NOT public
	RunningProcess(List<String> line) throws IOException {
		pb = new ProcessBuilder(line);
		pb.redirectErrorStream(true);
		if (logger.isInfoEnabled())
			logger.info("Starting {}", procName());
		proc = pb.start();
	}

	public int quit() throws IllegalStateException {
		if (logger.isInfoEnabled())
			logger.info("About to stop {}", procName());
		if (proc == null) {
			throw new IllegalStateException(procName() + " was already stopped");
		}
		proc.destroy();
		int r;
		try {
			// NOTE: We MUST waitFor() after destroy() - on some platforms at
			// least, such as Windows
			r = proc.waitFor();
		} catch (InterruptedException e) {
			throw new RuntimeException(
					"Huh?! This should normally never happen here..."
							+ procName(), e);
		}
		proc = null;
		if (logger.isInfoEnabled())
			logger.info("Successfully stopped {}, exit value = {}", procName(),
					r);
		return r;
	}

	private String procName() {
		return "Program \""
				+ pb.command()
				+ "\""
				+ (pb.directory() == null ? "" : " (in directory \""
						+ pb.directory().getAbsolutePath() + "\")");
	}

}
