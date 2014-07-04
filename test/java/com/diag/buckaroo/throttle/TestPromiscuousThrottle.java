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
package com.diag.buckaroo.throttle;

import junit.framework.TestCase;
import com.diag.buckaroo.throttle.ExtendedThrottle;
import com.diag.buckaroo.throttle.PromiscuousThrottle;
import com.diag.buckaroo.throttle.Throttle;

public class TestPromiscuousThrottle extends TestCase {

	public void test00Construction() {
		Throttle pt = new PromiscuousThrottle();
		assertTrue(pt.isValid());
		assertFalse(pt.isAlarmed());
		assertNotNull(pt.toString());
		System.out.println("pt=" + pt);
	}
	
	public void test01Time() {
		Throttle pt = new PromiscuousThrottle();
		assertNotNull(pt);
		assertTrue(pt.isValid());
		assertEquals(pt.frequency(), 0);
		pt.time();
	}

	public void test02Admissibility() {
		ExtendedThrottle pt = new PromiscuousThrottle();
		assertNotNull(pt);
		assertTrue(pt.isValid());
		assertEquals(pt.admissible(), 0);
		assertTrue(pt.commit());
		assertFalse(pt.isAlarmed());
		assertEquals(pt.admissible(pt.time()), 0);
		assertTrue(pt.rollback());
		assertFalse(pt.isAlarmed());
		pt.reset();
		assertEquals(pt.admissible(pt.time()), 0);
		assertTrue(pt.commit(1));
		assertFalse(pt.isAlarmed());
		pt.reset(pt.time());
		assertEquals(pt.admissible(), 0);
		assertTrue(pt.rollback());
		assertFalse(pt.isAlarmed());
		assertNotNull(pt.toString());
	}
	
}
