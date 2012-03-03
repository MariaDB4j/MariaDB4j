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

import ch.vorburger.mariadb4j.MariaDB4jException;
import ch.vorburger.mariadb4j.internal.Platform;
import ch.vorburger.mariadb4j.internal.Platform.Type;

/**
 * Tests {@link ManagedProcessBuilder}.
 * 
 * @author Michael Vorburger
 */
public class ManagedProcessBuilderTest {

	@Test
	public void test() throws IOException, MariaDB4jException {
		ManagedProcessBuilder mbp = new ManagedProcessBuilder();
		
		File exec = new File("/somewhere/absolute/bin/thing");
		mbp.add(exec);
		
		File arg = new File("relative/file");
		mbp.add(arg);
		
		File cwd = mbp.getProcessBuilder().directory();
		if (Platform.is(Type.Windows)) {
			assertThat(cwd.getAbsolutePath(), is("C:\\somewhere\\absolute\\bin"));
		} else {
			assertThat(cwd.getAbsolutePath(), is("/somewhere/absolute/bin"));			
		}
		
		String arg0 = mbp.getProcessBuilder().command().get(0);
		if (Platform.is(Type.Windows)) {
			assertThat(arg0, is("C:\\somewhere\\absolute\\bin\\thing"));
		} else {
			assertThat(arg0, is("/somewhere/absolute/bin/thing"));			
		}
		
		String arg1 = mbp.getProcessBuilder().command().get(1);
		assertNotSame(arg1, "relative/file");
		assertTrue(arg1.contains("relative"));
		//System.out.println(arg1);
	}

}
