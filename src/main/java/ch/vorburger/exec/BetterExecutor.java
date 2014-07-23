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
package ch.vorburger.exec;

import org.apache.commons.exec.DefaultExecutor;

/**
 * DefaultExecutor with fix for bad EXEC-69 bug.
 * 
 * @see https://issues.apache.org/jira/browse/EXEC-69
 * @see https://github.com/vorburger/MariaDB4j/issues/12
 */
public class BetterExecutor extends DefaultExecutor {

    @Override
    protected Thread createThread(Runnable runnable, String name) {
        Thread t = super.createThread(runnable, name);
        t.setDaemon(true);
        return t;
    }

}