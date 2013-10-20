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
package ch.vorburger.mariadb4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Enables passing in custom options when starting up the database server
 * This is the analog to my.cnf
 */
public class DBOptions {

	private String dataDirectory; // If non null, loads a specific database.  Otherwise, creates new temp databas
	private List<String> mysqldOptions; // Allows specifying command-line options to mysqld startup

	public DBOptions() {}

	public String getDataDirectory() {
		return dataDirectory;
	}

	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public void addMysqldOption(String optionName, String optionValue) {
		getMysqldOptions().add("--" + optionName + "=" + optionValue);
	}

	public List<String> getMysqldOptions() {
		if (mysqldOptions == null) {
			mysqldOptions = new ArrayList<String>();
		}
		return mysqldOptions;
	}

	public void setMysqldOptions(List<String> mysqldOptions) {
		this.mysqldOptions = mysqldOptions;
	}
}
