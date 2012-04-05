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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import ch.vorburger.exec.Platform.Type;
import ch.vorburger.mariadb4j.MariaDB4jException;

/**
 * Tests {@link ManagedProcessBuilder}.
 * 
 * @author Michael Vorburger
 */
public class ManagedProcessBuilderTest {

	@Test
	public void testManagedProcessBuilder() throws IOException, MariaDB4jException {
		ManagedProcessBuilder mbp = new ManagedProcessBuilder(new File("/somewhere/absolute/bin/thing"));
		
		File arg = new File("relative/file");
		mbp.addArgument(arg);
		
		// needed to force auto-setting the directory
		mbp.getCommandLine();
		
		File cwd = mbp.getWorkingDirectory();
		if (Platform.is(Type.Windows)) {
			assertThat(cwd.getAbsolutePath(), is("C:\\somewhere\\absolute\\bin"));
		} else {
			assertThat(cwd.getAbsolutePath(), is("/somewhere/absolute/bin"));			
		}
		
		String arg0 = mbp.getExecutable();
		if (Platform.is(Type.Windows)) {
			assertThat(arg0, is("C:\\somewhere\\absolute\\bin\\thing"));
		} else {
			assertThat(arg0, is("/somewhere/absolute/bin/thing"));			
		}
		
		String arg1 = mbp.getArguments()[0];
		assertNotSame(arg1, "relative/file");
		assertTrue(arg1.contains("relative"));
		//System.out.println(arg1);
	}

}
