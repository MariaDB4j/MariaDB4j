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

/**
 * Builder for ManagedProcess.
 * 
 * This is inspired by {@link java.lang.ProcessBuilder} & {@link org.apache.commons.exec.CommandLine}, and/but:
 * 
 * It offers to add java.io.File arguments, and makes sure that their absolute path is used.
 * 
 * If no directory is set, it automatically sets the initial working directory using the directory of executable if it was a File,
 * and thus makes sure an initial working directory is always passed to the process.
 * 
 * It intentionally doesn't offer "parsing" space delimited command "lines", but forces you to set an executable and add arguments.
 * 
 * @author Michael Vorburger
 */
public class ManagedProcessBuilder {

	private final CommandLine commonsExecCommandLine;
	// private Map<String,String> environment = new HashMap<String, String>();
	private File directory;

	public ManagedProcessBuilder(String executable) {
		commonsExecCommandLine = new CommandLine(executable);
	}

	public ManagedProcessBuilder(File executable) {
		commonsExecCommandLine = new CommandLine(executable);
	}

	/**
	 * Adds a File as a argument to the command.
	 * This uses {@link File#getCanonicalPath()}, which is usually what you'll actually want when launching external processes.
	 * 
	 * @throws IOException 
	 * @see ProcessBuilder
	 */
	public ManagedProcessBuilder addArgument(File arg) throws IOException {
		commonsExecCommandLine.addArgument(arg.getCanonicalPath());
		return this;
	}

	/**
	 * Adds an argument to the command.
	 * The first argument is the name of an executable.
	 * @see ProcessBuilder
	 */
	public ManagedProcessBuilder addArgument(String arg) {
		commonsExecCommandLine.addArgument(arg);
		return this;
	}

	public ManagedProcessBuilder addArgument(String argName, File fileArg) throws IOException {
		addArgument(argName + "=" + fileArg.getCanonicalPath());
		return this;
	}
	
	public String[] getArguments() {
		return commonsExecCommandLine.getArguments();
	}
	
	/**
	 * @see ProcessBuilder#directory(File)
	 */
	public ManagedProcessBuilder directory(File directory) {
		this.directory = directory;
		return this;
	}
	
	/**
	 * @see ProcessBuilder#directory()
	 */
	public File directory() {
		return this.directory;
	}

// Not needed yet - could certainly be implemented with Apache Commons Exec, am just too lazy right now
//	/**
//	 * @see ProcessBuilder#environment(File)
//	 */
//    public Map<String,String> environment() {
//    	return environment;
//    }

	public String executable() {
		return commonsExecCommandLine.getExecutable();
	}

    // ----
    
	public ManagedProcess build() {
		return new ManagedProcess(getCommandLine(), directory/*, environment */);
	}

	/* package-local... let's keep ch.vorburger.exec's API separate from Apache Commons Exec, so it COULD be replaced */  
	CommandLine getCommandLine() {
		if (directory() == null) {
			if (commonsExecCommandLine.isFile()) {
				File exec = new File(commonsExecCommandLine.getExecutable());
				File dir = exec.getParentFile();
				if (dir == null)
					throw new IllegalStateException("directory MUST be set (and could not be auto-determined from executable, although it was a File)");
				this.directory(dir);
// DO NOT   } else {
//				throw new IllegalStateException("directory MUST be set (and could not be auto-determined from executable)");
			}
		}
		return commonsExecCommandLine;
	}

	/**
	 * Intended for debugging / logging, only.
	 */
	@Override
	public String toString() {
		return commonsExecCommandLine.toString();
	}
	
}
