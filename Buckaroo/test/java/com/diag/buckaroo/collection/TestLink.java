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
package com.diag.buckaroo.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.diag.buckaroo.collection.Link;
import com.diag.buckaroo.collection.Link.Functor;

public class TestLink extends TestCase {
	
	class Payload {
		int sn;
		public Payload(int sn) { this.sn = sn; }
		public int getSn() { return sn; }
	}
	
	/**
	 * This Link Functor audits every Link in a chain starting with the specified Link
	 * and traversing forwards.
	 * @param link refers to the Link on which to start.
	 * @return the reference to the Link.
	 */
	static public Link<Payload> audit(Link<Payload> link) {
		return link.apply(
				new Functor<Payload>() {
					
					Link<Payload> origin;
					
					public Link<Payload> evaluate(Link<Payload> that) {
						if (origin == null) { origin = that; }
						if (!that.isValid()) { return that; }
						that = that.getNext();
						if (that == origin) { return null; }
						return that;
					}
					
				}
		);
	}
	
	/**
	 * This Link Functor traverses forward on a chain starting with the specified Link
	 * looking for the first Link whose payload refers to the specified Payload.
	 * @param link refers to the Link on which to start.
	 * @param payload refers to the Payload in question.
	 * @return a reference to the first Link containing a reference to the Payload, or null
	 * if not found.
	 */
	public Link<Payload> find(Link<Payload> link, Payload payload) {
		return link.apply(
				new Link.Functor<Payload>() {
					
					Link<Payload> origin;
					Payload payload;
					
					public Functor<Payload> setTarget(Payload payload) {
						this.payload = payload;
						return this;
					}
					
					public Link<Payload> evaluate(Link<Payload> that) {
						if (origin == null) { origin = that; }
						if (that.getPayload() == payload) { return that; }
						that = that.getNext();
						if (that == origin) { return null; }
						return that;
					}
					
				}.setTarget(payload)
		);
	}

	public void test00CtorSettorsGettors() {
		
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
		assertNull(audit(link));
		assertNull(link.getPayload());
		
		Link<Payload> link3 = find(link, null);
		assertNotNull(link3);
		assertEquals(link3, link);
		
		Payload payload = new Payload(-1);
		assertNotNull(payload);
		assertEquals(payload.getSn(), -1);
		Link<Payload> link4 = find(link, payload);
		assertNull(link4);
		
		Link<Payload> link5 = link.setPayload(payload);
		assertNotNull(link5);
		assertEquals(link5, link);
		assertEquals(link.getPayload(), payload);
		
		Link<Payload> link6 = find(link, null);
		assertNull(link6);
		
		Link<Payload> link7 = find(link, payload);
		assertNotNull(link7);
		assertEquals(link7, link);
		
	}
	
	public void test01InsertRootApplyRemove() {
		
		// Link 0
		
		Payload payload0 = new Payload(0);
		Link<Payload> link0 = new Link<Payload>(payload0);
		assertNotNull(link0);
		assertTrue(link0.isValid());
		
		assertFalse(link0.isChained());
		assertTrue(link0.isRoot());
		assertTrue(link0.hasRoot(link0));
		
		assertNull(link0.remove());
		assertNull(link0.insert(link0));
		
		assertTrue(link0.isValid());
		assertNull(audit(link0));
		assertNotNull(link0.getRoot());
		assertEquals(link0.getRoot(), link0);
		assertEquals(link0.getNext(), link0);
		assertEquals(link0.getPrevious(), link0);
		assertNotNull(link0.getPayload());
		assertEquals(link0.getPayload().getSn(), 0);
		
		// Insert Link 1 after Link 0
		
		Payload payload1 = new Payload(1);
		Link<Payload> link1 = new Link<Payload>(payload1);
		assertNotNull(link1);
		assertTrue(link1.isValid());
		
		assertFalse(link1.isChained());
		assertTrue(link1.isRoot());
		assertTrue(link1.hasRoot(link1));
		
		assertNull(link1.remove());
		assertEquals(link1.insert(link0), link1);
		assertNull(link0.insert(link0));
		assertNull(link0.insert(link1));
		assertNull(link1.insert(link0));
		assertNull(link1.insert(link1));
		
		assertTrue(link0.isValid());
		assertNull(audit(link0));
		assertNotNull(link0.getRoot());
		assertEquals(link0.getRoot(), link0);
		assertEquals(link0.getNext(), link1);
		assertEquals(link0.getPrevious(), link1);
		assertNotNull(link0.getPayload());
		assertEquals(link0.getPayload().getSn(), 0);
		
		assertTrue(link0.isChained());
		assertTrue(link0.isRoot());
		assertTrue(link0.hasRoot(link0));
		
		assertTrue(link1.isValid());
		assertNull(audit(link1));
		assertNotNull(link1.getRoot());
		assertEquals(link1.getRoot(), link0);
		assertEquals(link1.getNext(), link0);
		assertEquals(link1.getPrevious(), link0);
		assertNotNull(link1.getPayload());
		assertEquals(link1.getPayload().getSn(), 1);
		
		assertTrue(link1.isChained());
		assertFalse(link1.isRoot());
		assertTrue(link1.hasRoot(link0));
		
		// Insert Link 2 after Link 1
		
		Payload payload2 = new Payload(2);
		Link<Payload> link2 = new Link<Payload>(payload2);
		assertNotNull(link2);
		assertTrue(link2.isValid());
		
		assertFalse(link2.isChained());
		assertTrue(link2.isRoot());
		assertTrue(link2.hasRoot(link2));
		
		assertNull(link2.remove());
		assertEquals(link2.insert(link1), link2);
		assertNull(link0.insert(link0));
		assertNull(link0.insert(link1));
		assertNull(link0.insert(link2));
		assertNull(link1.insert(link0));
		assertNull(link1.insert(link1));
		assertNull(link1.insert(link2));
		assertNull(link2.insert(link0));
		assertNull(link2.insert(link1));
		assertNull(link2.insert(link2));
		
		assertTrue(link0.isValid());
		assertNull(audit(link0));
		assertNotNull(link0.getRoot());
		assertEquals(link0.getRoot(), link0);
		assertEquals(link0.getNext(), link1);
		assertEquals(link0.getPrevious(), link2);
		assertNotNull(link0.getPayload());
		assertEquals(link0.getPayload().getSn(), 0);
		
		assertTrue(link0.isChained());
		assertTrue(link0.isRoot());
		assertTrue(link0.hasRoot(link0));
		
		assertTrue(link1.isValid());
		assertNull(audit(link1));
		assertNotNull(link1.getRoot());
		assertEquals(link1.getRoot(), link0);
		assertEquals(link1.getNext(), link2);
		assertEquals(link1.getPrevious(), link0);
		assertNotNull(link1.getPayload());
		assertEquals(link1.getPayload().getSn(), 1);
		
		assertTrue(link1.isChained());
		assertFalse(link1.isRoot());
		assertTrue(link1.hasRoot(link0));
		
		assertTrue(link2.isValid());
		assertNull(audit(link2));		
		assertNotNull(link2.getRoot());
		assertEquals(link2.getRoot(), link0);
		assertEquals(link2.getNext(), link0);
		assertEquals(link2.getPrevious(), link1);
		assertNotNull(link2.getPayload());
		assertEquals(link2.getPayload().getSn(), 2);
		
		assertTrue(link2.isChained());
		assertFalse(link2.isRoot());
		assertTrue(link2.hasRoot(link0));
		
		// Find all Payloads
		
		assertEquals(find(link0, payload0), link0);
		assertEquals(find(link1, payload0), link0);
		assertEquals(find(link2, payload0), link0);
		assertEquals(find(link0, payload1), link1);
		assertEquals(find(link1, payload1), link1);
		assertEquals(find(link2, payload1), link1);
		assertEquals(find(link0, payload2), link2);
		assertEquals(find(link1, payload2), link2);
		assertEquals(find(link2, payload2), link2);
		
		// Root chain from Link 0 to Link 1
		
		assertNull(audit(link0));
		assertNull(audit(link1));
		assertNull(audit(link2));
		
		assertTrue(link0.isRoot());
		assertTrue(link0.hasRoot(link0));
		assertFalse(link0.hasRoot(link1));
		assertFalse(link0.hasRoot(link2));
		assertFalse(link1.isRoot());
		assertTrue(link1.hasRoot(link0));
		assertFalse(link1.hasRoot(link1));
		assertFalse(link1.hasRoot(link2));
		assertFalse(link2.isRoot());
		assertTrue(link2.hasRoot(link0));
		assertFalse(link2.hasRoot(link1));
		assertFalse(link2.hasRoot(link2));
		
		assertEquals(link0.getRoot(), link0);
		assertEquals(link1.getRoot(), link0);
		assertEquals(link2.getRoot(), link0);
		assertEquals(link1.root(), link1);
		assertEquals(link0.getRoot(), link1);
		assertEquals(link1.getRoot(), link1);
		assertEquals(link2.getRoot(), link1);
		
		assertFalse(link0.isRoot());
		assertFalse(link0.hasRoot(link0));
		assertTrue(link0.hasRoot(link1));
		assertFalse(link0.hasRoot(link2));
		assertTrue(link1.isRoot());
		assertFalse(link1.hasRoot(link0));
		assertTrue(link1.hasRoot(link1));
		assertFalse(link1.hasRoot(link2));
		assertFalse(link2.isRoot());
		assertFalse(link2.hasRoot(link0));
		assertTrue(link2.hasRoot(link1));
		assertFalse(link2.hasRoot(link2));
		
		assertNull(audit(link0));
		assertNull(audit(link1));
		assertNull(audit(link2));
		
		// Remove Link 1

		assertEquals(link1.remove(), link1);
		assertNull(link1.remove());
		
		assertTrue(link0.isValid());
		assertNull(audit(link0));
		assertNotNull(link0.getRoot());
		assertEquals(link0.getRoot(), link1);
		assertEquals(link0.getNext(), link2);
		assertEquals(link0.getPrevious(), link2);
		assertNotNull(link0.getPayload());
		assertEquals(link0.getPayload().getSn(), 0);
		
		assertTrue(link0.isChained());
		assertFalse(link0.isRoot());
		assertTrue(link0.hasRoot(link1));
		
		assertTrue(link1.isValid());
		assertNull(audit(link1));
		assertNotNull(link1.getRoot());
		assertEquals(link1.getRoot(), link1);
		assertEquals(link1.getNext(), link1);
		assertEquals(link1.getPrevious(), link1);
		assertNotNull(link1.getPayload());
		assertEquals(link1.getPayload().getSn(), 1);
		
		assertFalse(link1.isChained());
		assertTrue(link1.isRoot());
		assertTrue(link1.hasRoot(link1));
		
		assertTrue(link2.isValid());
		assertNull(audit(link2));		
		assertNotNull(link2.getRoot());
		assertEquals(link2.getRoot(), link1);
		assertEquals(link2.getNext(), link0);
		assertEquals(link2.getPrevious(), link0);
		assertNotNull(link2.getPayload());
		assertEquals(link2.getPayload().getSn(), 2);
		
		assertTrue(link2.isChained());
		assertFalse(link2.isRoot());
		assertTrue(link2.hasRoot(link1));
		
		// Re-insert Link 1 after Link 0
		
		assertEquals(link1.insert(link0), link1);
		
		assertTrue(link0.isValid());
		assertNull(audit(link0));
		assertNotNull(link0.getRoot());
		assertEquals(link0.getRoot(), link1);
		assertEquals(link0.getNext(), link1);
		assertEquals(link0.getPrevious(), link2);
		assertNotNull(link0.getPayload());
		assertEquals(link0.getPayload().getSn(), 0);
		
		assertTrue(link0.isChained());
		assertFalse(link0.isRoot());
		assertTrue(link0.hasRoot(link1));
		
		assertTrue(link1.isValid());
		assertNull(audit(link1));
		assertNotNull(link1.getRoot());
		assertEquals(link1.getRoot(), link1);
		assertEquals(link1.getNext(), link2);
		assertEquals(link1.getPrevious(), link0);
		assertNotNull(link1.getPayload());
		assertEquals(link1.getPayload().getSn(), 1);
		
		assertTrue(link1.isChained());
		assertTrue(link1.isRoot());
		assertTrue(link1.hasRoot(link1));
		
		assertTrue(link2.isValid());
		assertNull(audit(link2));		
		assertNotNull(link2.getRoot());
		assertEquals(link2.getRoot(), link1);
		assertEquals(link2.getNext(), link0);
		assertEquals(link2.getPrevious(), link1);
		assertNotNull(link2.getPayload());
		assertEquals(link2.getPayload().getSn(), 2);
		
		assertTrue(link2.isChained());
		assertFalse(link2.isRoot());
		assertTrue(link2.hasRoot(link1));
		
		// Re-remove Link 1
		
		assertEquals(link1.remove(), link1);
		
		assertTrue(link0.isValid());
		assertNull(audit(link0));
		assertNotNull(link0.getRoot());
		assertEquals(link0.getRoot(), link1);
		assertEquals(link0.getNext(), link2);
		assertEquals(link0.getPrevious(), link2);
		assertNotNull(link0.getPayload());
		assertEquals(link0.getPayload().getSn(), 0);
		
		assertTrue(link0.isChained());
		assertFalse(link0.isRoot());
		assertTrue(link0.hasRoot(link1));
		
		assertTrue(link1.isValid());
		assertNull(audit(link1));
		assertNotNull(link1.getRoot());
		assertEquals(link1.getRoot(), link1);
		assertEquals(link1.getNext(), link1);
		assertEquals(link1.getPrevious(), link1);
		assertNotNull(link1.getPayload());
		assertEquals(link1.getPayload().getSn(), 1);
		
		assertFalse(link1.isChained());
		assertTrue(link1.isRoot());
		assertTrue(link1.hasRoot(link1));
		
		assertTrue(link2.isValid());
		assertNull(audit(link2));		
		assertNotNull(link2.getRoot());
		assertEquals(link2.getRoot(), link1);
		assertEquals(link2.getNext(), link0);
		assertEquals(link2.getPrevious(), link0);
		assertNotNull(link2.getPayload());
		assertEquals(link2.getPayload().getSn(), 2);
		
		assertTrue(link2.isChained());
		assertFalse(link2.isRoot());
		assertTrue(link2.hasRoot(link1));
		
		// Find all Payloads
		
		assertEquals(find(link0, payload0), link0);
		assertNull(find(link1, payload0));
		assertEquals(find(link2, payload0), link0);
		assertNull(find(link0, payload1));
		assertEquals(find(link1, payload1), link1);
		assertNull(find(link2, payload1));
		assertEquals(find(link0, payload2), link2);
		assertNull(find(link1, payload2));
		assertEquals(find(link2, payload2), link2);
		
		// Remove Link 0

		assertEquals(link0.remove(), link0);
		assertNull(link0.remove());
		assertNull(link2.remove());
		
		assertTrue(link0.isValid());
		assertNull(audit(link0));
		assertNotNull(link0.getRoot());
		assertEquals(link0.getRoot(), link0);
		assertEquals(link0.getNext(), link0);
		assertEquals(link0.getPrevious(), link0);
		assertNotNull(link0.getPayload());
		assertEquals(link0.getPayload().getSn(), 0);
		
		assertFalse(link0.isChained());
		assertTrue(link0.isRoot());
		assertTrue(link0.hasRoot(link0));
		
		assertTrue(link1.isValid());
		assertNull(audit(link1));
		assertNotNull(link1.getRoot());
		assertEquals(link1.getRoot(), link1);
		assertEquals(link1.getNext(), link1);
		assertEquals(link1.getPrevious(), link1);
		assertNotNull(link1.getPayload());
		assertEquals(link1.getPayload().getSn(), 1);
		
		assertFalse(link1.isChained());
		assertTrue(link1.isRoot());
		assertTrue(link1.hasRoot(link1));
		
		assertTrue(link2.isValid());
		assertNull(audit(link2));		
		assertNotNull(link2.getRoot());
		assertEquals(link2.getRoot(), link2);
		assertEquals(link2.getNext(), link2);
		assertEquals(link2.getPrevious(), link2);
		assertNotNull(link2.getPayload());
		assertEquals(link2.getPayload().getSn(), 2);
		
		assertFalse(link2.isChained());
		assertTrue(link2.isRoot());
		assertTrue(link2.hasRoot(link2));
		
		// Find all Payloads
		
		assertEquals(find(link0, payload0), link0);
		assertNull(find(link1, payload0));
		assertNull(find(link2, payload0));
		assertNull(find(link0, payload1));
		assertEquals(find(link1, payload1), link1);
		assertNull(find(link2, payload1));
		assertNull(find(link0, payload2));
		assertNull(find(link1, payload2));
		assertEquals(find(link2, payload2), link2);
		
	}
	
	public void test02Iterator() {
		
		Payload payload0 = new Payload(0);
		Link<Payload> link0 = new Link<Payload>(payload0);
		
		Payload payload1 = new Payload(1);
		Link<Payload> link1 = new Link<Payload>(payload1);
		
		Payload payload2 = new Payload(2);
		Link<Payload> link2 = new Link<Payload>(payload2);
		
		link2.insert(link1.insert(link0));
		
		int sn = 0;
		Iterator<Link<Payload>> it0 = link2.getRoot().iterator();
		while (it0.hasNext()) {
			Link<Payload> link = it0.next();
			Payload payload = link.getPayload();
			assertEquals(payload.getSn(), sn++);
		}
		assertEquals(sn, 3);
		
		try {
			it0.next();
			fail();
		} catch (NoSuchElementException ignore) {
		} catch (Exception exception) {
			fail(exception.toString());
		}
		
		Iterator<Link<Payload>> it1 = link2.getRoot().iterator();
		sn = 0;
		assertTrue(link0.isChained());
		assertTrue(link1.isChained());
		assertTrue(link2.isChained());
		while (it1.hasNext()) {
			Link<Payload> link = it1.next();
			Payload payload = link.getPayload();
			assertEquals(payload.getSn(), sn++);
			it1.remove();
			if (sn == 1) {
				assertFalse(link0.isChained());
				assertTrue(link1.isChained());
				assertTrue(link2.isChained());
			} else if (sn == 2) {
				assertFalse(link0.isChained());
				assertFalse(link1.isChained());
				assertFalse(link2.isChained());
			}
			else if (sn == 3) {
				assertFalse(link0.isChained());
				assertFalse(link1.isChained());
				assertFalse(link2.isChained());
			} else {
				fail();
			}
			try {
				it1.remove();
				fail();
			} catch (IllegalStateException ignore) {
			} catch (Exception exception) {
				fail(exception.toString());
			}
		}
		assertEquals(sn, 3);
		
	}
	
	public void test03EnhancedFor() {
		
		Payload payload0 = new Payload(0);
		Link<Payload> link0 = new Link<Payload>(payload0);
		
		Payload payload1 = new Payload(1);
		Link<Payload> link1 = new Link<Payload>(payload1);
		
		Payload payload2 = new Payload(2);
		Link<Payload> link2 = new Link<Payload>(payload2);
		
		link2.insert(link1.insert(link0));
		
		int sn = 0;
		for (Link<Payload> link : link0) {
			Payload payload = link.getPayload();
			assertEquals(payload.getSn(), sn++);
		}
		assertEquals(sn, 3);

	}
}
