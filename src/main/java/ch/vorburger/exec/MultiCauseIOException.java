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

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * IOException with multiple causes.
 * 
 * @author Michael Vorburger
 */
public class MultiCauseIOException extends IOException {
	private static final long serialVersionUID = -2397961845533184622L;

	// Code review comments most welcome; I'm not sure I'm doing this right 100% right?
	// Doesn't something like this (or helpers for it) exist else? Couldn't find in commons-lang or Spring...

	protected List<IOException> causes = new LinkedList<IOException>();
	
	/**
	 * Add a Cause.
	 * Must be called at least once! (Otherwise why use this.)
	 * 
	 * @param message like the constructor argument of a Throwable 
	 * @param cause like the constructor argument of a Throwable
	 */
	public void add(String message, IOException cause) {
		causes.add(new IOException(message, cause));
	}

// May be safer not to do this - Exceptions from Exception may be more trouble than useful?
//	protected void checkUsage() throws IllegalStateException {
//		if (causes.isEmpty())
//			throw new IllegalStateException("Wrong usage of MultiCauseIOException in code; must call add() at least once before");
//	}
	
	public List<IOException> getCauses() {
		return causes;
	}

	@Override
	public String getMessage() {
		// checkUsage();
		StringBuilder message = new StringBuilder("MultiCauseIOException, causes: ");
		for (int i = 0; i < causes.size(); i++) {
			message.append(i);
			message.append(". ");
			message.append(causes.get(i).getMessage());
		}
		return message.toString();
	}


	@Override
	public void printStackTrace(PrintStream s) {
		s.println("MultiCauseIOException, stack traces: ");
		for (int i = 0; i < causes.size(); i++) {
			s.print(i);
			s.print(". ");
			causes.get(i).printStackTrace(s);
		}
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		s.println("MultiCauseIOException, stack traces: ");
		for (int i = 0; i < causes.size(); i++) {
			s.print(i);
			s.print(". ");
			causes.get(i).printStackTrace(s);
		}
	}

	// ---
	
	@Override
	public Throwable getCause() {
		throw new UnsupportedOperationException(); 
	}

	@Override
	public synchronized Throwable initCause(@SuppressWarnings("unused") Throwable cause) {
		throw new UnsupportedOperationException(); 
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		throw new UnsupportedOperationException(); 
	}

	@Override
	public void setStackTrace(@SuppressWarnings("unused") StackTraceElement[] stackTrace) {
		throw new UnsupportedOperationException(); 
	}
	
}
