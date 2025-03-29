/*
 * #%L
 * MariaDB4j
 * %%
 * Copyright (C) 2025 Michael Vorburger
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
package ch.vorburger.mariadb4j;

import org.apache.commons.lang3.SystemUtils;

/**
 * OS Detector.
 *
 * <p>Includes a method for cross-platform tests to temporarily override OS to simulate running on another one.</p>
 *
 * @author <a href="https://www.vorburger.ch/">Michael Vorburger.ch</a>
 */
/* Intentionally package private, not public */
final class Platform implements AutoCloseable {

    enum OS { LINUX, MAC, WINDOWS }

    // TODO Introduce OTHER instead of falling back to LINUX
    private static final OS real = SystemUtils.IS_OS_WINDOWS ? OS.WINDOWS : SystemUtils.IS_OS_MAC ? OS.MAC : OS.LINUX;

    private static OS simulated = real;

    static OS get() {
        return simulated;
    }

    // https://errorprone.info/bugpattern/StaticAssignmentInConstructor
    @SuppressWarnings("StaticAssignmentInConstructor")
    Platform(OS simulated) {
        Platform.simulated = simulated;
    }

    @Override
    public void close() {
        simulated = real;
    }
}
