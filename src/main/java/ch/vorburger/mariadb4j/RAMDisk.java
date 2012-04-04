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
package ch.vorburger.mariadb4j;

import java.io.File;

/**
 * RAM Disk.
 * 
 * This (of course) doesn't actually implement a RAM Disk in Java, but simply
 * appropriately launches a number of built-in OS specific commands.
 * 
 * @author Michael Vorburger
 */
public class RAMDisk {

	// TODO post on http://stackoverflow.com/questions/4428217/can-a-java-ram-disk-be-created-to-be-used-with-the-java-io-api

	private boolean stopOnExit = true;

	public RAMDisk(long sizeInBytes) {
		throw new UnsupportedOperationException();
	}

	public void start() {
		throw new UnsupportedOperationException();
	}

	public void stop() {
		throw new UnsupportedOperationException();
	}

	public File path() {
		throw new UnsupportedOperationException();
	}

	public boolean isStopOnExit() {
		return stopOnExit;
	}

	public void setStopOnExit(boolean stopOnExit) {
		this.stopOnExit = stopOnExit;
	}
}
