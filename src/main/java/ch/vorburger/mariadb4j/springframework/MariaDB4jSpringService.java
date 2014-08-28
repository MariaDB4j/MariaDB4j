/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2012 - 2014 Michael Vorburger
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package ch.vorburger.mariadb4j.springframework;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.Lifecycle;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.MariaDB4jService;

// Do NOT @org.springframework.stereotype.Service this - we don't want it to be auto-started without explicit declaration
public class MariaDB4jSpringService extends MariaDB4jService implements Lifecycle {

	protected ManagedProcessException lastException;

	@Value("${mariaDB4j.port:3306}")
	public void setPort(int port) {
		getConfiguration().setPort(port);
	}
	
	@Value("${mariaDB4j.socket:NA}")
	public void setSocket(String socket) {
		if (!"NA".equals(socket))
			getConfiguration().setSocket(socket);
	}
	
	@Value("${mariaDB4j.dataDir:NA}")
	public void setDataDir(String dataDir) {
		if (!"NA".equals(dataDir))
			getConfiguration().setDataDir(dataDir);
	}
	
	@Value("${mariaDB4j.baseDir:NA}") 
	public void setBaseDir(String baseDir) {
		if (!"NA".equals(baseDir))
			getConfiguration().setBaseDir(baseDir);
	}
	
	@Override
	public void start() { // no throws ManagedProcessException 
		try {
			super.start();
		} catch (ManagedProcessException e) {
			lastException = e;
			throw new IllegalStateException("MariaDB4jSpringService start() failed", e);
		}
	}
	
	@Override
	public void stop() { // no throws ManagedProcessException 
		try {
			super.stop();
		} catch (ManagedProcessException e) {
			lastException = e;
			throw new IllegalStateException("MariaDB4jSpringService stop() failed", e);
		}
	}

}