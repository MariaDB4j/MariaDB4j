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

import static org.junit.jupiter.api.Assertions.*;

import ch.vorburger.mariadb4j.Util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test for ClasspathUnpacker.
 *
 * @author Michael Vorburger
 */
class ClasspathUnpackerTest {

    @Test
    void testClasspathUnpackerFromUniqueClasspath(@TempDir Path tempDir) throws IOException {
        Path toDir = tempDir.resolve("testUnpack1");
        Util.extractFromClasspathToDir("org/apache/commons/exec", toDir);
        assertTrue(Files.exists(toDir.resolve("CommandLine.class")));
    }

    @Test
    @Disabled("Not implemented yet")
    void testClasspathUnpackerFromDuplicateClasspath(@TempDir Path tempDir) {
        Path toDir = tempDir.resolve("testUnpack3");
        assertThrows(
                IOException.class, () -> Util.extractFromClasspathToDir("META-INF/maven", toDir));
    }

    @Test
    void testClasspathUnpackerFromFilesystem(@TempDir Path tempDir) throws IOException {
        Path toDir = tempDir.resolve("testUnpack3");
        int c1 = Util.extractFromClasspathToDir("test", toDir);
        assertEquals(3, c1);
        assertTrue(Files.exists(toDir.resolve("a.txt")));
        assertTrue(Files.exists(toDir.resolve("b.txt")));
        assertTrue(Files.exists(toDir.resolve("subdir").resolve("c.txt")));

        // Now try again - it shouldn't copy anything anymore (optimization)
        int c2 = Util.extractFromClasspathToDir("test", toDir);
        assertEquals(0, c2);
    }

    @Test
    void testClasspathUnpackerPathDoesNotExist(@TempDir Path tempDir) {
        Path toDir = tempDir.resolve("testUnpack4");
        assertThrows(
                IOException.class, () -> Util.extractFromClasspathToDir("does/not/exist", toDir));
    }

    @Test
    void testClasspathUnpackerPackageExistsButIsEmpty(@TempDir Path tempDir) {
        Path toDir = tempDir.resolve("testUnpack4");
        assertThrows(IOException.class, () -> Util.extractFromClasspathToDir("test/empty", toDir));
    }
}
