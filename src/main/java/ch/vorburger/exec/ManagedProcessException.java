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

/**
 * Exception thrown when unexpected stuff happens in ManagedProcess.
 * 
 * @author Michael Vorburger
 */
public class ManagedProcessException extends Exception {
	private static final long serialVersionUID = -5945369742058979996L;

	public ManagedProcessException(String message, Throwable cause) {
		super(message, cause);
	}

	public ManagedProcessException(String message) {
		super(message);
	}
	
}
