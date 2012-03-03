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
import java.util.List;
import java.util.Map;

/**
 * Builder for ManagedProcess.
 * 
 * This is very similar to {@link java.lang.ProcessBuilder}, just has some additional convenience functions:
 * 
 * It offers to add java.io.File arguments, and make sure that their absolute path is used.
 * It automatically sets the initial working directory using the directory of the first argument.
 * It makes sure an initial working directory is always passed to the process.
 * 
 * @author Michael Vorburger
 */
public class ManagedProcessBuilder {

	protected ProcessBuilder pb = new ProcessBuilder();

	public ManagedProcess build() {
		return new ManagedProcess(getProcessBuilder());
	}
	
	public ProcessBuilder getProcessBuilder() {
		if (pb.directory() == null) {
			if (pb.command().isEmpty()) {
				throw new IllegalStateException("NO args have been set");
			}
			File exec = new File(pb.command().get(0));
			File dir = exec.getParentFile();
			if (dir == null)
				throw new IllegalStateException("directory MUST be set (and could not be auto-determined from first argument)");
			this.directory(dir);
		}
		return pb;
	}

	/**
	 * Adds a File as a argument to the command.
	 * This uses {@link File#getCanonicalPath()}, which is usually what you'll actually want when launching external processes.
	 * If the 
	 * @throws IOException 
	 * @see ProcessBuilder
	 */
	public ManagedProcessBuilder add(File arg) throws IOException {
		pb.command().add(arg.getCanonicalPath());
		return this;
	}

	/**
	 * Adds an argument to the command.
	 * The first argument is the name of an executable.
	 * @see ProcessBuilder
	 */
	public ManagedProcessBuilder add(String arg) {
		pb.command().add(arg);
		return this;
	}

	/**
	 * @see ProcessBuilder#command()
	 */
    public List<String> command() {
    	return pb.command();
    }
	
	/**
	 * @see ProcessBuilder#directory(File)
	 */
	public ManagedProcessBuilder directory(File directory) {
		pb.directory(directory);
		return this;
	}
	
	/**
	 * @see ProcessBuilder#directory()
	 */
	public File directory() {
		return pb.directory();
	}
	
	/**
	 * @see ProcessBuilder#environment(File)
	 */
    public Map<String,String> environment() {
    	return pb.environment();
    }
    
    // Intentionally no start() and redirectErrorStream() here - ManagedProcess handles that
}
