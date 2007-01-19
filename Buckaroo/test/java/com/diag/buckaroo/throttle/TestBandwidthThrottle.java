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

import junit.framework.TestCase;
import com.diag.buckaroo.throttle.BandwidthThrottle;
import com.diag.buckaroo.throttle.ManifoldThrottle;
import com.diag.buckaroo.throttle.TestBandwidthAlgorithm;

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
		
		int[] values = new int[] {
				Integer.MIN_VALUE,
				-1,
				0,
				1,
				10,
				100,
				1000,
				10000,
				100000,
				1000000,
				10000000,
				100000000,
				1000000000,
				Integer.MAX_VALUE
		};
		
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

	public void test05Exercise() {
		final int pbr = 1024;
		final int bps = 1024;
		final int maximum = 1024 * 1024;
		final int count = 10 * 1000 * 1000;
		final double bandwidth = TestBandwidthAlgorithm.exercise(new BandwidthThrottle(pbr), bps, maximum, count);
		final double accuracy = (Math.abs((double)pbr - bandwidth) / (double)pbr) * 100;
		System.out.println("accuracy=" + accuracy);
		assertTrue("accuracy=" + accuracy, accuracy < 0.1);
	}
	
	public void test06Exercise() {
		final int pbr = 10240;
		final int sbr = 1024;
		final int mbs = 10240;
		final int bps = 1024;
		final int maximum = 1024 * 1024;
		final int count = 10 * 1000 * 1000;
		final double bandwidth = TestBandwidthAlgorithm.exercise(new BandwidthThrottle(pbr, sbr, mbs), bps, maximum, count);
		final double accuracy = (Math.abs((double)bps - bandwidth) / (double)bps) * 100;
		System.out.println("accuracy=" + accuracy);
		assertTrue("accuracy=" + accuracy, accuracy < 0.1);
	}
	
	public void test07Exercise() {
		final int pbr = 1024;
		final int jt = 250000;
		final int bps = 1024;
		final int maximum = 1024 * 1024;
		final int count = 10 * 1000 * 1000;
		final double bandwidth = TestBandwidthAlgorithm.exercise(new BandwidthThrottle(pbr, jt), bps, maximum, count);
		final double accuracy = (Math.abs((double)bps - bandwidth) / (double)bps) * 100;
		System.out.println("accuracy=" + accuracy);
		assertTrue("accuracy=" + accuracy, accuracy < 0.1);
	}
	
	public void test08Exercise() {
		final int pbr = 10240;
		final int jt = 250000;
		final int sbr = 1024;
		final int mbs = 10240;
		final int bps = 1024;
		final int maximum = 1024 * 1024;
		final int count = 10 * 1000 * 1000;
		final double bandwidth = TestBandwidthAlgorithm.exercise(new BandwidthThrottle(pbr, jt, sbr, mbs), bps, maximum, count);
		final double accuracy = (Math.abs((double)bps - bandwidth) / (double)bps) * 100;
		System.out.println("accuracy=" + accuracy);
		assertTrue("accuracy=" + accuracy, accuracy < 0.1);
	}
	
	public void test09Example1() {
		final int pbr = 1;
		ManifoldThrottle bt = new BandwidthThrottle(pbr);
		System.out.println("bt=" + bt);
		
		long then = System.currentTimeMillis();
		for (int ii = 0; ii < 20; ++ii) {
			long ms = BandwidthThrottle.delay2ms(bt.admissible());
			while (ms > 0) {
				bt.rollback();
				try { Thread.sleep(ms); } catch (Exception ignore) { }
				ms = BandwidthThrottle.delay2ms(bt.admissible());
			}
			bt.commit(1);
			assertFalse(bt.isAlarmed());
			long now = System.currentTimeMillis();
			System.out.println("event=" + ii + " elapsed=" + (now - then) + "ms");
			then = now;
		}
	}
	
	public void test10Example2() {
		final int pbr = 10;
		final int jt = 250000;
		final int sbr = 1;
		final int mbs = 10;
		ManifoldThrottle bt = new BandwidthThrottle(pbr, jt, sbr, mbs);
		System.out.println("bt=" + bt);
		
		long then = System.nanoTime();
		for (int ii = 0; ii < 20; ++ii) {
			long ticks = bt.admissible();
			while (ticks > 0) {
				bt.rollback();
				long ms = BandwidthThrottle.delay2ms1(ticks);
				int ns = BandwidthThrottle.delay2ns2(ticks);
				try { Thread.sleep(ms, ns); } catch (Exception ignore) { }
				ticks = bt.admissible();
			}
			bt.commit(1);
			assertFalse(bt.isAlarmed());
			long now = System.nanoTime();
			System.out.println("event=" + ii + " elapsed=" + (now - then) + "ns");
			then = now;
		}
	}

}
