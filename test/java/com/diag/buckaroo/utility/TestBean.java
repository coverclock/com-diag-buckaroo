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

import com.diag.buckaroo.utility.Bean;
import junit.framework.TestCase;

public class TestBean extends TestCase {

	private class MyBean {
		public boolean getBoolean() { return true; }
		public int getInteger() { return 1; }
		public long getLong() { return 2L; }
		public float getFloat() { return (float)3.0; }
		public double getDouble() { return (double)4.0; }
		public char getChar() { return '5'; }
		public String getString() { return "six"; }
		public MyBean getBean() { return this; }
		public int getInvalid(int invalid) { return 8; }
		public String GetInvalid() { return "9"; }
	}
	
	private class NonBean {
		public String toString() { return "Stuff"; }
	}
	
	private class StringBean {
		private String string = "10";
		public String toString() { return Bean.toString(this); }
		public String getString() { return string; }
		public StringBean setString(String string) { this.string = string; return this; }
	}
	
	public void test00() {
		System.out.println("Bean=" + Bean.toString(new MyBean()));
	}
	
	public void test01() {
		System.out.println("NonBean=" + Bean.toString(new NonBean()));
	}
	
	public void test02() {
		System.out.println("StringBean=" + new StringBean());
	}

}
