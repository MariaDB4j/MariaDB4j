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

/**
 * OS Process.
 * 
 * @author Michael Vorburger
 */
public class RunningProcess {

	// TODO Directory, Environment
	// TODO suck output... in a rolling buffer? @see my https://github.com/mifos/head/blob/master/war-test-exec/src/test/java/org/mifos/server/wartestexec/MifosExecutableWARBasicTest.java) 
	// TODO Needs timeout management? (again @see my https://github.com/mifos/head/blob/master/war-test-exec/src/test/java/org/mifos/server/wartestexec/MifosExecutableWARBasicTest.java) 
	
	private final ProcessBuilder pb;
	private Process proc = null;

	// package-local, NOT public
	RunningProcess(List<String> line) throws IOException {
		pb = new ProcessBuilder(line);
		pb.redirectErrorStream(true);
		proc = pb.start();
	}

	public int quit() throws IllegalStateException {
		if (proc == null) {
			throw new IllegalStateException(procName() + " was already stopped");
		}
		proc.destroy();
		int r;
		try {
			// NOTE: We MUST waitFor() after destroy() - on some platforms at least, such as Windows
			r = proc.waitFor();
		} catch (InterruptedException e) {
			throw new RuntimeException("Huh?! This should normally never happen here", e);
		}  
		proc = null;
		return r;
	}

	private String procName() {
		return "Program \"" + pb.command() + "\"" + (pb.directory() == null ? "" : " (in directory \"" + pb.directory().getAbsolutePath() + "\")");
	}

}
