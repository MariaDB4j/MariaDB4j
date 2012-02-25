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
package ch.vorburger.mariadb4j.internal;

import ch.vorburger.mariadb4j.MariaDB4jException;

/**
 * Util for Platform.
 * 
 * @author Michael Vorburger
 */
public class Platform {

	public enum Type {
		Windows("win32"), Linux("linux"), Mac("mac"), Solaris("solaris");
		private final String code;

		Type(String code) {
			this.code = code;
		}

		public String getCode() {
			return this.code;
		}
	}

	public static Type is() throws MariaDB4jException {
		String os = System.getProperty("os.name");
		// See here for possible values of os.name:
		// http://lopica.sourceforge.net/os.html
		if (os.startsWith("Windows")) {
			return Type.Windows;
		} else if ("Linux".equals(os)) {
			return Type.Linux;
		} else if ("Solaris".equals(os) || "SunOS".equals(os)) {
			return Type.Solaris;
		} else if ("Mac OS X".equals(os) || "Darwin".equals(os)) {
			return Type.Mac;
		} else {
			throw new MariaDB4jException("Sorry, MariaDB4j doesn't support the '" + os + "' OS.");
		}
	}

	public static boolean is(Type type) throws MariaDB4jException {
		return is() == type;
	}

}
