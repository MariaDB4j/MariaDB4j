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

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;

import java.io.IOException;

import org.junit.Test;

import ch.vorburger.mariadb4j.MariaDB4jException;
import ch.vorburger.mariadb4j.internal.Platform;
import ch.vorburger.mariadb4j.internal.Platform.Type;

/**
 * Tests ManagedProcess.
 * 
 * @author Michael Vorburger
 */
public class ManagedProcessTest {

	@Test
	public void testBasics() throws Exception {
		ManagedProcess p = new ManagedProcess(new ProcessBuilder());
		assertThat(p.isRunning(), is(false));
		try {
			p.destroy();
		} catch (IllegalStateException e) {
			// as expected
		}
		try {
			p.exitValue();
		} catch (IllegalStateException e) {
			// as expected
		}
	}

	@Test(expected=IOException.class)
	public void testNonExistingCommand() throws Exception {
		ManagedProcess p = new ManagedProcess(new ProcessBuilder("something"));
		p.start();
	}
	
	@Test
	public void testSelfTerminatingExec() throws Exception {
		ProcessBuilder pb;
		switch (Platform.is()) {
		case Windows:
			pb = new ProcessBuilder("cmd.exe", "/C dir /X");
			break;

		case Mac:
		case Linux:
		case Solaris:
			pb = new ProcessBuilder("true", "--version");
			break;

		default:
			throw new MariaDB4jException("Unexpected Platform, improve the test dude...");
		}

		ManagedProcess p = new ManagedProcess(pb);
		assertThat(p.isRunning(), is(false));
		p.start();
		assertThat(p.isRunning(), is(true));
		Thread.sleep(200); 
		assertThat(p.isRunning(), is(false));
		p.exitValue(); // just making sure it works, don't check, as Win/NIX diff.
		// TODO Check that output was produced?
	}

	@Test
	public void testMustTerminateExec() throws Exception {
		ProcessBuilder pb;
		if (Platform.is(Type.Windows)) {
			pb = new ProcessBuilder("notepad.exe");
		} else {
			pb = new ProcessBuilder("vi"); // TODO ?
		}
		
		ManagedProcess p = new ManagedProcess(pb);
		assertThat(p.isRunning(), is(false));
		p.start();
		assertThat(p.isRunning(), is(true));
		p.destroy();
		assertThat(p.isRunning(), is(false));
		p.exitValue(); // just making sure it works, don't check, as Win/NIX diff.
	}

}
