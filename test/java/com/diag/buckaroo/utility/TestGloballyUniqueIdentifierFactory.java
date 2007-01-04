/**
 * Copyright 2007 Digital Aggregates Corp., Arvada CO 80001-0597, USA.
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
import com.diag.buckaroo.utility.GloballyUniqueIdentifierFactory;

public class TestGloballyUniqueIdentifierFactory extends TestCase {
	
	final static byte[] SALT = { (byte)0x00, (byte)0x11, (byte)0x22, (byte)0x33, (byte)0x44, (byte)0x55, (byte)0x66, (byte)0x77 };
	final static int COUNT  = 8;
	final static char[] PASSWORD = { 'p', 'a', 's', 's', 'w', 'o', 'r', 'd' };

	GloballyUniqueIdentifierFactory guidf = new GloballyUniqueIdentifierFactory();
	GloballyUniqueIdentifierFactory guidf2 = new GloballyUniqueIdentifierFactory(SALT, COUNT, PASSWORD);

	public void test00() {
		assertNotNull(guidf);
		assertNotNull(guidf2);
	}
	
	public void test01() {
		String guid = guidf.create();
		assertNotNull(guid);
		assertTrue(guidf.isValid(guid));
		assertFalse(guidf2.isValid(guid));
		System.out.println("guid[" + guid.length() + "]=" + guid);
	}

	public void test02() {
		assertFalse(guidf.isValid(null));
		assertFalse(guidf2.isValid(null));
	}

	public void test03() {
		assertFalse(guidf.isValid(""));
		assertFalse(guidf2.isValid(""));
	}

	public void test04() {
		assertTrue(guidf.isValid("7e72bead537abe58c472b5d5a3e300ca91e9c13f429f87a5d3d7ffdec81039ed80a642d1c6026d9b31856b228877501d1ae43f8bb79a5dd1deccdffac787bb3b"));
		assertFalse(guidf2.isValid("7e72bead537abe58c472b5d5a3e300ca91e9c13f429f87a5d3d7ffdec81039ed80a642d1c6026d9b31856b228877501d1ae43f8bb79a5dd1deccdffac787bb3b"));
	}

	public void test05() {
		assertFalse(guidf.isValid("7e72bead537abe58c472b5d5a3e300ca91e9c13f429f87a5d3d7ffdec81039ed80a642d1c6026d9b31856b228877501d1ae43f8bb79a5dd1deccdffac787bb3c"));
		assertFalse(guidf2.isValid("7e72bead537abe58c472b5d5a3e300ca91e9c13f429f87a5d3d7ffdec81039ed80a642d1c6026d9b31856b228877501d1ae43f8bb79a5dd1deccdffac787bb3c"));
	}

	public void test06() {
		assertFalse(guidf.isValid("8e72bead537abe58c472b5d5a3e300ca91e9c13f429f87a5d3d7ffdec81039ed80a642d1c6026d9b31856b228877501d1ae43f8bb79a5dd1deccdffac787bb3b"));
		assertFalse(guidf2.isValid("8e72bead537abe58c472b5d5a3e300ca91e9c13f429f87a5d3d7ffdec81039ed80a642d1c6026d9b31856b228877501d1ae43f8bb79a5dd1deccdffac787bb3b"));
	}

	public void test07() {
		assertFalse(guidf.isValid("7e72bead537abe58c472b5d5a3e300ca91e9c13f429f87a5d3d7ffdec81039ed80a642d1c6026d9b31856b228877501d1ae43f8bb79a5dd1deccdffac787bb3"));
		assertFalse(guidf2.isValid("7e72bead537abe58c472b5d5a3e300ca91e9c13f429f87a5d3d7ffdec81039ed80a642d1c6026d9b31856b228877501d1ae43f8bb79a5dd1deccdffac787bb3"));
	}

	public void test08() {
		assertFalse(guidf.isValid("7e72bead537abe58c472b5d5a3e300ca91e9c13f429f87a5d3d7ffdec81039ed80a642d1c6026d9b31856b228877501d1ae43f8bb79a5dd1deccdffac787bb3bb"));
		assertFalse(guidf2.isValid("7e72bead537abe58c472b5d5a3e300ca91e9c13f429f87a5d3d7ffdec81039ed80a642d1c6026d9b31856b228877501d1ae43f8bb79a5dd1deccdffac787bb3bb"));
	}

	public void test09() {
		assertFalse(guidf.isValid("7e72bead537abe58c472b5d5a3e300ca91e9c13f429f87a5d3d7ffdec81039ed80a641d1c6026d9b31856b228877501d1ae43f8bb79a5dd1deccdffac787bb3b"));
		assertFalse(guidf2.isValid("7e72bead537abe58c472b5d5a3e300ca91e9c13f429f87a5d3d7ffdec81039ed80a641d1c6026d9b31856b228877501d1ae43f8bb79a5dd1deccdffac787bb3b"));
	}

	public void test10() {
		String prior = null;
		for (int ii = 0; ii < 1000; ++ii) {
			String guid = guidf2.create();
			assertNotNull(guid);
			assertTrue(guidf2.isValid(guid));
			assertFalse(guidf.isValid(guid));
			if (prior != null) { assertFalse(guid.equals(prior)); }
			prior = guid;
		}
	}
}
