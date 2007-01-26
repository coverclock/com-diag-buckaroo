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
package com.diag.buckaroo.jmx;

import com.diag.buckaroo.jmx.CallBack;
import com.diag.buckaroo.jmx.Counters;
import com.diag.buckaroo.utility.Bean;

import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

public class TestCounters extends TestCase {
	
	enum Counter {
		ONE,
		TWO,
		THREE,
		FOUR,
		FIVE
	}
	
	enum Error {
		SIX,
		SEVEN,
		EIGHT
	}
	
	long delay;
	Counters counters;
	Counters errors;
	int changing;
	String[] changed = new String[15];
	
	public class MyCallBack implements CallBack {
		Counters counters;
		public MyCallBack(Counters counters) { this.counters = counters; }
		public void callback(String name) {
			System.err.println(counters.getClass().getName()
				+ "[0x"
				+ Integer.toHexString(counters.hashCode())
				+ "]."
				+ name
				+ " changed");
			if (changing < changed.length) { changed[changing++] = name; }
		}
	}
	
	public void setUp() {
		String value = System.getProperty(this.getClass().getSimpleName());
		delay = (value != null) ? Long.parseLong(value) : 0;
		counters = new Counters(Counter.class);
		CallBack callback = new MyCallBack(counters);
		assertNull(counters.getCallBack());
		counters.setCallBack(callback);
		assertEquals(counters.getCallBack(), callback);
		errors = new Counters(Error.class);
	}
	
	public void test00OrdinalSanity() {
		assertEquals(Counter.ONE.ordinal(), 0);
		assertEquals(Counter.TWO.ordinal(), 1);
		assertEquals(Counter.THREE.ordinal(), 2);
		assertEquals(Counter.FOUR.ordinal(), 3);
		assertEquals(Counter.FIVE.ordinal(), 4);
	}
	
	public void test01StringSanity() {
		assertEquals(Counter.ONE.toString(), "ONE");
		assertEquals(Counter.TWO.toString(), "TWO");
		assertEquals(Counter.THREE.toString(), "THREE");
		assertEquals(Counter.FOUR.toString(), "FOUR");
		assertEquals(Counter.FIVE.toString(), "FIVE");
	}
	
	public void test02TypeSanity() {
		try
		{
			Class type1 = Counter.class;
			assertNotNull(type1);
			Class<? extends Enum> type2 = Counter.class;
			assertNotNull(type2);
			Class<? extends Enum<Counter>> type3 = Counter.class;
			assertNotNull(type3);
		}
		catch (Exception exception)
		{
			exception.printStackTrace(System.err);
			fail(exception.toString());
		}
	}
	
	private void validate5(long one, long two, long three, long four, long five) {
		assertEquals(counters.get(Counter.ONE), one);
		assertEquals(counters.get(Counter.TWO), two);
		assertEquals(counters.get(Counter.THREE), three);
		assertEquals(counters.get(Counter.FOUR), four);
		assertEquals(counters.get(Counter.FIVE), five);
	}
	
	public void test03CounterOperations() {
		validate5(0, 0, 0, 0, 0);
		counters.inc(Counter.ONE);
		counters.set(Counter.THREE, 3);
		counters.dec(Counter.FIVE);
		validate5(1, 0, 3, 0, -1);
		counters.add(Counter.TWO, 2);
		counters.clear(Counter.THREE);
		counters.add(Counter.FOUR, -4);
		validate5(1, 2, 0, -4, -1);
		counters.min(Counter.TWO, -4);
		counters.max(Counter.FOUR, 2);
		validate5(1, -4, 0, 2, -1);
		counters.max(Counter.ONE, -2);
		counters.min(Counter.FIVE, 2);
		validate5(1, -4, 0, 2, -1);
		counters.reset();
		validate5(0, 0, 0, 0, 0);
	}
	
	public void test04MBeanInfo() {
		assertNotNull(counters.getMBeanName());
		assertNotNull(counters.getMBeanServer());
		assertNotNull(counters.getMBeanInfo());
	}
	
	public void test05AttributeNotFoundException() {
		try {
			counters.getAttribute("FOO");
			fail("FOO");
		} catch (Exception exception) {
			assertNotNull(exception);
			assertTrue(exception instanceof AttributeNotFoundException);
		}
	}
	
	private void validate(Object that, String keyword, long value) {
		try {
			assertNotNull(that);
			assertTrue(that instanceof Attribute);
			Attribute attribute = (Attribute)that;
			String name = attribute.getName();
			assertNotNull(name);
			assertEquals(name, keyword);
			Object object = attribute.getValue();
			assertNotNull(object);
			assertTrue(object instanceof Long);
			assertEquals(((Long)object).longValue(), value);
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
			fail(exception.toString());
		}
	}
	
	private void validate(String keyword, long value) {
		try {
			Object object = counters.getAttribute(keyword);
			assertNotNull(object);
			assertTrue(object instanceof Long);
			assertEquals(((Long)object).longValue(), value);
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
			fail(exception.toString());
		}
	}
	
	public void test06GetSetAttribute() {
		long fortytwo = 42;
		changing = 0;
		changed[0] = null;
		Attribute attribute = new Attribute(Counter.THREE.toString(), new Long(fortytwo));
		try {
			counters.setAttribute(attribute);
		} catch (Exception exception) {
			fail(exception.toString());
		}
		validate(Counter.THREE.toString(), fortytwo);
		validate5(0, 0, fortytwo, 0, 0);
		assertEquals(changed[0], Counter.THREE.toString());
	}
	
	public void test07GetSetAttributes() {
		counters.set(Counter.ONE, 1);
		counters.set(Counter.TWO, 2);
		counters.set(Counter.THREE, 3);
		counters.set(Counter.FOUR, 4);
		counters.set(Counter.FIVE, 5);
		validate5(1, 2, 3, 4, 5);
		String[] names = {
			Counter.ONE.toString(),
			Counter.THREE.toString(),
			Counter.FIVE.toString()
		};
		AttributeList list = counters.getAttributes(names);
		assertNotNull(list);
		assertEquals(list.size(), 3);
		int[] found = { 0, 0, 0, 0, 0 };
		int[] index = { -1, -1, -1, -1, -1 };
		int total = 0;
		Counter[] candidate = {
			Counter.ONE,
			Counter.TWO,
			Counter.THREE,
			Counter.FOUR,
			Counter.FIVE
		};
		for (int ii = 0; ii < list.size(); ++ii) {
			Object object = list.get(ii);
			assertNotNull(object);
			assertTrue(object instanceof Attribute);
			Attribute attribute = (Attribute)object;
			for (int jj = 0; jj < candidate.length; ++jj)
			{
				Counter here = candidate[jj];
				assertEquals(here.ordinal(), jj);
				if (attribute.getName().equals(here.toString())) {
					++total;
					++found[jj];
					index[jj] = ii;
					validate(attribute, here.toString(), jj + 1);
				}
			}
		}
		assertEquals(total, 3);
		assertEquals(found[0], 1);
		assertEquals(found[1], 0);
		assertEquals(found[2], 1);
		assertEquals(found[3], 0);
		assertEquals(found[4], 1);
		assertEquals(index[0], 0);
		assertEquals(index[1], -1);
		assertEquals(index[2], 1);
		assertEquals(index[3], -1);
		assertEquals(index[4], 2);
		validate5(1, 2, 3, 4, 5);
		changing = 0;
		changed[0] = null;
		changed[1] = null;
		Attribute attribute = new Attribute(Counter.ONE.toString(), new Long(6));
		list.set(index[0], attribute);
		attribute = new Attribute(Counter.THREE.toString(), new Long(7));
		list.set(index[2], attribute);
		list = counters.setAttributes(list);
		assertNotNull(list);
		validate5(6, 2, 7, 4, 5);
		assertEquals(changed[0], Counter.ONE.toString());
		assertEquals(changed[1], Counter.THREE.toString());
	}
	
	public void test08Invoke() {
		try {
			changing = 0;
			changed[0] = null;
			changed[1] = null;
			changed[2] = null;
			changed[3] = null;
			changed[4] = null;
			counters.set(Counter.ONE, 1);
			counters.set(Counter.TWO, 22);
			counters.set(Counter.THREE, 333);
			counters.set(Counter.FOUR, 4444);
			counters.set(Counter.FIVE, 55555);
			validate5(1, 22, 333, 4444, 55555);
			String[] params = { };
			Object object = counters.invoke("reset", params, null);
			assertNull(object);
			validate5(0, 0, 0, 0, 0);
			assertEquals(changed[0], Counter.ONE.toString());
			assertEquals(changed[1], Counter.TWO.toString());
			assertEquals(changed[2], Counter.THREE.toString());
			assertEquals(changed[3], Counter.FOUR.toString());
			assertEquals(changed[4], Counter.FIVE.toString());
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
		Set mbeans = server.queryNames(null, null);
		assertNotNull(mbeans);
		for (Object object : mbeans) {
			assertTrue(object instanceof ObjectName);
			ObjectName mbeanname = (ObjectName)object;
			System.out.println(mbeanname);
		}
	}

	public void test09MBeanLifeCycle() {
		try {
			counters.start();
			validate(counters.getMBeanServer());
			counters.stop();
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
			fail(exception.toString());
		}
	}
	
	public void test10Bean() {
		System.out.println(Bean.toString(counters));
	}

	public void test11MBeanServer() {
		try {
			System.out.println("-Dcom.sun.management.jmxremote");
			
			MBeanServer server = counters.getMBeanServer();
			assertNotNull(server);
					    
			ObjectName name = counters.getMBeanName();
			assertNotNull(name);
			
			counters.setMBeanName(name).setMBeanServer(server).start();
			errors.start();
			
			validate(server);
			
			System.out.println("jconsole");
			
			try { Thread.sleep(delay); } catch (Exception interrupted) {}
			
			System.out.println("begin");
			
			for (int ii = 0; ii < 10; ++ii) {
				counters.inc(Counter.ONE);
				Thread.sleep(delay / 10);
				counters.dec(Counter.TWO);
				Thread.sleep(delay / 10);
				counters.set(Counter.THREE, ii);
				Thread.sleep(delay / 10);
				counters.add(Counter.FOUR, 3);
				Thread.sleep(delay / 10);
				counters.add(Counter.FIVE, -5);
				Thread.sleep(delay / 10);
			}
			
			System.out.println("end");
			
			try { Thread.sleep(delay); } catch (Exception interrupted) {}
			
			counters.stop();
			
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
			fail(exception.toString());
		}
	}
}
