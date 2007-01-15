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
import java.util.Random;
import junit.framework.TestCase;
import com.diag.buckaroo.throttle.BandwidthAlgorithm;
import com.diag.buckaroo.throttle.Throttle;

public class TestBandwidthAlgorithm extends TestCase {
	
	void validateInitialState(Throttle ba) {
		long ticks = 0;
		assertNotNull(ba);
		ba.reset(ticks);
		assertTrue(ba.isValid());
		assertNotNull(ba.toString());
		assertEquals(ba.admissible(ticks), 0);
		assertFalse(ba.isAlarmed());
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		assertEquals(ba.admissible(ticks), 0);
		assertFalse(ba.isAlarmed());
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		assertEquals(ba.admissible(ticks), 0);
		assertFalse(ba.isAlarmed());
		ba.reset(ticks);
		assertFalse(ba.isAlarmed());
		assertEquals(ba.admissible(ticks), 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
	}

	public void test00Construction() {	
		
		long[] increments = new long[] { Long.MIN_VALUE, 0, Long.MAX_VALUE };
		long[] limits = new long[] { Long.MIN_VALUE, 0, Long.MAX_VALUE };
		
		Throttle ba = new BandwidthAlgorithm();
		System.out.println("ba=" + ba);
		validateInitialState(ba);
		for (long increment : increments) {
			ba = new BandwidthAlgorithm(increment);
			System.out.println("i=" + increment + " ba=" + ba);
			validateInitialState(ba);
			for (long limit : limits)
			{
				ba = new BandwidthAlgorithm(increment, limit);
				System.out.println("i=" + increment + " l=" + limit + " ba=" + ba);
				validateInitialState(ba);
			}
		}
		
	}
	
	public void test01Time() {
		Throttle ba = new BandwidthAlgorithm();
		assertNotNull(ba);
		long hz = ba.frequency();
		assertEquals(hz, 1000000000L);
		long then = ba.time();
		for (int ii = 0; ii < 1000; ++ii) {
			try { Thread.sleep(0, 1); } catch (Exception ignore) { }
			long now = ba.time();
			long elapsed = now - then;
			assertTrue(elapsed > 0);
			then = now;
			assertEquals(ba.frequency(), hz);
		}
	}
	
	public void test02Sanity() {
		
		long increment = 1000;
		long limit = 250;
		
		Throttle ba = new BandwidthAlgorithm(increment, limit);
		assertNotNull(ba);
		assertTrue(ba.isValid());
		assertNotNull(ba.toString());
		
		assertEquals(ba.admissible(), 0);
		assertFalse(ba.isAlarmed());
		
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		
		assertEquals(ba.admissible(ba.time()), 0);
		assertFalse(ba.isAlarmed());
		
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		
		assertEquals(ba.admissible(), 0);
		assertFalse(ba.isAlarmed());
		
		ba.reset();
		assertFalse(ba.isAlarmed());
		
		System.out.println("i=" + increment + " l=" + limit + " ba=" + ba + "");
		
	}
		
	public void test04Reset() {
		
		Throttle ba = new BandwidthAlgorithm(1, 0);
		assertNotNull(ba);
		assertTrue(ba.isValid());
		
		long instantaneous = ba.time();
		assertEquals(ba.admissible(instantaneous), 0);
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		
		assertEquals(ba.admissible(instantaneous), 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
		assertTrue(ba.admissible(instantaneous) > 0);
		assertFalse(ba.commit());
		assertTrue(ba.isAlarmed());
		ba.reset(instantaneous);
		
		assertEquals(ba.admissible(instantaneous), 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
	}
	
	public void test05Increment() {
		
		Throttle ba = new BandwidthAlgorithm(1, 0);
		assertNotNull(ba);
		assertTrue(ba.isValid());
		
		long now = ba.time();
		assertEquals(ba.admissible(now), 0);
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		
		assertEquals(ba.admissible(now), 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
		long delay = ba.admissible(now);
		assertEquals(delay, 1);
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		
		for (int ii = 0; ii < 1000; ++ii) {
			
			now += delay;
			delay = ba.admissible(now);
			assertEquals(delay, 0);
			assertTrue(ba.commit());
			assertFalse(ba.isAlarmed());
			
			delay = ba.admissible(now);
			assertEquals(delay, 1);
			assertTrue(ba.rollback());
			assertFalse(ba.isAlarmed());
			
		}
	}
	
	public void test06Increment() {
		
		Throttle ba = new BandwidthAlgorithm(1000, 0);
		assertNotNull(ba);
		assertTrue(ba.isValid());
		
		long now = ba.time();
		assertEquals(ba.admissible(now), 0);
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		
		assertEquals(ba.admissible(now), 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
		long delay = ba.admissible(now);
		assertEquals(delay, 1000);
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		
		for (int ii = 0; ii < 1000; ++ii) {
			
			now += delay;
			delay = ba.admissible(now);
			assertEquals(delay, 0);
			assertTrue(ba.commit());
			assertFalse(ba.isAlarmed());
			
			delay = ba.admissible(now);
			assertEquals(delay, 1000);
			assertTrue(ba.rollback());
			assertFalse(ba.isAlarmed());
			
		}
	}
	
	public void test07Limit() {
		
		Throttle ba = new BandwidthAlgorithm(1000, 250);
		assertNotNull(ba);
		assertTrue(ba.isValid());
		
		long now = ba.time();
		assertEquals(ba.admissible(now), 0);
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		
		assertEquals(ba.admissible(now), 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
		long delay = ba.admissible(now);
		assertEquals(delay, 750);
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		
		now += 900;
		delay = ba.admissible(now);
		assertEquals(delay, 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
		now += 900;
		delay = ba.admissible(now);
		assertEquals(delay, 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
		now += 900;
		delay = ba.admissible(now);
		assertEquals(delay, 50);
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		
		now += 50;
		delay = ba.admissible(now);
		assertEquals(delay, 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
		delay = ba.admissible(now);
		assertEquals(delay, 1000);
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		
		now += 1000;
		delay = ba.admissible(now);
		assertEquals(delay, 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
		now += 999;
		delay = ba.admissible(now);
		assertEquals(delay, 1);
		assertTrue(ba.rollback());
		assertFalse(ba.isAlarmed());
		
		now += 1250;
		delay = ba.admissible(now);
		assertEquals(delay, 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
		now += 1000;
		delay = ba.admissible(now);
		assertEquals(delay, 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
		now += 750;
		delay = ba.admissible(now);
		assertEquals(delay, 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
		now += 1000;
		delay = ba.admissible(now);
		assertEquals(delay, 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());
		
		delay = ba.admissible(now);
		assertEquals(delay, 1000);
		assertFalse(ba.commit());
		assertTrue(ba.isAlarmed());
		
		now += 2000;
		delay = ba.admissible(now);
		assertEquals(delay, 0);
		assertTrue(ba.commit());
		assertFalse(ba.isAlarmed());

	}
	
	public void test08Conversions() {
		
		assertEquals(BandwidthAlgorithm.ms2increment(0), 0L);
		assertEquals(BandwidthAlgorithm.ms2increment(1), 1000000L);
		assertEquals(BandwidthAlgorithm.ms2increment(1000000), 1000000000000L);
		assertEquals(BandwidthAlgorithm.ms2increment(1001000), 1001000000000L);
		assertEquals(BandwidthAlgorithm.ms2increment(1999000), 1999000000000L);
		assertEquals(BandwidthAlgorithm.ms2increment(2000000), 2000000000000L);
		
		assertEquals(BandwidthAlgorithm.ms2limit(0), 0L);
		assertEquals(BandwidthAlgorithm.ms2limit(1), 1000000L);
		assertEquals(BandwidthAlgorithm.ms2limit(1000000), 1000000000000L);
		assertEquals(BandwidthAlgorithm.ms2limit(1001000), 1001000000000L);
		assertEquals(BandwidthAlgorithm.ms2limit(1999000), 1999000000000L);
		assertEquals(BandwidthAlgorithm.ms2limit(2000000), 2000000000000L);
		
		assertEquals(BandwidthAlgorithm.ns2increment(0), 0L);
		assertEquals(BandwidthAlgorithm.ns2increment(1), 1L);
		assertEquals(BandwidthAlgorithm.ns2increment(1000000), 1000000L);
		assertEquals(BandwidthAlgorithm.ns2increment(1001000), 1001000L);
		assertEquals(BandwidthAlgorithm.ns2increment(1999000), 1999000L);
		assertEquals(BandwidthAlgorithm.ns2increment(2000000), 2000000L);
		
		assertEquals(BandwidthAlgorithm.ns2limit(0), 0L);
		assertEquals(BandwidthAlgorithm.ns2limit(1), 1L);
		assertEquals(BandwidthAlgorithm.ns2limit(1000000), 1000000L);
		assertEquals(BandwidthAlgorithm.ns2limit(1001000), 1001000L);
		assertEquals(BandwidthAlgorithm.ns2limit(1999000), 1999000L);
		assertEquals(BandwidthAlgorithm.ns2limit(2000000), 2000000L);
		
		assertEquals(BandwidthAlgorithm.delay2ms(0), 0L);
		assertEquals(BandwidthAlgorithm.delay2ms(1), 1L);
		assertEquals(BandwidthAlgorithm.delay2ms(1000000), 1L);
		assertEquals(BandwidthAlgorithm.delay2ms(1001000), 2L);
		assertEquals(BandwidthAlgorithm.delay2ms(1999000), 2L);
		assertEquals(BandwidthAlgorithm.delay2ms(2000000), 2L);
		
		assertEquals(BandwidthAlgorithm.delay2ms1(0), 0L);
		assertEquals(BandwidthAlgorithm.delay2ms1(1), 0L);
		assertEquals(BandwidthAlgorithm.delay2ms1(1000000), 1L);
		assertEquals(BandwidthAlgorithm.delay2ms1(1001000), 1L);
		assertEquals(BandwidthAlgorithm.delay2ms1(1999000), 1L);
		assertEquals(BandwidthAlgorithm.delay2ms1(2000000), 2L);
		
		assertEquals(BandwidthAlgorithm.delay2ns2(0), 0L);
		assertEquals(BandwidthAlgorithm.delay2ns2(1), 1L);
		assertEquals(BandwidthAlgorithm.delay2ns2(1000000), 0L);
		assertEquals(BandwidthAlgorithm.delay2ns2(1001000), 1000L);
		assertEquals(BandwidthAlgorithm.delay2ns2(1999000), 999000L);
		assertEquals(BandwidthAlgorithm.delay2ns2(2000000), 0L);
		
	}
	
	public void test09Example1() {
		
		long bps = 1024;
		long increment = (1000000000L + bps - 1) / bps;
		long limit = 0;
		BandwidthAlgorithm ba = new BandwidthAlgorithm(increment, limit);
		long now = 0;
		// Because the Throttle was constructed using the actual time, not simulated time.
		ba.reset(now);
		System.out.println("ba=" + ba);
		
		Random random = new Random();
		
		int octets;
		int minoctets = Integer.MAX_VALUE;
		int maxoctets = Integer.MIN_VALUE;
		// Beware making totoctets float or double because of floating-point round-off errors.
		long totoctets = 0;
		
		long ticks;
		long minticks = Long.MAX_VALUE;
		long maxticks = Long.MIN_VALUE;
		// Beware making totticks float or double because of floating-point round-off errors.
		long totticks = 0;
		
		final int maxsize = 1024 * 1024;
		// Beware making count much larger or else totticks will overflow.
		final int count = 10 * 1000 * 1000;
		
		for (int ii = 0; ii < count; ++ii) {
			
			assertTrue(ba.isValid());
			ticks = ba.admissible(now);
			assertTrue(ticks >= 0);
			if (ticks < minticks) { minticks = ticks; }
			if (ticks > maxticks) { maxticks = ticks; }
			totticks += ticks;
			assertTrue(totticks >= 0);
			
			if (ticks > 0) {
				ba.rollback();
				now += ticks;
				ticks = ba.admissible(now);
				// There is no scheduling non-determinism using simulated time.
				assertEquals(ticks, 0);
			}
			
			float fraction = random.nextFloat();
			octets = (int)(maxsize * fraction);
			if (octets < minoctets) { minoctets = octets; }
			if (octets > maxoctets) { maxoctets = octets; }
			totoctets += octets;
			assertTrue(totoctets >= 0);
			
			ba.commit(octets);
			assertFalse(ba.isAlarmed());
			
		}
		
		// Account for the final delay to get the bandwidth calculation to be correct.
		assertTrue(ba.isValid());
		ticks = ba.admissible(now);
		assertTrue(ticks >= 0);
		if (ticks < minticks) { minticks = ticks; }
		if (ticks > maxticks) { maxticks = ticks; }
		totticks += ticks;
		assertTrue(totticks >= 0);
		
		float meanoctets = totoctets;
		meanoctets /= count;
		float mindelay = minticks;
		mindelay /= 1000000000;
		float meandelay = totticks;
		meandelay /= 1000000000;
		meandelay /= count;
		float maxdelay = maxticks;
		maxdelay /= 1000000000;
		float bandwidth = totoctets;
		bandwidth /= totticks;
		bandwidth *= 1000000000;
		System.out.println(
				"sent=" + count + "packets"
				+ " size=" + minoctets + "<=" + meanoctets + "<=" + maxoctets + "octets"
				+ " delay=" + mindelay + "<=" + meandelay + "<=" + maxdelay + "seconds"
				+ " expected=" + (float)bps + "bytes/second"
				+ " actual=" + bandwidth + "bytes/second"
		);
		assertTrue(Math.ceil(bandwidth) == (float)bps);
	}
	
	public void test10Example2() {
		
		long increment = BandwidthAlgorithm.ms2increment(1000);
		long limit = BandwidthAlgorithm.ms2limit(250);
		Throttle ba = new BandwidthAlgorithm(increment, limit);
		System.out.println("ba=" + ba);
		
		long then = System.currentTimeMillis();
		for (int ii = 0; ii < 20; ++ii) {
			long delay = BandwidthAlgorithm.delay2ms(ba.admissible());
			while (delay > 0) {
				ba.rollback();
				try { Thread.sleep(delay); } catch (Exception ignore) { }
				delay = BandwidthAlgorithm.delay2ms(ba.admissible());
			}
			ba.commit();
			assertFalse(ba.isAlarmed());
			long now = System.currentTimeMillis();
			System.out.println("event=" + ii + " elapsed=" + (now - then) + "ms");
			then = now;
		}
	}
	
	public void test11Example3() {
		
		long increment = BandwidthAlgorithm.ns2increment(1000000000L);
		long limit = BandwidthAlgorithm.ns2limit(250000000L);
		Throttle ba = new BandwidthAlgorithm(increment, limit);
		System.out.println("ba=" + ba);
		
		long then = System.currentTimeMillis();
		for (int ii = 0; ii < 20; ++ii) {
			long us = ba.admissible();
			while (us > 0) {
				long ms = BandwidthAlgorithm.delay2ms1(us);
				int ns = BandwidthAlgorithm.delay2ns2(us);
				ba.rollback();
				try { Thread.sleep(ms, ns); } catch (Exception ignore) { }
				us = ba.admissible();
			}
			ba.commit();
			assertFalse(ba.isAlarmed());
			long now = System.currentTimeMillis();
			System.out.println("event=" + ii + " elapsed=" + (now - then) + "ms");
			then = now;
		}
	}

}
