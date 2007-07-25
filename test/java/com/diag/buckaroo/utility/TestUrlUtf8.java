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

import junit.framework.TestCase;
import com.diag.buckaroo.utility.UrlUtf8;

public class TestUrlUtf8 extends TestCase {
	
	public void testOneInMany() {
		for (int codepoint = 0x000000; codepoint <= 0x10ffff; ++codepoint) {
			StringBuilder buffer = new StringBuilder();
			char[] array = Character.toChars(codepoint);
			buffer.append(array);
			String string = buffer.toString();
			String encoded = UrlUtf8.encode(string);
			assertNotNull(encoded);
			String decoded = UrlUtf8.decode(encoded);
			assertNotNull(decoded);
			assertEquals(string, decoded);
		}
	}
	
	public void testManyInOne() {
		StringBuilder buffer = new StringBuilder();
		for (int codepoint = 0x0000; codepoint <= 0xffff; ++codepoint)
		{
			char[] chars = Character.toChars(codepoint);
			buffer.append(chars);
		}
		String string = buffer.toString();
		String encoded = UrlUtf8.encode(string);
		assertNotNull(encoded);
		String decoded = UrlUtf8.decode(encoded);
		assertNotNull(decoded);
		assertEquals(string, decoded);
	}
}
