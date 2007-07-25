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

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import junit.framework.TestCase;
import com.diag.buckaroo.jmx.LifeCycle;

public class TestLifeCycle extends TestCase {
	
	long delay;
	LifeCycle lifecycle;
	
	public void setUp() {
		String value = System.getProperty(this.getClass().getSimpleName());
		delay = (value != null) ? Long.parseLong(value) : 0;
		lifecycle = new LifeCycle();
	}
	
	public void test00LifeCycle() {
		assertNotNull(lifecycle.getLogger());
		assertNotNull(lifecycle.getMBeanName());
		assertNotNull(lifecycle.getMBeanServer());
	}
	
	public void test01GetMBeanName() {
		ObjectName name = lifecycle.getMBeanName();
		assertNotNull(name);
		System.out.println(name);
	}

	public void test02SetMBeanNameObjectName() {
		try {
			ObjectName first = new ObjectName("junit.framework:type=TestLifeCycle,name=TestLifeCycle");
			assertNotNull(first);
			LifeCycle result = lifecycle.setMBeanName(first);
			assertNotNull(result);
			assertEquals(result, lifecycle);
			ObjectName second = lifecycle.getMBeanName();
			assertNotNull(second);
			assertEquals(second, first);
			System.out.println(second);
		} catch (Exception exception) {
			fail(exception.toString());
		}
	}

	public void test03SetMBeanNameClass() {
		try {
			LifeCycle result = lifecycle.setMBeanName(this.getClass());
			assertNotNull(result);
			assertEquals(result, lifecycle);
			ObjectName second = lifecycle.getMBeanName();
			assertNotNull(second);
			System.out.println(second);
		} catch (Exception exception) {
			fail(exception.toString());
		}
	}

	public void test04SetMBeanNameObject() {
		try {
			LifeCycle result = lifecycle.setMBeanName(this);
			assertNotNull(result);
			assertEquals(result, lifecycle);
			ObjectName second = lifecycle.getMBeanName();
			assertNotNull(second);
			System.out.println(second);
		} catch (Exception exception) {
			fail(exception.toString());
		}
	}

	public void test05SetMBeanNameString() {
		try {
			LifeCycle result = lifecycle.setMBeanName("TestLifeCycle");
			assertNotNull(result);
			assertEquals(result, lifecycle);
			ObjectName second = lifecycle.getMBeanName();
			assertNotNull(second);
			System.out.println(second);
		} catch (Exception exception) {
			fail(exception.toString());
		}
	}

	public void tes06tSetLogger() {
		Logger first = lifecycle.getLogger();
		assertNotNull(first);
		Logger second = Logger.getLogger(this.getClass().getName());
		assertNotNull(second);
		LifeCycle result = lifecycle.setLogger(second);
		assertNotNull(result);
		assertEquals(result, lifecycle);
		Logger third = lifecycle.getLogger();
		assertNotNull(third);
		assertEquals(third, second);
	}

	public void test07SetMBeanServer() {
		MBeanServer first = lifecycle.getMBeanServer();
		assertNotNull(first);
		MBeanServer second = ManagementFactory.getPlatformMBeanServer();
		assertNotNull(second);
		LifeCycle result = lifecycle.setMBeanServer(second);
		assertNotNull(result);
		assertEquals(result, lifecycle);
		MBeanServer third = lifecycle.getMBeanServer();
		assertNotNull(third);
		assertEquals(third, second);

	}
	
	public interface ValueMBean {
		public int getValue();
		public void setValue(int value);
	}
	
	public class Value extends LifeCycle implements ValueMBean {
		
		int value = 0;
		
		public int getValue() {
			getLogger().info("getValue()=" + value);
			return value;
		}
		
		public void setValue(int value) {
			getLogger().info("setValue(" + value + ")");
			this.value = value;
		}
	}

	public void test08StartStop() {
		System.out.println("-Dcom.sun.management.jmxremote");
		Value value = new Value();
		value.start();
		System.out.println("jconsole");
		System.out.println("begin");
		try { Thread.sleep(delay); } catch (Exception ignore) { }
		System.out.println("end");
		value.stop();
	}
}
