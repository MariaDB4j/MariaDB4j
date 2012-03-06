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
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Utility to "unpack" files from the classpath into a (typically temporary) directory.
 * 
 * @author Michael Vorburger
 */
public class ClasspathUnpacker {

	// TODO Logging
	
	/**
	 * Extract stuff from a package on the classpath to a directory.
	 * 
	 * @param packagePath e.g. "com/stuff"
	 * @param toDir directory to extract to
	 * @return 
	 * @throws IOException if something goes wrong, including if nothing was found on classpath
	 */
	public static int extract(String packagePath, File toDir) throws IOException {
		toDir.mkdirs();
		String locationPattern = "classpath*:" + packagePath + "/**";
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(); 
		Resource[] resources = resourcePatternResolver.getResources(locationPattern);
		if (resources.length == 0) {
			throw new IOException("Nothing found at " + locationPattern);
		}
		int counter = 0;
		for (Resource resource : resources) {
			// skip directory Resource entries...
			if (!resource.isReadable())
				continue;
			
			URL url = resource.getURL();
			String path = url.toString();
			if (path.endsWith("/"))
				continue;
			
			int p = path.lastIndexOf(packagePath) + packagePath.length();
			path = path.substring(p);
			
			File targetFile = new File(toDir, path);
			long len = resource.contentLength();
			if (targetFile.exists() && targetFile.length() == len)
				continue;

			FileUtils.copyURLToFile(url, targetFile);
			++counter;
		}
		return counter;
	}
}
