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

import org.apache.commons.exec.LogOutputStream;
import org.slf4j.Logger;

/**
 * OutputStream which logs to SLF4j.
 * 
 * With many thanks to http://stackoverflow.com/questions/5499042/writing-output-error-to-log-files-using-pumpstreamhandler
 * 
 * @author Michael Vorburger
 */
// intentionally package local
class SLF4jLogOutputStream extends LogOutputStream {

	enum Type { stdout, stderr }
	
	private final Logger logger;
	private final Type type;
	private final String pid;
	
	SLF4jLogOutputStream(Logger logger, String pid, Type type) {
		this.logger = logger;
		this.type = type;
		this.pid = pid;
	}
	
	@Override
	protected void processLine(String line, @SuppressWarnings("unused") int level) {
		switch (type) {
		case stdout:
			logger.info("{}: {}", pid, line);
			break;

		case stderr:
			logger.error("{}: {}", pid, line);
			break;
		}
	}

}
