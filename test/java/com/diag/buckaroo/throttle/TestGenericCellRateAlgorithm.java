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

import java.lang.Long;
import junit.framework.TestCase;
import com.diag.buckaroo.throttle.GenericCellRateAlgorithm;
import com.diag.buckaroo.throttle.Throttle;

public class TestGenericCellRateAlgorithm extends TestCase {

	public void test00Construction() {
		long[] increments = new long[] { Long.MIN_VALUE, 0, Long.MAX_VALUE };
		long[] limits = new long[] { Long.MIN_VALUE, 0, Long.MAX_VALUE };
		for (long increment : increments) {
			for (long limit : limits)
			{
				Throttle gcra = new GenericCellRateAlgorithm(increment, limit);
				assertNotNull(gcra);
				assertTrue(gcra.isValid());
				assertFalse(gcra.isAlarmed());
				System.out.println("i=" + increment + " l=" + limit + " gcra=" + gcra + "");
			}
		}
	}
	
	public void test01Time() {
		Throttle gcra = new GenericCellRateAlgorithm();
		assertNotNull(gcra);
		long hz = gcra.frequency();
		assertTrue(hz > 0);
		long then = 0;
		for (int ii = 0; ii < 1000; ++ii) {
			long now = gcra.time();
			assertTrue(now >= then);
			then = now;
			assertEquals(gcra.frequency(), hz);
		}
	}
	
	public void test02Sanity() {
		
		long increment = 1000;
		long limit = 250;
		
		Throttle gcra = new GenericCellRateAlgorithm(increment, limit);
		assertNotNull(gcra);
		assertTrue(gcra.isValid());
		assertNotNull(gcra.toString());
		
		assertEquals(gcra.admissable(), 0);
		assertFalse(gcra.isAlarmed());
		
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		assertEquals(gcra.admissable(gcra.time()), 0);
		assertFalse(gcra.isAlarmed());
		
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		assertEquals(gcra.admissable(), 0);
		assertFalse(gcra.isAlarmed());
		
		gcra.reset();
		assertFalse(gcra.isAlarmed());
		
		System.out.println("i=" + increment + " l=" + limit + " gcra=" + gcra + "");
		
	}
		
	public void test04Reset() {
		
		Throttle gcra = new GenericCellRateAlgorithm(1, 0);
		assertNotNull(gcra);
		assertTrue(gcra.isValid());
		
		long instantaneous = gcra.time();
		assertEquals(gcra.admissable(instantaneous), 0);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		assertEquals(gcra.admissable(instantaneous), 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		assertTrue(gcra.admissable(instantaneous) > 0);
		assertFalse(gcra.commit());
		assertTrue(gcra.isAlarmed());
		gcra.reset(instantaneous);
		
		assertEquals(gcra.admissable(instantaneous), 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
	}
	
	public void test05Increment() {
		
		Throttle gcra = new GenericCellRateAlgorithm(1, 0);
		assertNotNull(gcra);
		assertTrue(gcra.isValid());
		
		long now = gcra.time();
		assertEquals(gcra.admissable(now), 0);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		assertEquals(gcra.admissable(now), 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		long delay = gcra.admissable(now);
		assertEquals(delay, 1);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		for (int ii = 0; ii < 1000; ++ii) {
			
			now += delay;
			delay = gcra.admissable(now);
			assertEquals(delay, 0);
			assertTrue(gcra.commit());
			assertFalse(gcra.isAlarmed());
			
			delay = gcra.admissable(now);
			assertEquals(delay, 1);
			assertTrue(gcra.rollback());
			assertFalse(gcra.isAlarmed());
			
		}
	}
	
	public void test06Increment() {
		
		Throttle gcra = new GenericCellRateAlgorithm(1000, 0);
		assertNotNull(gcra);
		assertTrue(gcra.isValid());
		
		long now = gcra.time();
		assertEquals(gcra.admissable(now), 0);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		assertEquals(gcra.admissable(now), 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		long delay = gcra.admissable(now);
		assertEquals(delay, 1000);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		for (int ii = 0; ii < 1000; ++ii) {
			
			now += delay;
			delay = gcra.admissable(now);
			assertEquals(delay, 0);
			assertTrue(gcra.commit());
			assertFalse(gcra.isAlarmed());
			
			delay = gcra.admissable(now);
			assertEquals(delay, 1000);
			assertTrue(gcra.rollback());
			assertFalse(gcra.isAlarmed());
			
		}
	}
	
	public void test07Limit() {
		
		Throttle gcra = new GenericCellRateAlgorithm(1000, 250);
		assertNotNull(gcra);
		assertTrue(gcra.isValid());
		
		long now = gcra.time();
		assertEquals(gcra.admissable(now), 0);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		assertEquals(gcra.admissable(now), 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		long delay = gcra.admissable(now);
		assertEquals(delay, 750);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		now += 900;
		delay = gcra.admissable(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		now += 900;
		delay = gcra.admissable(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		now += 900;
		delay = gcra.admissable(now);
		assertEquals(delay, 50);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		now += 50;
		delay = gcra.admissable(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		delay = gcra.admissable(now);
		assertEquals(delay, 1000);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		now += 1000;
		delay = gcra.admissable(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		now += 999;
		delay = gcra.admissable(now);
		assertEquals(delay, 1);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		now += 1250;
		delay = gcra.admissable(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		now += 1000;
		delay = gcra.admissable(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		now += 750;
		delay = gcra.admissable(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		now += 1000;
		delay = gcra.admissable(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		delay = gcra.admissable(now);
		assertEquals(delay, 1000);
		assertFalse(gcra.commit());
		assertTrue(gcra.isAlarmed());
		
		now += 2000;
		delay = gcra.admissable(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());

	}
	
	static long us2Ms(long us) { return (us + 1000 - 1) / 1000; }
	
	static long ms2Us(long ms) { return ms * 1000; }
	
	public void test07RealTimeCareful() {
		
		Throttle gcra = new GenericCellRateAlgorithm(ms2Us(1000), ms2Us(250));
		System.out.println("gcra=" + gcra);
		
		long then = System.currentTimeMillis();
		for (int ii = 0; ii < 20; ++ii) {
			long delay = us2Ms(gcra.admissable());
			while (delay > 0) {
				gcra.rollback();
				try { Thread.sleep(delay); } catch (Exception ignore) { }
				delay = us2Ms(gcra.admissable());
			}
			gcra.commit();
			if (gcra.isAlarmed()) { System.err.println("alarm!"); }
			long now = System.currentTimeMillis();
			System.out.println("event=" + ii + " elapsed=" + (now - then) + "ms");
			then = now;
		}
	}
	
	public void test08RealTimeCareless() {
		
		Throttle gcra = new GenericCellRateAlgorithm(ms2Us(1000), ms2Us(250));
		System.out.println("gcra=" + gcra);
		
		long then = System.currentTimeMillis();
		for (int ii = 0; ii < 20; ++ii) {
			long delay = us2Ms(gcra.admissable());
			if (delay > 0) {
				gcra.rollback();
				try { Thread.sleep(delay); } catch (Exception ignore) { }
				us2Ms(gcra.admissable());
			}
			gcra.commit();
			if (gcra.isAlarmed()) { System.err.println("alarm!"); }
			long now = System.currentTimeMillis();
			System.out.println("event=" + ii + " elapsed=" + (now - then) + "ms");
			then = now;
		}
	}

}
