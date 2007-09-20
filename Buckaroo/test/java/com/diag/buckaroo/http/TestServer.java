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
package com.diag.buckaroo.http;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TestServer {

	@Test
	public void test00() {
		Server server = new Server();
		assertNotNull(server);
		assertEquals(server.getPort(), 80);
	}

	@Test
	public void test01() {
		Server server = new Server(8080);
		assertNotNull(server);
		assertEquals(server.getPort(), 8080);
	}

	@Test
	public void test02() {
		Server server = new Server();
		assertNotNull(server);
		assertEquals(server.getPort(), 80);
		Server server0 = server.start();
		assertEquals(server, server0);
		Server server1 = server.stop();
		assertEquals(server, server1);
	}

	@Test
	public void test03() {
		Server server = new Server();
		assertNotNull(server);
		Logger log = server.getLogger();
		assertNotNull(log);
		log.setLevel(Level.ALL);
		assertEquals(server.getPort(), 80);
		Server server0 = server.start();
		assertEquals(server, server0);
		try { Thread.sleep(60 * 1000); } catch (Exception interrupted) {}
		Server server1 = server.stop();
		assertEquals(server, server1);
		try { Thread.sleep(1000); } catch (Exception interrupted) {}
	}

	
}
