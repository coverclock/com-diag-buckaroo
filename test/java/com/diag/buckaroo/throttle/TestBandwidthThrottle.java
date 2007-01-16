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

import java.lang.Integer;
import java.util.Random;

import junit.framework.TestCase;
import com.diag.buckaroo.throttle.BandwidthThrottle;
import com.diag.buckaroo.throttle.ManifoldThrottle;

public class TestBandwidthThrottle extends TestCase {
	
	void validateInitialState(ManifoldThrottle bt) {
		long ticks = 0;
		assertNotNull(bt);
		bt.reset(ticks);
		assertTrue(bt.isValid());
		assertNotNull(bt.toString());
		assertEquals(bt.admissible(ticks), 0);
		assertFalse(bt.isAlarmed());
		assertTrue(bt.rollback());
		assertFalse(bt.isAlarmed());
		assertEquals(bt.admissible(ticks), 0);
		assertFalse(bt.isAlarmed());
		assertTrue(bt.rollback());
		assertFalse(bt.isAlarmed());
		assertEquals(bt.admissible(ticks), 0);
		assertFalse(bt.isAlarmed());
		bt.reset(ticks);
		assertFalse(bt.isAlarmed());
		assertEquals(bt.admissible(ticks), 0);
		assertTrue(bt.commit(1));
		assertFalse(bt.isAlarmed());
	}

	public void test00Construction() {
		int[] values = new int[] { Integer.MIN_VALUE, 0, Integer.MAX_VALUE };
		ManifoldThrottle bt = new BandwidthThrottle();
		System.out.println("bt=" + bt);
		validateInitialState(bt);
		for (int pbr : values) {
			bt = new BandwidthThrottle(pbr);
			System.out.println("pbr=" + pbr + " bt=" + bt);
			validateInitialState(bt);
			for (int jt : values)
			{
				bt = new BandwidthThrottle(pbr, jt);
				System.out.println("pbr=" + pbr + " jt=" + jt + " bt=" + bt);
				validateInitialState(bt);
				for (int sbr : values) {
					for (int mbs : values) {
						if (jt == 0) {
							bt = new BandwidthThrottle(pbr, sbr, mbs);
							System.out.println("pbr=" + pbr + " sbr=" + sbr + " mbs=" + mbs + " bt=" + bt);
							validateInitialState(bt);
						}
						bt = new BandwidthThrottle(pbr, jt, sbr, mbs);
						System.out.println("pbr=" + pbr + " jt=" + jt + " sbr=" + sbr + " mbs=" + mbs + " bt=" + bt);
						validateInitialState(bt);
					}
				}
			}
		}
	}
	
	public void test01Time() {
		ManifoldThrottle bt = new BandwidthThrottle();
		assertNotNull(bt);
		long hz = bt.frequency();
		assertEquals(hz, 1000000000L);
		long then = bt.time();
		for (int ii = 0; ii < 1000; ++ii) {
			try { Thread.sleep(1); } catch (Exception ignore) { }
			long now = bt.time();
			assertTrue(now > then);
			then = now;
			assertEquals(bt.frequency(), hz);
		}
	}
	
	public void test02ConstantBitRate() {
		int pbr = 1024;
		int jt = 250000;
		ManifoldThrottle bt = new BandwidthThrottle(pbr, jt);
		System.out.println("pbr=" + pbr + " jt=" + jt + " bt=" + bt);
		assertTrue(bt.isValid());
		validateInitialState(bt);
	}
	
	public void test03VariableBitRate() {
		int pbr = 2048;
		int jt = 250000;
		int sbr = 1024;
		int mbs = 512;
		ManifoldThrottle bt = new BandwidthThrottle(pbr, jt, sbr, mbs);
		System.out.println("pbr=" + pbr + " jt=" + jt + " sbr=" + sbr + " mbs=" + mbs + " bt=" + bt);
		assertTrue(bt.isValid());
		validateInitialState(bt);
	}
	public void test04Conversions() {
		
		assertEquals(BandwidthThrottle.delay2ms(0), 0L);
		assertEquals(BandwidthThrottle.delay2ms(1), 1L);
		assertEquals(BandwidthThrottle.delay2ms(1000000), 1L);
		assertEquals(BandwidthThrottle.delay2ms(1001000), 2L);
		assertEquals(BandwidthThrottle.delay2ms(1999000), 2L);
		assertEquals(BandwidthThrottle.delay2ms(2000000), 2L);
		
		assertEquals(BandwidthThrottle.delay2ms1(0), 0L);
		assertEquals(BandwidthThrottle.delay2ms1(1), 0L);
		assertEquals(BandwidthThrottle.delay2ms1(1000000), 1L);
		assertEquals(BandwidthThrottle.delay2ms1(1001000), 1L);
		assertEquals(BandwidthThrottle.delay2ms1(1999000), 1L);
		assertEquals(BandwidthThrottle.delay2ms1(2000000), 2L);
		
		assertEquals(BandwidthThrottle.delay2ns2(0), 0L);
		assertEquals(BandwidthThrottle.delay2ns2(1), 1L);
		assertEquals(BandwidthThrottle.delay2ns2(1000000), 0L);
		assertEquals(BandwidthThrottle.delay2ns2(1001000), 1000L);
		assertEquals(BandwidthThrottle.delay2ns2(1999000), 999000L);
		assertEquals(BandwidthThrottle.delay2ns2(2000000), 0L);
		
	}
	
	private float exercise(ManifoldThrottle bt, int bps) {
		
		long now = 0;
		// Because the Throttle was constructed using the actual time, not simulated time.
		bt.reset(now);
		System.out.println("bt=" + bt);
		
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
			
			assertTrue(bt.isValid());
			ticks = bt.admissible(now);
			assertTrue(ticks >= 0);
			if (ticks < minticks) { minticks = ticks; }
			if (ticks > maxticks) { maxticks = ticks; }
			totticks += ticks;
			assertTrue(totticks >= 0);
			
			if (ticks > 0) {
				bt.rollback();
				now += ticks;
				ticks = bt.admissible(now);
				// There is no scheduling non-determinism using simulated time.
				assertEquals(ticks, 0);
			}
			
			float fraction = random.nextFloat();
			octets = (int)(maxsize * fraction);
			if (octets < minoctets) { minoctets = octets; }
			if (octets > maxoctets) { maxoctets = octets; }
			totoctets += octets;
			assertTrue(totoctets >= 0);
			
			bt.commit(octets);
			assertFalse(bt.isAlarmed());
			
		}
		
		// Account for the final delay to get the bandwidth calculation to be correct.
		assertTrue(bt.isValid());
		ticks = bt.admissible(now);
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
		
		return  bandwidth;
	}
	
	public void test05Example() {
		exercise(new BandwidthThrottle(1024), 1024);
	}
	
	public void test06Example() {
		exercise(new BandwidthThrottle(10240, 1024, 10240), 1024);
	}
	
	public void test07Example() {
		exercise(new BandwidthThrottle(1024, 250000), 1024);
	}
	
	public void test08Example() {
		exercise(new BandwidthThrottle(10240, 250000, 1024, 10240), 1024);
	}

}
