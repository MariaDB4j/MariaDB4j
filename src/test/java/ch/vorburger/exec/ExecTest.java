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

import org.junit.Test;

import ch.vorburger.mariadb4j.MariaDB4jException;
import ch.vorburger.mariadb4j.internal.Platform;
import ch.vorburger.mariadb4j.internal.Platform.Type;

/**
 * Test Exec Util.
 * 
 * @author Michael Vorburger
 */
public class ExecTest {

	@Test
	public void testSelfTerminatingExec() throws Exception {
		String cmd;
		String arg;

		switch (Platform.is()) {
		case Windows:
			cmd = "cmd.exe";
			arg = "/C dir /X";
			break;

		case Mac:
		case Linux:
		case Solaris:
			cmd = "ls";
			arg = "-lh";
			break;

		default:
			throw new MariaDB4jException("Unexpected Platform, write test...");
		}

		CommandBuilder pf = new CommandBuilder();
		pf.setExecutable(cmd);
		pf.addArgument(arg);
		RunningProcess shortProc = pf.exec();
		// TODO assert non-null PID?
		// TODO assert state is running?
		Thread.sleep(2000);
		// TODO assert state is stopped?
	}

	@Test
	public void testMustTerminateExec() throws Exception {
		String cmd;
		if (Platform.is(Type.Windows)) {
			cmd = "notepad.exe";
		} else {
			cmd = "vi";
		}
		CommandBuilder pf = new CommandBuilder();
		pf.setExecutable(cmd);
		RunningProcess blockingProc = pf.exec();
		// TODO assert non-null PID?
		Thread.sleep(2000);
		blockingProc.quit();
		// TODO assert state is stopped?
	}

}
