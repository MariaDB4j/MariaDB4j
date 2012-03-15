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

import java.util.Locale;

/**
 * Util for Platform.
 * 
 * @author Michael Vorburger
 */
public class Platform {

	// This is better than org.apache.commons.exec.OS
	
	public enum Type {
		Windows("win32"), Linux("linux"), Mac("mac"), Solaris("solaris");
		private final String code;

		Type(String code) {
			this.code = code;
		}

		/**
		 * Platform Code.
		 * Useful for e.g. directory names of platform specific native applications.
		 */
		public String getCode() {
			return this.code;
		}
	}

	/**
	 * Gets the current Platform.
	 * 
	 * @return Type enumeration
	 * @throws UnknownPlatformException if unknown OS is encountered
	 */
	public static Type is() throws UnknownPlatformException {
		String os = System.getProperty("os.name").toLowerCase(Locale.US);
		if (os == null)
			throw new UnknownPlatformException("Java System Property os.name not set");
		// See here for possible values of os.name:
		// http://lopica.sourceforge.net/os.html
		if (os.contains("windows")) {
			return Type.Windows;
		} else if (os.contains("linux")) {
			return Type.Linux;
		} else if (os.contains("solaris") || os.contains("sunos")) {
			return Type.Solaris;
		} else if (os.contains("mac os x") || os.contains("darwin")) {
			return Type.Mac;
		} else {
			throw new UnknownPlatformException("Unknown Platform; Java System Property, os.name: " + os + ".");
		}
	}

	public static boolean is(Type type) {
		return is() == type;
	}

}
