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
import com.diag.buckaroo.throttle.GeometricThrottle;
import com.diag.buckaroo.throttle.Throttle;

/*
 * N.B. This takes several minutes to run. Getting a cup of
 * coffee might be appropriate. Going to the Starbucks down
 * the street, maybe (of course there is one).
 */
public class TestGeometricThrottle extends TestCase {
	
	public void test00Construction() {
		
		Throttle gt = new GeometricThrottle();
		assertNotNull(gt);
		
		assertTrue(gt.isValid());
		assertFalse(gt.isAlarmed());
		assertNotNull(gt.toString());
		
	}
	
	public void test01Time() {
		
		Throttle gt = new GeometricThrottle();
		assertNotNull(gt);
		
		assertTrue(gt.isValid());
		assertTrue(gt.frequency() < 0);
		gt.time();
		
	}
	
	public void test02Admissability() {
		
		Throttle gt = new GeometricThrottle();
		assertNotNull(gt);
		
		assertEquals(gt.admissible(), 0L);
		assertTrue(gt.commit());
		assertFalse(gt.isAlarmed());
		
		assertEquals(gt.admissible(), 0L);
		assertTrue(gt.commit());
		assertFalse(gt.isAlarmed());
		
		assertTrue(gt.admissible() < 0L);
		assertTrue(gt.rollback());
		assertFalse(gt.isAlarmed());
		
		assertEquals(gt.admissible(), 0L);
		assertTrue(gt.commit());
		assertFalse(gt.isAlarmed());
		
		assertTrue(gt.admissible() < 0L);
		assertFalse(gt.commit());
		assertTrue(gt.isAlarmed());
		
		gt.reset();
		
		assertEquals(gt.admissible(), 0L);
		assertTrue(gt.commit());
		assertFalse(gt.isAlarmed());
		
	}
	
	private void cycle(Throttle gt)
	{
		long limit = 1073741825;
		long then = System.currentTimeMillis();
		long total = 0;
		long admitted = 0;
		long rejected2 = 0;
		long total2 = 1;
		while (true) {
			long rejected = 0;
			while (true) {
				assertTrue(gt.isValid());
				assertFalse(gt.isAlarmed());
				++total;
				if (total >= limit) { break; }
				else if (gt.admissible() == 0) { gt.commit(); ++admitted; break; }
				else { gt.rollback(); ++rejected; }
			}
			if (total >= limit) { break; }
			assertEquals(rejected2, rejected);
			if (admitted > 1) { rejected2 = (rejected2 * 2) + 1; }
			assertEquals(total2, total);
			total2 = total2 * 2;
			long now = System.currentTimeMillis();
			long elapsed = now - then;
			then = now;
			System.out.println(
				"elapsed=" + elapsed
				+ "ms rejected=" + rejected
				+ " admitted=" + admitted
				+ " total=" + total
				+ " gt=" + gt
			);
		}
		assertEquals(admitted, 30);
	}

	public void test03Example() {
		
		Throttle gt = new GeometricThrottle();
		assertNotNull(gt);

		cycle(gt);
		gt.reset();
		cycle(gt);
		
	}
}
