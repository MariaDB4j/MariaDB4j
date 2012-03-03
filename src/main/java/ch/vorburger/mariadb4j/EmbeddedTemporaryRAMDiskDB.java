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
import java.io.IOException;

/**
 * Embedded MariaDB (or MySQL®).
 * 
 * This one tries to, if possible, place the temporary DB on a RAM Disk.
 * 
 * @author Michael Vorburger
 */
public class EmbeddedTemporaryRAMDiskDB extends EmbeddedTemporaryDB {

	public EmbeddedTemporaryRAMDiskDB() throws IOException {
		super(tempRAMDiskDataDir());
	}

	protected static File tempRAMDiskDataDir() {
		// TODO Implement deleteOnExit! How to avoid duplicating?
		throw new UnsupportedOperationException();
	}

}
