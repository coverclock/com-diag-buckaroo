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
import java.net.InetAddress;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import com.diag.buckaroo.jmx.Connector;

public class TestConnector extends TestCase {
	
	long delay;
	Connector connector;
	String hostname;
	
	public void setUp() {
		try {
			String value = System.getProperty(this.getClass().getSimpleName());
			delay = (value != null) ? Long.parseLong(value) : 0;
			connector = new Connector();
			InetAddress address = InetAddress.getLocalHost();
			hostname = address.getCanonicalHostName();
		} catch (Exception exception) {
			fail(exception.toString());
		}
	}
	
	private void flush(Logger logger) {
		Handler[] handlers = logger.getHandlers();
		for (Handler handler : handlers) {
			if (handler instanceof ConsoleHandler) {
				((ConsoleHandler)handler).flush();
				System.err.flush();
			}
		}
	}
	
	public void tearDown() {
		flush(connector.getLogger());
	}
	
	private void validate(String url) {
		Logger logger = connector.getLogger();
		assertNotNull(logger);
		String thisUrl = connector.toString();
		assertNotNull(thisUrl);
		assertEquals(thisUrl, url);
	}
	
	public void test00Default() {
		assertNotNull(connector);
		boolean result = connector.start();
		assertTrue(result);
		result = connector.start();
		assertFalse(result);
		validate("service:jmx:rmi://" + hostname + ":" + Connector.DEFAULT_JMX_PORT + "/jndi/rmi://" + hostname + ":" + Connector.DEFAULT_JNDI_PORT + "/" + Connector.DEFAULT_PATH);
		result = connector.stop();
		assertTrue(result);
		result = connector.stop();
		assertFalse(result);
	}
	
	public void test01Settors() {
		assertNotNull(connector);
		connector
			.setLogger(Logger.getLogger(this.getClass().getCanonicalName()))
			.setMBeanServer(ManagementFactory.getPlatformMBeanServer())
			.setPath("junit")
			.setJMXPort(32112)
			.setJNDIPort(32113)
			.setJNDIRebind(true);
		boolean result = connector.start();
		assertTrue(result);
		result = connector.start();
		assertFalse(result);
		validate("service:jmx:rmi://" + hostname + ":32112/jndi/rmi://" + hostname + ":32113/junit");
		result = connector.stop();
		assertTrue(result);
		result = connector.stop();
		assertFalse(result);
	}
	
	public void test02Suppression() {
		assertNotNull(connector);
		connector.setMBeanServer(ManagementFactory.getPlatformMBeanServer());
		connector.setPath("suppress");
		connector.setJMXPort(0);
		connector.setJNDIPort(0);
		boolean result = connector.start();
		assertFalse(result);
		assertNull(connector.toString());
		result = connector.start();
		assertFalse(result);
		result = connector.stop();
		assertFalse(result);
		result = connector.stop();
		assertFalse(result);
	}
	
	public void test03Properties1() {
		System.setProperty(Connector.PROPERTY_JMX_PORT, "32114");
		System.setProperty(Connector.PROPERTY_JNDI_PORT, "32115");
		System.setProperty(Connector.PROPERTY_JNDI_REBIND, "true");
		System.setProperty("com.sun.management.jmxremote.ssl", "true");
		System.setProperty("com.sun.management.jmxremote.authenticate", "true");
		System.setProperty("com.sun.management.jmxremote.password.file", "jmxremote.password");
		connector = new Connector();
		assertNotNull(connector);
		Logger logger = connector.getLogger();
		assertNotNull(logger);
		ConsoleHandler handler = new ConsoleHandler();
		assertNotNull(handler);
		logger.addHandler(handler);
		logger.setLevel(Level.FINE);
		assertEquals(logger.getLevel(), Level.FINE);
		assertTrue(logger.isLoggable(Level.FINE));
		logger.fine("FINE");
		logger.log(Level.FINE, "FINE");
		flush(logger);
		boolean result = connector.start();
		assertTrue(result);
		result = connector.start();
		assertFalse(result);
		validate("service:jmx:rmi://" + hostname + ":32114/jndi/rmi://" + hostname + ":32115/" + Connector.DEFAULT_PATH);
		result = connector.stop();
		assertTrue(result);
		result = connector.stop();
		assertFalse(result);
	}
	
	public void test04Properties2() {
		System.setProperty(Connector.PROPERTY_JMX_PORT, "32116");
		System.setProperty(Connector.PROPERTY_JNDI_PORT, "32117");
		System.setProperty("com.sun.management.jmxremote.authenticate", "true");
		connector = new Connector();
		assertNotNull(connector);
		Logger logger = connector.getLogger();
		assertNotNull(logger);
		ConsoleHandler handler = new ConsoleHandler();
		assertNotNull(handler);
		logger.addHandler(handler);
		logger.setLevel(Level.FINE);
		assertEquals(logger.getLevel(), Level.FINE);
		assertTrue(logger.isLoggable(Level.FINE));
		logger.fine("FINE");
		logger.log(Level.FINE, "FINE");
		flush(logger);
		boolean result = connector.start();
		assertTrue(result);
		result = connector.start();
		assertFalse(result);
		validate("service:jmx:rmi://" + hostname + ":32116/jndi/rmi://" + hostname + ":32117/" + Connector.DEFAULT_PATH);
		result = connector.stop();
		assertTrue(result);
		result = connector.stop();
		assertFalse(result);
	}
	
	public void test05Logger() {
		assertNotNull(connector);
		Logger logger0 = connector.getLogger();
		assertNotNull(logger0);
		Logger logger1 = Logger.getLogger(this.getClass().getCanonicalName());
		assertNotNull(logger1);
		connector.setLogger(logger1);
		Logger logger2 = connector.getLogger();
		assertNotNull(logger2);
		assertEquals(logger1, logger2);
	}

	public void test06JConsole() {
		System.out.println("-Dcom.sun.management.jmxremote");
		connector.setJMXPort(32118).setJNDIPort(32119).start();
		validate("service:jmx:rmi://" + hostname + ":32118/jndi/rmi://" + hostname + ":32119/" + Connector.DEFAULT_PATH);
		System.out.println("jconsole");
		System.out.println(connector.toString());
		System.out.println("begin");
		try { Thread.sleep(delay); } catch (Exception ignore) { }
		System.out.println("end");
		connector.stop();
	}
}
