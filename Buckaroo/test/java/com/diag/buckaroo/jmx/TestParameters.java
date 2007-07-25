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
package com.diag.buckaroo.jmx;

import com.diag.buckaroo.jmx.CallBack;
import com.diag.buckaroo.jmx.Parameters;
import com.diag.buckaroo.utility.Bean;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

public class TestParameters extends TestCase {
	
	long delay;
	Parameters parameters;
	int changing;
	String[] changed = new String[15];
	
	public class MyCallBack implements CallBack {
		Parameters parameters;
		public MyCallBack(Parameters parameters) { this.parameters = parameters; }
		public void callback(String name) {
			System.err.println(parameters.getClass().getName()
					+ "[0x"
					+ Integer.toHexString(parameters.hashCode())
					+ "]."
					+ name
					+ " changed");
			if (changing < changed.length) { changed[changing++] = name; }
		}
	}
	
	public void setUp() throws FileNotFoundException, IOException {
		String value = System.getProperty(this.getClass().getSimpleName());
		delay = (value != null) ? Long.parseLong(value) : 0;
		Properties properties = new Properties();
		InputStream stream = new FileInputStream("./test/in/TestParameters.properties");
		properties.load(stream);
		parameters = new Parameters(properties);
		CallBack callback = new MyCallBack(parameters);
		assertNull(parameters.getCallBack());
		parameters.setCallBack(callback);
		assertEquals(parameters.getCallBack(), callback);
	}
	
	private void validate1(String keyword, String expected) {
		String actual = parameters.get(keyword);
		if (expected != null) { assertEquals(actual, expected); } else { assertNull(actual); }
	}
	
	private void validate6(String one, String two, String three, String four, String five, String six) {
		validate1("one", one);
		validate1("two", two);
		validate1("three", three);
		validate1("four", four);
		validate1("five", five);
		validate1("fix", six);
	}
	
	public void test00Get() {
		validate6("One", null, "Three", null, "Five", null);
	}
	
	public void test01Set() {
		String one = parameters.get("one");
		assertNotNull(one);
		assertEquals(one, "One");
		String two = parameters.get("two");
		assertNull(two);
		two = parameters.set("two", "Two");
		assertNull(two);
		two = parameters.get("two");
		assertNotNull(two);
		assertEquals(two, "Two");
		String three = parameters.get("three");
		assertNotNull(three);
		assertEquals(three, "Three");
		three = parameters.set("three", "3");
		assertNotNull(three);
		assertEquals(three, "Three");
		three = parameters.set("three", "Three");
		assertNotNull(three);
		assertEquals(three, "3");
		three = parameters.get("three");
		assertNotNull(three);
		assertEquals(three, "Three");
		String four = parameters.get("four");
		assertNull(four);
		String five = parameters.get("five");
		assertNotNull(five);
		assertEquals(five, "Five");
		five = parameters.remove("five");
		assertNotNull(five);
		assertEquals(five, "Five");
		five = parameters.get("five");
		assertNull(five);
		five = parameters.set("five", "5");
		assertNull(five);
		five = parameters.get("five");
		assertNotNull(five);
		assertEquals(five, "5");
		String six = parameters.get("six");
		assertNull(six);
	}
		
	public void test02MBeanInfo() {
		assertNotNull(parameters.getMBeanName());
		assertNotNull(parameters.getMBeanServer());
		assertNotNull(parameters.getMBeanInfo());
	}
	
	public void test03AttributeNotFoundException() {
		try {
			parameters.getAttribute("two");
			fail("two");
		} catch (Exception exception) {
			assertNotNull(exception);
			assertTrue(exception instanceof AttributeNotFoundException);
		}
	}
	
	private void validate(Object candidate, String keyword, String value) {
		try {
			assertNotNull(candidate);
			assertTrue(candidate instanceof Attribute);
			Attribute attribute = (Attribute)candidate;
			String name = attribute.getName();
			assertNotNull(name);
			assertEquals(name, keyword);
			Object object = attribute.getValue();
			assertNotNull(object);
			assertTrue(object instanceof String);
			assertEquals((String)object, value);
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
			fail(exception.toString());
		}
	}
	
	private void validate(String keyword, String value) {
		try {
			Object object = parameters.getAttribute(keyword);
			assertNotNull(object);
			assertTrue(object instanceof String);
			assertEquals((String)object, value);
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
			fail(exception.toString());
		}
	}

	public void test04GetAttribute() {
		validate("one", "One");
		validate("three", "Three");
		validate("five", "Five");
	}
	
	public void test05SetAttribute() {
		Attribute attribute = new Attribute("three", "3");
		changing = 0;
		changed[0] = null;
		try {
			parameters.setAttribute(attribute);
		} catch (Exception exception) {
			fail(exception.toString());
		}
		validate("three", "3");
		validate6("One", null, "3", null, "Five", null);
		assertEquals(changing, 1);
		assertEquals(changed[0], "three");
	}
	
	public void test06GetSetAttributes() {
		validate6("One", null, "Three", null, "Five", null);
		String[] names = {
			"one",
			"three",
			"five"
		};
		AttributeList list = parameters.getAttributes(names);
		assertNotNull(list);
		assertEquals(list.size(), 3);
		int[] found = { 0, 0, 0, 0, 0, 0};
		int[] index = { -1, -1, -1, -1, -1, -1 };
		int total = 0;
		String[] candidate = {
			"one",
			"two",
			"three",
			"four",
			"five",
			"six"
		};
		String[] expected = {
				"One",
				null,
				"Three",
				null,
				"Five",
				null
			};
		for (int ii = 0; ii < list.size(); ++ii) {
			Object object = list.get(ii);
			assertNotNull(object);
			assertTrue(object instanceof Attribute);
			Attribute attribute = (Attribute)object;
			for (int jj = 0; jj < candidate.length; ++jj)
			{
				String here = candidate[jj];
				assertNotNull(here);
				if (attribute.getName().equals(here)) {
					++total;
					++found[jj];
					index[jj] = ii;
					validate(attribute, here, expected[jj]);
				}
			}
		}
		assertEquals(total, 3);
		assertEquals(found[0], 1);
		assertEquals(found[1], 0);
		assertEquals(found[2], 1);
		assertEquals(found[3], 0);
		assertEquals(found[4], 1);
		assertEquals(found[5], 0);
		assertEquals(index[0], 0);
		assertEquals(index[1], -1);
		assertEquals(index[2], 1);
		assertEquals(index[3], -1);
		assertEquals(index[4], 2);
		assertEquals(index[5], -1);
		validate6("One", null, "Three", null, "Five", null);
		changing = 0;
		changed[0] = null;
		changed[1] = null;
		Attribute attribute1 = new Attribute("one", "1");
		list.set(index[0], attribute1);
		Attribute attribute5 = new Attribute("five", "Five");
		list.set(index[4], attribute5);
		Attribute attribute4 = new Attribute("four", "4");
		list.add(attribute4);
		list = parameters.setAttributes(list);
		assertNotNull(list);
		validate6("1", null, "Three", "4", "Five", null);
		assertEquals(changing, 2);
		assertEquals(changed[0], "one");
		assertEquals(changed[1], "four");
	}

	public void test07Invoke() {
		try {
			changing = 0;
			changed[0] = null;
			String[] getNine = { "nine" };
			Object object = parameters.invoke("get", getNine, null);
			assertNull(object);
			String[] getThree = { "three" };
			object = parameters.invoke("get", getThree, null);
			assertTrue(object instanceof String);
			assertEquals((String)object, "Three");
			String[] setNine = { "nine", "Nine" };
			object = parameters.invoke("set", setNine, null);
			assertNull(object);
			String result = parameters.get("nine");
			assertNotNull(result);
			assertEquals(result, "Nine");
			String[] setThree = { "three", "3" };
			object = parameters.invoke("set", setThree, null);
			assertNotNull(object);
			assertTrue(object instanceof String);
			assertEquals((String)object, "Three");
			result = parameters.get("three");
			assertNotNull(result);
			assertEquals(result, "3");
			String[] removeNine = { "nine" };
			object = parameters.invoke("remove", removeNine, null);
			assertNotNull(object);
			assertTrue(object instanceof String);
			assertEquals((String)object, "Nine");
			object = parameters.invoke("get", getNine, null);
			assertNull(object);
			validate6("One", null, "3", null, "Five", null);
			assertEquals(changing, 3);
			assertEquals(changed[0], "nine");
			assertEquals(changed[1], "three");
			assertEquals(changed[2], "nine");
		} catch (Exception exception) {
			fail(exception.toString());
		}
	}
	
	private void validate(MBeanServer server) {
		System.out.println("SERVER:");
		assertNotNull(server);
		System.out.println("DEFAULTDOMAIN:");
		String defaultdomain = server.getDefaultDomain();
		assertNotNull(defaultdomain);
		System.out.println(defaultdomain);
		System.out.println("DOMAINS:");
		String[] domains = server.getDomains();
		assertNotNull(domains);
		for (String string : domains) {
			assertNotNull(string);
			System.out.println(string);
		}
		System.out.println("OBJECTNAMES:");
		Set<ObjectName> mbeans = server.queryNames(null, null);
		assertNotNull(mbeans);
		for (Object object : mbeans) {
			assertTrue(object instanceof ObjectName);
			ObjectName mbeanname = (ObjectName)object;
			System.out.println(mbeanname);
		}
	}

	public void test08MBeanLifeCycle() {
		try {
			parameters.start();
			validate(parameters.getMBeanServer());
			parameters.stop();
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
			fail(exception.toString());
		}
	}
	
	public void test09Bean() {
		System.out.println(Bean.toString(parameters));
	}

	public void test10MBeanServer() {
		try {
			System.out.println("-Dcom.sun.management.jmxremote");
			
			MBeanServer server = parameters.getMBeanServer();
			assertNotNull(server);
					    
			ObjectName name = parameters.getMBeanName();
			assertNotNull(name);
			
			parameters.setMBeanName(name).setMBeanServer(server).start();
			
			validate(server);
			
			System.out.println("jconsole");
			
			try { Thread.sleep(delay); } catch (Exception interrupted) {}
			
			System.out.println("begin");
			
			for (int ii = 0; ii < 10; ++ii) {
				parameters.set("two", Integer.toString(ii * 3));
				Thread.sleep(delay / 10);
				parameters.remove("three");
				Thread.sleep(delay / 10);
				parameters.set("five", Integer.toString(ii * 5));
				Thread.sleep(delay / 10);
				parameters.set("three", Integer.toString(ii * 7));
				Thread.sleep(delay / 10);
			}
			
			System.out.println("end");
			
			try { Thread.sleep(delay); } catch (Exception interrupted) {}
			
			parameters.stop();
			
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
			fail(exception.toString());
		}
	}
}
