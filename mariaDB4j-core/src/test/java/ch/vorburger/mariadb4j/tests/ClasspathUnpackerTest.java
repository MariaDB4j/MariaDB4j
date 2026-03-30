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
package ch.vorburger.mariadb4j.tests;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.vorburger.mariadb4j.Util;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

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
        assertThat(new File(toDir, "CommandLine.class").exists()).isTrue();
    }

    @Test
    @Disabled
    // Not yet implemented... not really important
    public void testClasspathUnpackerFromDuplicateClasspath() throws IOException {
        File toDir = new File("target/testUnpack3");
        FileUtils.deleteDirectory(toDir);
        assertThrows(
                IOException.class,
                () -> {
                    Util.extractFromClasspathToFile("META-INF/maven", toDir);
                });
    }

    @Test
    public void testClasspathUnpackerFromFilesystem() throws IOException {
        File toDir = new File("target/testUnpack3");
        FileUtils.deleteDirectory(toDir);
        int c1 = Util.extractFromClasspathToFile("test", toDir);
        assertThat(c1).isEqualTo(3);
        assertThat(new File(toDir, "a.txt").exists()).isTrue();
        assertThat(new File(toDir, "b.txt").exists()).isTrue();
        assertThat(new File(toDir, "subdir/c.txt").exists()).isTrue();

        // Now try again - it shouldn't copy anything anymore (optimization)
        int c2 = Util.extractFromClasspathToFile("test", toDir);
        assertThat(c2).isEqualTo(0);
    }

    @Test
    public void testClasspathUnpackerPathDoesNotExist() throws IOException {
        File toDir = new File("target/testUnpack4");
        FileUtils.deleteDirectory(toDir);
        assertThrows(
                IOException.class,
                () -> {
                    Util.extractFromClasspathToFile("does/not/exist", toDir);
                });
    }

    @Test
    public void testClasspathUnpackerPackageExistsButIsEmpty() throws IOException {
        File toDir = new File("target/testUnpack4");
        FileUtils.deleteDirectory(toDir);
        assertThrows(
                IOException.class,
                () -> {
                    Util.extractFromClasspathToFile("test/empty", toDir);
                });
    }
}
