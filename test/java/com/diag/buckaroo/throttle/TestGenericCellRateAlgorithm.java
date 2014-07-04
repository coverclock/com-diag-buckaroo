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

import java.lang.Long;
import junit.framework.TestCase;
import com.diag.buckaroo.throttle.GenericCellRateAlgorithm;
import com.diag.buckaroo.throttle.Throttle;

public class TestGenericCellRateAlgorithm extends TestCase {
	
	void validateInitialState(Throttle gcra) {
		long ticks = 0;
		assertNotNull(gcra);
		gcra.reset(ticks);
		assertTrue(gcra.isValid());
		assertNotNull(gcra.toString());
		assertEquals(gcra.admissible(ticks), 0);
		assertFalse(gcra.isAlarmed());
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		assertEquals(gcra.admissible(ticks), 0);
		assertFalse(gcra.isAlarmed());
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		assertEquals(gcra.admissible(ticks), 0);
		assertFalse(gcra.isAlarmed());
		gcra.reset(ticks);
		assertFalse(gcra.isAlarmed());
		assertEquals(gcra.admissible(ticks), 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
	}

	public void test00Construction() {	
		
		long[] values = new long[] {
				Long.MIN_VALUE,
				-1L,
				0L,
				1L,
				1000L,
				1000000L,
				1000000000L,
				1000000000000L,
				1000000000000000L,
				1000000000000000000L,
				Long.MAX_VALUE
			};
		
		Throttle gcra = new GenericCellRateAlgorithm();
		System.out.println("gcra=" + gcra);
		validateInitialState(gcra);
		for (long increment : values) {
			gcra = new GenericCellRateAlgorithm(increment);
			System.out.println("i=" + increment + " gcra=" + gcra);
			validateInitialState(gcra);
			for (long limit : values)
			{
				gcra = new GenericCellRateAlgorithm(increment, limit);
				System.out.println("i=" + increment + " l=" + limit + " gcra=" + gcra);
				validateInitialState(gcra);
			}
		}
		
	}
	
	public void test01Time() {
		Throttle gcra = new GenericCellRateAlgorithm();
		assertNotNull(gcra);
		long hz = gcra.frequency();
		assertEquals(hz, 1000000L);
		long then = gcra.time();
		for (int ii = 0; ii < 1000; ++ii) {
			try { Thread.sleep(1); } catch (Exception ignore) { }
			long now = gcra.time();
			assertTrue(now > then);
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
		
		assertEquals(gcra.admissible(), 0);
		assertFalse(gcra.isAlarmed());
		
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		assertEquals(gcra.admissible(gcra.time()), 0);
		assertFalse(gcra.isAlarmed());
		
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		assertEquals(gcra.admissible(), 0);
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
		assertEquals(gcra.admissible(instantaneous), 0);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		assertEquals(gcra.admissible(instantaneous), 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		assertTrue(gcra.admissible(instantaneous) > 0);
		assertFalse(gcra.commit());
		assertTrue(gcra.isAlarmed());
		gcra.reset(instantaneous);
		
		assertEquals(gcra.admissible(instantaneous), 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
	}
	
	public void test05Increment() {
		
		Throttle gcra = new GenericCellRateAlgorithm(1, 0);
		assertNotNull(gcra);
		assertTrue(gcra.isValid());
		
		long now = gcra.time();
		assertEquals(gcra.admissible(now), 0);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		assertEquals(gcra.admissible(now), 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		long delay = gcra.admissible(now);
		assertEquals(delay, 1);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		for (int ii = 0; ii < 1000; ++ii) {
			
			now += delay;
			delay = gcra.admissible(now);
			assertEquals(delay, 0);
			assertTrue(gcra.commit());
			assertFalse(gcra.isAlarmed());
			
			delay = gcra.admissible(now);
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
		assertEquals(gcra.admissible(now), 0);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		assertEquals(gcra.admissible(now), 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		long delay = gcra.admissible(now);
		assertEquals(delay, 1000);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		for (int ii = 0; ii < 1000; ++ii) {
			
			now += delay;
			delay = gcra.admissible(now);
			assertEquals(delay, 0);
			assertTrue(gcra.commit());
			assertFalse(gcra.isAlarmed());
			
			delay = gcra.admissible(now);
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
		assertEquals(gcra.admissible(now), 0);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		assertEquals(gcra.admissible(now), 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		long delay = gcra.admissible(now);
		assertEquals(delay, 750);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		now += 900;
		delay = gcra.admissible(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		now += 900;
		delay = gcra.admissible(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		now += 900;
		delay = gcra.admissible(now);
		assertEquals(delay, 50);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		now += 50;
		delay = gcra.admissible(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		delay = gcra.admissible(now);
		assertEquals(delay, 1000);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		now += 1000;
		delay = gcra.admissible(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		now += 999;
		delay = gcra.admissible(now);
		assertEquals(delay, 1);
		assertTrue(gcra.rollback());
		assertFalse(gcra.isAlarmed());
		
		now += 1250;
		delay = gcra.admissible(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		now += 1000;
		delay = gcra.admissible(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		now += 750;
		delay = gcra.admissible(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		now += 1000;
		delay = gcra.admissible(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());
		
		delay = gcra.admissible(now);
		assertEquals(delay, 1000);
		assertFalse(gcra.commit());
		assertTrue(gcra.isAlarmed());
		
		now += 2000;
		delay = gcra.admissible(now);
		assertEquals(delay, 0);
		assertTrue(gcra.commit());
		assertFalse(gcra.isAlarmed());

	}
	
	public void test08Conversions() {
		
		assertEquals(GenericCellRateAlgorithm.ms2increment(0), 0L);
		assertEquals(GenericCellRateAlgorithm.ms2increment(1), 1000L);
		assertEquals(GenericCellRateAlgorithm.ms2increment(1000), 1000000L);
		assertEquals(GenericCellRateAlgorithm.ms2increment(1001), 1001000L);
		assertEquals(GenericCellRateAlgorithm.ms2increment(1999), 1999000L);
		assertEquals(GenericCellRateAlgorithm.ms2increment(2000), 2000000L);
		
		assertEquals(GenericCellRateAlgorithm.ms2limit(0), 0L);
		assertEquals(GenericCellRateAlgorithm.ms2limit(1), 1000L);
		assertEquals(GenericCellRateAlgorithm.ms2limit(1000), 1000000L);
		assertEquals(GenericCellRateAlgorithm.ms2limit(1001), 1001000L);
		assertEquals(GenericCellRateAlgorithm.ms2limit(1999), 1999000L);
		assertEquals(GenericCellRateAlgorithm.ms2limit(2000), 2000000L);
		
		assertEquals(GenericCellRateAlgorithm.ns2increment(0), 0L);
		assertEquals(GenericCellRateAlgorithm.ns2increment(1), 1L);
		assertEquals(GenericCellRateAlgorithm.ns2increment(1000), 1L);
		assertEquals(GenericCellRateAlgorithm.ns2increment(1001), 2L);
		assertEquals(GenericCellRateAlgorithm.ns2increment(1999), 2L);
		assertEquals(GenericCellRateAlgorithm.ns2increment(2000), 2L);
		
		assertEquals(GenericCellRateAlgorithm.ns2limit(0), 0L);
		assertEquals(GenericCellRateAlgorithm.ns2limit(1), 0L);
		assertEquals(GenericCellRateAlgorithm.ns2limit(1000), 1L);
		assertEquals(GenericCellRateAlgorithm.ns2limit(1001), 1L);
		assertEquals(GenericCellRateAlgorithm.ns2limit(1999), 1L);
		assertEquals(GenericCellRateAlgorithm.ns2limit(2000), 2L);

		assertEquals(GenericCellRateAlgorithm.delay2ms(0), 0L);
		assertEquals(GenericCellRateAlgorithm.delay2ms(1), 1L);
		assertEquals(GenericCellRateAlgorithm.delay2ms(1000), 1L);
		assertEquals(GenericCellRateAlgorithm.delay2ms(1001), 2L);
		assertEquals(GenericCellRateAlgorithm.delay2ms(1999), 2L);
		assertEquals(GenericCellRateAlgorithm.delay2ms(2000), 2L);
		
		assertEquals(GenericCellRateAlgorithm.delay2ms1(0), 0L);
		assertEquals(GenericCellRateAlgorithm.delay2ms1(1), 0L);
		assertEquals(GenericCellRateAlgorithm.delay2ms1(1000), 1L);
		assertEquals(GenericCellRateAlgorithm.delay2ms1(1001), 1L);
		assertEquals(GenericCellRateAlgorithm.delay2ms1(1999), 1L);
		assertEquals(GenericCellRateAlgorithm.delay2ms1(2000), 2L);
		
		assertEquals(GenericCellRateAlgorithm.delay2ns2(0), 0L);
		assertEquals(GenericCellRateAlgorithm.delay2ns2(1), 1000L);
		assertEquals(GenericCellRateAlgorithm.delay2ns2(1000), 0L);
		assertEquals(GenericCellRateAlgorithm.delay2ns2(1001), 1000L);
		assertEquals(GenericCellRateAlgorithm.delay2ns2(1999), 999000L);
		assertEquals(GenericCellRateAlgorithm.delay2ns2(2000), 0L);
		
	}
	
	public void test09Example1() {
		
		long increment = GenericCellRateAlgorithm.ms2increment(1000);
		long limit = GenericCellRateAlgorithm.ms2limit(250);
		Throttle gcra = new GenericCellRateAlgorithm(increment, limit);
		System.out.println("gcra=" + gcra);
		
		long then = System.currentTimeMillis();
		for (int ii = 0; ii < 20; ++ii) {
			long delay = GenericCellRateAlgorithm.delay2ms(gcra.admissible());
			while (delay > 0) {
				gcra.rollback();
				try { Thread.sleep(delay); } catch (Exception ignore) { }
				delay = GenericCellRateAlgorithm.delay2ms(gcra.admissible());
			}
			gcra.commit();
			assertFalse(gcra.isAlarmed());
			long now = System.currentTimeMillis();
			System.out.println("event=" + ii + " elapsed=" + (now - then) + "ms");
			then = now;
		}
	}
	
	public void test10Example2() {
		
		long increment = GenericCellRateAlgorithm.ns2increment(1000000000L);
		long limit = GenericCellRateAlgorithm.ns2limit(250000000L);
		Throttle gcra = new GenericCellRateAlgorithm(increment, limit);
		System.out.println("gcra=" + gcra);
		
		long then = System.currentTimeMillis();
		for (int ii = 0; ii < 20; ++ii) {
			long us = gcra.admissible();
			while (us > 0) {
				long ms = GenericCellRateAlgorithm.delay2ms1(us);
				int ns = GenericCellRateAlgorithm.delay2ns2(us);
				gcra.rollback();
				try { Thread.sleep(ms, ns); } catch (Exception ignore) { }
				us = gcra.admissible();
			}
			gcra.commit();
			assertFalse(gcra.isAlarmed());
			long now = System.currentTimeMillis();
			System.out.println("event=" + ii + " elapsed=" + (now - then) + "ms");
			then = now;
		}
	}

}
