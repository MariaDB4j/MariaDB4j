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
import java.util.LinkedList;
import java.util.List;

/**
 * Builder for a Process.
 * 
 * This is only called CommandBuilder instead of ProcessBuilder so as to not cause confusion with java.lang.ProcessBuilder.
 * 
 * @author Michael Vorburger
 */
public class CommandBuilder {

	// TODO Directory, Environment

	private String executableName;
	private final List<String> arguments = new LinkedList<String>();

	public RunningProcess exec() throws IOException {
		List<String> line = new LinkedList<String>();
		line.add(executableName);
		line.addAll(arguments);
		return new RunningProcess(line);
	}

	/**
	 * Sets the executable name.
	 * The arguments lists is cleared.
	 * @param name Name of executable process (e.g. "dir" or "notepad.exe" or "ls" etc.)
	 */
	public void setExecutable(String name) {
		this.executableName = name;
		arguments.clear();
	}

	public void addArgument(String arg) {
		arguments.add(arg);
	}

}
