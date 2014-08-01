/*
 * Copyright (c) 2014 Michael Vorburger
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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import ch.vorburger.exec.ManagedProcessException;

/**
 * MariaDB4j starter "Service".
 * 
 * Its main() could be used typically from an IDE (waits for CR to shutdown..),
 * or e.g. as a Spring / Guice bean from a withing @Configuration (Module).
 *  
 * @author Michael Vorburger
 */
// Do NOT @org.springframework.stereotype.Service this - we don't want it to be auto-started without explicit declaration
public class MariaDB4jService {

	protected DB db;
	protected DBConfigurationBuilder configBuilder;
	
	public DB getDB() {
		return db;
	}
	
	public String getURL(String databaseName) {
		return configBuilder.getURL(databaseName);
	}
	
	@PostConstruct
	protected void start() throws ManagedProcessException {
		configBuilder = DBConfigurationBuilder.newBuilder();
		configBuilder.detectFreePort();
		db = DB.newEmbeddedDB(configBuilder.build());
		db.start();
	}

	@PreDestroy
	protected void stop() throws ManagedProcessException {
		db.stop();
	}
	
	public static void main(String[] args) throws Exception {
		MariaDB4jService service = new MariaDB4jService();
		service.start();

		// NOTE: In Eclipse, System.console() is not available.. so: (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=122429)
		System.out.println("\n\nHit Enter to quit...");
		BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
		d.readLine();
	
		// NOTE: In Eclipse, the MariaDB4j Shutdown Hook is not invoked on exit.. so: (@see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38016)
		service.stop();
	}
	
}
