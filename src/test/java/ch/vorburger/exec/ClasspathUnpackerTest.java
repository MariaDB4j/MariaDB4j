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

import java.io.File;
import java.io.IOException;

import ch.vorburger.mariadb4j.Util;
import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for ClasspathUnpacker.
 * 
 * @author Michael Vorburger
 */
public class ClasspathUnpackerTest {

	@Test
	public void testClasspathUnpackerFromUniqueClasspath() throws IOException {
		File toDir = new File("target/testUnpack1");
		FileUtils.deleteDirectory(toDir);
		Util.extractFromClasspathToFile("org/apache/commons/exec", toDir);
		Assert.assertTrue(new File(toDir, "CommandLine.class").exists());
	}
	
	@Test(expected=IOException.class)
	@Ignore // Not yet implemented... not really important
	public void testClasspathUnpackerFromDuplicateClasspath() throws IOException {
		File toDir = new File("target/testUnpack3");
		FileUtils.deleteDirectory(toDir);
		Util.extractFromClasspathToFile("META-INF/maven", toDir);
	}
	
	@Test
	public void testClasspathUnpackerFromFilesystem() throws IOException {
		File toDir = new File("target/testUnpack3");
		FileUtils.deleteDirectory(toDir);
		int c1 = Util.extractFromClasspathToFile("test", toDir);
		Assert.assertEquals(3, c1);
		Assert.assertTrue(new File(toDir, "a.txt").exists());
		Assert.assertTrue(new File(toDir, "b.txt").exists());
		Assert.assertTrue(new File(toDir, "subdir/c.txt").exists());
		
		// Now try again - it shouldn't copy anything anymore (optimization)
		int c2 = Util.extractFromClasspathToFile("test", toDir);
		Assert.assertEquals(0, c2);
	}

	@Test(expected=IOException.class)
	public void testClasspathUnpackerPathDoesNotExist() throws IOException {
		File toDir = new File("target/testUnpack4");
		FileUtils.deleteDirectory(toDir);
		Util.extractFromClasspathToFile("does/not/exist", toDir);
	}

	@Test(expected=IOException.class)
	public void testClasspathUnpackerPackageExistsButIsEmpty() throws IOException {
		File toDir = new File("target/testUnpack4");
		FileUtils.deleteDirectory(toDir);
		Util.extractFromClasspathToFile("test/empty", toDir);
	}

}
