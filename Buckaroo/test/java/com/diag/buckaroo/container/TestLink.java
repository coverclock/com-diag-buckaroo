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
package com.diag.buckaroo.container;

import junit.framework.TestCase;
import com.diag.buckaroo.container.Link;

public class TestLink extends TestCase {
	
	class Payload {
		int sn;
		public Payload(int sn) { this.sn = sn; }
		public int getSn() { return sn; }
	}
	
	Payload payloads[] = {
		new Payload(0),
		new Payload(1),
		new Payload(3),
		new Payload(4),
		new Payload(5)
	};
	
	Link links[] = {
		new Link<Payload>(payloads[0]),
		new Link<Payload>(payloads[1]),
		new Link<Payload>(payloads[2]),
		new Link<Payload>(payloads[3]),
		new Link<Payload>(payloads[4])
	};

	public void test00BasicSanity() {
		Link<Payload> link = new Link<Payload>();
		assertNotNull(link);
		Link<Payload> next = link.getNext();
		assertNotNull(next);
		assertEquals(next, link);
		Link<Payload> previous = link.getPrevious();
		assertNotNull(previous);
		assertEquals(previous, link);
		Link<Payload> root = link.getRoot();
		assertNotNull(root);
		assertEquals(root, link);
		assertTrue(link.isRoot());
		assertTrue(link.hasRoot(root));
		assertFalse(link.hasRoot(null));
		Link<Payload> link2 = new Link<Payload>();
		assertFalse(link.hasRoot(link2));
		assertFalse(link.isChained());
		assertTrue(link.isValid());
		assertNull(link.remove());
		assertNull(link.insert(link));
		assertNull(link.audit());
		assertNull(link.getPayload());
		Link<Payload> link3 = link.find(null);
		assertNotNull(link3);
		assertEquals(link3, link);
		Payload payload = new Payload(-1);
		assertNotNull(payload);
		assertEquals(payload.getSn(), -1);
		Link<Payload> link4 = link.find(payload);
		assertNull(link4);
		Link<Payload> link5 = link.setPayload(payload);
		assertNotNull(link5);
		assertEquals(link5, link);
		assertEquals(link.getPayload(), payload);
		Link<Payload> link6 = link.find(null);
		assertNull(link6);
		Link<Payload> link7 = link.find(payload);
		assertNotNull(link7);
		assertEquals(link7, link);
	}
	
}
