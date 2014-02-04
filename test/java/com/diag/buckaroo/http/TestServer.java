/**
 * Copyright 2007-2013 Digital Aggregates Corporation, Colorado, USA.
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
package com.diag.buckaroo.http;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

public class TestServer {

	private int port = 8080;

	@Test
	public void test00() {
		Server server = new Server();
		assertNotNull(server);
		assertEquals(server.getPort(), port);
		assertNull(server.getRoot());
	}

	@Test
	public void test01() {
		Server server = new Server();
		assertNotNull(server);
		assertEquals(server.getPort(), port);
		assertNull(server.getRoot());
		Server server0 = server.setPort(8081);
		assertEquals(server, server0);
		assertEquals(server.getPort(), 8081);		
		Server server1 = server.setRoot("/Users/jsloan/Desktop/Home/www");
		assertEquals(server, server1);
		assertEquals(server.getRoot(),"/Users/jsloan/Desktop/Home/www");
		Server server2 = server.setRoot(null);
		assertEquals(server, server2);
		assertNull(server.getRoot());
		Server server3 = server.setPort(port);
		assertEquals(server, server3);
		assertEquals(server.getPort(), port);		
	}

	@Test
	public void test02() {
		Server server = new Server();
		assertNotNull(server);
		assertEquals(server.getPort(), port);
		assertNull(server.getRoot());
		Server server0 = server.start();
		assertEquals(server, server0);
		Server server1 = server.stop();
		assertEquals(server, server1);
	}

	@Test
	public void test03() {
		String value = System.getProperty(this.getClass().getSimpleName());
		long delay = (value != null) ? Long.parseLong(value) : 60000;
		Server server = new Server() {
			public boolean isDirectory(String name) throws FileNotFoundException {
				return name.equals("/synthesized.html") ? false : super.isDirectory(name);
			}
			protected void doFile(DataOutputStream output, String name) {
				if (!name.equals("/synthesized.html")) {
					super.doFile(output, name);
					return;
				}
				try {
					output.writeBytes(header(200, "text/html"));
					output.writeBytes("<HTML>");
					output.writeBytes("<BODY>");
					output.writeBytes("<A HREF=\"synthesized.html\">synthesized</A>");
					output.writeBytes("</BODY>");
					output.writeBytes("</HTML>");
				} catch (Exception exception) {
					log(exception);
				}
			}
		};
		assertNotNull(server);
		Logger log = server.getLogger();
		assertNotNull(log);
		assertEquals(server.getPort(), port);
		Server server1 = server.setRoot("/Users/jsloan/Desktop/Home/www");
		assertEquals(server, server1);
		assertEquals(server.getRoot(),"/Users/jsloan/Desktop/Home/www");
		Server server0 = server.start();
		assertEquals(server, server0);
		try { Thread.sleep(delay); } catch (Exception interrupted) {}
		Server server2 = server.stop();
		assertEquals(server, server2);
	}

	
}
