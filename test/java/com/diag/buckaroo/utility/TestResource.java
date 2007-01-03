/**
 * Copyright 2006 Digital Aggregates Corp., Arvada CO 80001-0597, USA.
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

import junit.framework.TestCase;
import java.io.FileNotFoundException;
import com.diag.buckaroo.utility.Resource;

public class TestResource extends TestCase {
	
	private final static String DRIVE = "C:";
	private final static String WINDOWS = "\\Documents and Settings\\jsloan\\Workspace2\\Buckaroo\\";
	private final static String POSIX = "/home/jsloan/src/Buckaroo";
	private final static String URL = "http://www.diag.com/ftp/China_Journal.pdf";
	
	public void testIsUrlLike() {
		assertNull(Resource.isUrlLike(null));
		assertNull(Resource.isUrlLike(""));
		assertNotNull(Resource.isUrlLike("file:./test/in/TestFileSupport.txt"));
		assertNull(Resource.isUrlLike("./test/in/TestFileSupport.txt"));
		assertNull(Resource.isUrlLike(WINDOWS + "test\\in\\TestFileSupport.txt"));
		assertNull(Resource.isUrlLike(".\\test\\in\\TestFileSupport.txt"));
		assertNull(Resource.isUrlLike(DRIVE + WINDOWS + "test\\in\\TestFileSupport.txt"));
		assertNull(Resource.isUrlLike(DRIVE + ".\\test\\in\\TestFileSupport.txt"));
		assertNull(Resource.isUrlLike(POSIX + "test/in/TestFileSupport.txt"));
		assertNull(Resource.isUrlLike("TestFileSupport.txt"));
		assertNotNull(Resource.isUrlLike(URL));
		assertNull(Resource.isUrlLike("java/lang/String.class"));
		assertNull(Resource.isUrlLike("com/diag/buckaroo/utility/TestPlatform.properties"));
	}
	
	public void testIsRelativePathLike() {
		assertNull(Resource.isRelativePathLike(null));
		assertNull(Resource.isRelativePathLike(""));
		assertNull(Resource.isRelativePathLike("file:./test/in/TestFileSupport.txt"));
		assertNotNull(Resource.isRelativePathLike("./test/in/TestFileSupport.txt"));
		assertNull(Resource.isRelativePathLike(WINDOWS + "test\\in\\TestFileSupport.txt"));
		assertNotNull(Resource.isRelativePathLike(".\\test\\in\\TestFileSupport.txt"));
		assertNull(Resource.isRelativePathLike(DRIVE + WINDOWS + "test\\in\\TestFileSupport.txt"));
		assertNotNull(Resource.isRelativePathLike(DRIVE + ".\\test\\in\\TestFileSupport.txt"));
		assertNull(Resource.isRelativePathLike(POSIX + "test/in/TestFileSupport.txt"));
		assertNull(Resource.isRelativePathLike("TestFileSupport.txt"));
		assertNull(Resource.isRelativePathLike(URL));
		assertNull(Resource.isRelativePathLike("java/lang/String.class"));
		assertNull(Resource.isRelativePathLike("com/diag/buckaroo/utility/TestPlatform.properties"));
	}

	public void testIsAbsolutePathLike() {
		assertNull(Resource.isAbsolutePathLike(null));
		assertNull(Resource.isAbsolutePathLike(""));
		assertNull(Resource.isAbsolutePathLike("file:./test/in/TestFileSupport.txt"));
		assertNull(Resource.isAbsolutePathLike("./test/in/TestFileSupport.txt"));
		assertNotNull(Resource.isAbsolutePathLike(WINDOWS + "test\\in\\TestFileSupport.txt"));
		assertNull(Resource.isAbsolutePathLike(".\\test\\in\\TestFileSupport.txt"));
		assertNotNull(Resource.isAbsolutePathLike(DRIVE + WINDOWS + "test\\in\\TestFileSupport.txt"));
		assertNull(Resource.isAbsolutePathLike(DRIVE + ".\\test\\in\\TestFileSupport.txt"));
		assertNotNull(Resource.isAbsolutePathLike(POSIX + "test/in/TestFileSupport.txt"));
		assertNull(Resource.isAbsolutePathLike("TestFileSupport.txt"));
		assertNull(Resource.isAbsolutePathLike(URL));
		assertNull(Resource.isAbsolutePathLike("java/lang/String.class"));
		assertNull(Resource.isAbsolutePathLike("com/diag/buckaroo/utility/TestPlatform.properties"));
	}

	public void testIsPathLike() {
		assertNull(Resource.isPathLike(null));
		assertNull(Resource.isPathLike(""));
		assertNull(Resource.isPathLike("file:./test/in/TestFileSupport.txt"));
		assertNotNull(Resource.isPathLike("./test/in/TestFileSupport.txt"));
		assertNotNull(Resource.isPathLike(WINDOWS + "test\\in\\TestFileSupport.txt"));
		assertNotNull(Resource.isPathLike(".\\test\\in\\TestFileSupport.txt"));
		assertNotNull(Resource.isPathLike(DRIVE + WINDOWS + "test\\in\\TestFileSupport.txt"));
		assertNotNull(Resource.isPathLike(DRIVE + ".\\test\\in\\TestFileSupport.txt"));
		assertNotNull(Resource.isPathLike(POSIX + "test/in/TestFileSupport.txt"));
		assertNull(Resource.isPathLike("TestFileSupport.txt"));
		assertNull(Resource.isPathLike(URL));
		assertNull(Resource.isPathLike("java/lang/String.class"));
		assertNull(Resource.isPathLike("com/diag/buckaroo/utility/TestPlatform.properties"));
	}

	public void testCreateInputStream() {
		try {
			assertNotNull(Resource.createInputStream("file:./test/in/TestFileSupport.txt"));
			assertNotNull(Resource.createInputStream("./test/in/TestFileSupport.txt"));
			assertNotNull(Resource.createInputStream(WINDOWS + "test\\in\\TestFileSupport.txt"));
			assertNotNull(Resource.createInputStream(".\\test\\in\\TestFileSupport.txt"));
			assertNotNull(Resource.createInputStream(DRIVE + WINDOWS + "test\\in\\TestFileSupport.txt"));
			assertNotNull(Resource.createInputStream(DRIVE + ".\\test\\in\\TestFileSupport.txt"));
			// assertNotNull(FileSupport.createInputStream(POSIX + "test/in/TestFileSupport.txt"));
			assertNotNull(Resource.createInputStream("TestFileSupport.txt"));
			assertNotNull(Resource.createInputStream(URL));
			assertNotNull(Resource.createInputStream("java/lang/String.class"));
			assertNotNull(Resource.createInputStream("com/diag/buckaroo/utility/TestPlatform.properties"));
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
			fail(exception.toString());
		}
		try {
			Resource.createInputStream(null);
			fail("null");
		} catch (FileNotFoundException ignore) {
		} catch (Exception exception) {
			fail(exception.toString());
		}
		try {
			Resource.createInputStream("FileNotFound.txt");
			fail("FileNotFound.txt");
		} catch (FileNotFoundException ignore) {
		} catch (Exception exception) {
			fail(exception.toString());
		}
		try {
			Resource.createInputStream("http://www.diag.com/ftp/HTTP440.pdf");
			fail("http://www.diag.com/ftp/HTTP440.pdf");
		} catch (FileNotFoundException ignore) {
		} catch (Exception exception) {
			fail(exception.toString());
		}
	}
}
