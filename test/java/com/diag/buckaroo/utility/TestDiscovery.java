/**
 * Copyright 2006-2007 Digital Aggregates Corp., Arvada CO 80001-0597, USA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Name$
 *
 * $Id$
 */
package com.diag.buckaroo.utility;

import java.io.File;
import java.util.Date;

import com.diag.buckaroo.utility.Discovery;
import junit.framework.TestCase;

public class TestDiscovery extends TestCase {

	public void testGetResourceBundlePath() {
		String path = Discovery.getResourceBundlePath("com/diag/buckaroo/utility/TestDiscovery.properties");
		assertNotNull(path);
		System.out.println(path);
	}

	public void testGetClassFilePathClass() {
		String path = Discovery.getClassFilePath(TestDiscovery.class);
		assertNotNull(path);
		System.out.println(path);
		path = Discovery.getClassFilePath(String.class);
		assertNotNull(path);
		System.out.println(path);
	}

	public void testGetClassFilePathObject() {
		String path = Discovery.getClassFilePath(this);
		assertNotNull(path);
		System.out.println(path);
		path = Discovery.getClassFilePath(new String());
		assertNotNull(path);
		System.out.println(path);
	}

	public void testGetClassCompilationDateClass() {
		Date date = Discovery.getClassCompilationDate(TestDiscovery.class);
		assertNotNull(date);
		System.out.println(date);
		date = Discovery.getClassCompilationDate(String.class);
		assertNotNull(date);
		System.out.println(date);
	}

	public void testGetClassCompilationDateObject() {
		Date date = Discovery.getClassCompilationDate(this);
		assertNotNull(date);
		System.out.println(date);
		date = Discovery.getClassCompilationDate(new String());
		assertNotNull(date);
		System.out.println(date);
	}

	public void testGetClassLoaderClass() {
		ClassLoader loader = Discovery.getClassLoader(TestDiscovery.class);
		assertNotNull(loader);
		loader = Discovery.getClassLoader(String.class);
		assertNotNull(loader);
	}

	public void testGetClassLoaderObject() {
		ClassLoader loader = Discovery.getClassLoader(this);
		assertNotNull(loader);
		loader = Discovery.getClassLoader(new String());
		assertNotNull(loader);
		loader = Discovery.getClassLoader(null);
		assertNotNull(loader);
	}

	public void testGetCurrentWorkingDirectoryPath() {
		String path = Discovery.getCurrentWorkingDirectoryPath();
		assertNotNull(path);
		System.out.println(path);
	}
	
	public void testGetClassPathStrings() {
		String[] paths = Discovery.getClassPathStrings();
		assertNotNull(paths);
		assertTrue(paths.length > 0);
		for (String path : paths) {
			System.out.println(path);
		}
	}
	
	public void testGetClassPathFiles() {
		File[] files = Discovery.getClassPathFiles();
		assertNotNull(files);
		assertTrue(files.length > 0);
		for (File file : files) {
			System.out.println(file.toString());
		}
	}
}
