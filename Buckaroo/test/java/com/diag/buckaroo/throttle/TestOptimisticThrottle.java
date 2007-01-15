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
package com.diag.buckaroo.throttle;

import junit.framework.TestCase;
import com.diag.buckaroo.throttle.OptimisticThrottle;
import com.diag.buckaroo.throttle.Throttle;

public class TestOptimisticThrottle extends TestCase {

	public void test00Construction() {
		Throttle ot = new OptimisticThrottle();
		assertTrue(ot.isValid());
		assertFalse(ot.isAlarmed());
		assertNotNull(ot.toString());
	}
	
	public void test01Time() {
		Throttle ot = new OptimisticThrottle();
		assertNotNull(ot);
		assertTrue(ot.isValid());
		assertTrue(ot.frequency() < 0);
		ot.time();
	}

	public void test02Admissibility() {
		Throttle ot = new OptimisticThrottle();
		assertNotNull(ot);
		assertTrue(ot.isValid());
		assertEquals(ot.admissible(), 0);
		assertTrue(ot.commit());
		assertFalse(ot.isAlarmed());
		assertEquals(ot.admissible(ot.time()), 0);
		assertTrue(ot.rollback());
		assertFalse(ot.isAlarmed());
		ot.reset();
		assertEquals(ot.admissible(ot.time()), 0);
		assertTrue(ot.commit());
		assertFalse(ot.isAlarmed());
		ot.reset(ot.time());
		assertEquals(ot.admissible(), 0);
		assertTrue(ot.rollback());
		assertFalse(ot.isAlarmed());
		assertNotNull(ot.toString());
	}
	
}
