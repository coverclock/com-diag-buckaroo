/**
 * Copyright 2006-2013 Digital Aggregates Corporation, Colorado, USA.
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

import com.diag.buckaroo.jmx.Platform;
import com.diag.buckaroo.utility.Bean;

import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

public class TestPlatform extends TestCase {

	long delay;
	
	public void setUp() {
		String value = System.getProperty(this.getClass().getSimpleName());
		delay = (value != null) ? Long.parseLong(value) : 0;
	}
	
	public void test00Get() {
		Platform platform = new Platform();
		long ini = platform.getInitialHeapBytes();
		long use = platform.getUsedHeapBytes();
		long com = platform.getCommittedHeapBytes();
		long max = platform.getMaximumHeapBytes();
		assert ((ini <= use) && (use <= com) && (com <= max));
		System.out.println(ini + " " + use + " " + com + " " + max);
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

	public void test01MBeanLifeCycle() {
		try {
			Platform platform = new Platform();
			platform.start();
			validate(platform.getMBeanServer());
			platform.stop();
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
			fail(exception.toString());
		}
	}
	
	public void test02MBeanInfo() {
		Platform platform = new Platform();
		assertNotNull(platform.getMBeanName());
		assertNotNull(platform.getMBeanServer());
	}

	public void test02Bean() {
		System.out.println(Bean.toString(new Platform()));
	}

	public void test03MBeanServer() {
		try {
			System.out.println("-Dcom.sun.management.jmxremote");
			
			assertNotNull(new Platform());
			assertNotNull(new Platform(this.getClass()));
			Platform platform = new Platform(this);
			assertNotNull(platform);
			
			MBeanServer server = platform.getMBeanServer();
			assertNotNull(server);
					    
			ObjectName name = platform.getMBeanName();
			assertNotNull(name);
			
			platform.setMBeanName(name).setMBeanServer(server).start();
			
			validate(server);
			
			System.out.println("jconsole");
			
			System.out.println("begin");
			
			try { Thread.sleep(delay); } catch (Exception interrupted) {}
			
			System.out.println("end");
			
			platform.stop();
			
		} catch (Exception exception) {
			exception.printStackTrace(System.err);
			fail(exception.toString());
		}
	}
}
