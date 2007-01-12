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
import com.diag.buckaroo.throttle.CellRateThrottle;

public class TestCellRateThrottle extends TestCase {
	
	void validateInitialState(Throttle crt) {
		long ticks = 0;
		assertNotNull(crt);
		crt.reset(ticks);
		assertTrue(crt.isValid());
		assertNotNull(crt.toString());
		assertEquals(crt.admissible(ticks), 0);
		assertFalse(crt.isAlarmed());
		assertTrue(crt.rollback());
		assertFalse(crt.isAlarmed());
		assertEquals(crt.admissible(ticks), 0);
		assertFalse(crt.isAlarmed());
		assertTrue(crt.rollback());
		assertFalse(crt.isAlarmed());
		assertEquals(crt.admissible(ticks), 0);
		assertFalse(crt.isAlarmed());
		crt.reset(ticks);
		assertFalse(crt.isAlarmed());
		assertEquals(crt.admissible(ticks), 0);
		assertTrue(crt.commit());
		assertFalse(crt.isAlarmed());
	}

	public void test00Construction() {
		int[] values = new int[] { Integer.MIN_VALUE, 0, Integer.MAX_VALUE };
		Throttle crt = new CellRateThrottle();
		System.out.println("crt=" + crt);
		validateInitialState(crt);
		for (int pcr : values) {
			crt = new CellRateThrottle(pcr);
			System.out.println("pcr=" + pcr + " crt=" + crt);
			validateInitialState(crt);
			for (int cdvt : values)
			{
				crt = new CellRateThrottle(pcr, cdvt);
				System.out.println("pcr=" + pcr + " cdvt=" + cdvt + " crt=" + crt);
				validateInitialState(crt);
				for (int scr : values) {
					for (int mbs : values) {
						if (cdvt == 0) {
							crt = new CellRateThrottle(pcr, scr, mbs);
							System.out.println("pcr=" + pcr + " scr=" + scr + " mbs=" + mbs + " crt=" + crt);
							validateInitialState(crt);
						}
						crt = new CellRateThrottle(pcr, cdvt, scr, mbs);
						System.out.println("pcr=" + pcr + " cdvt=" + cdvt + " scr=" + scr + " mbs=" + mbs + " crt=" + crt);
						validateInitialState(crt);
					}
				}
			}
		}
	}
	
	public void test01Time() {
		Throttle gcra = new CellRateThrottle();
		assertNotNull(gcra);
		long hz = gcra.frequency();
		assertTrue(hz > 0);
		long then = 0;
		for (int ii = 0; ii < 1000; ++ii) {
			long now = gcra.time();
			assertTrue(now > then);
			then = now;
			assertEquals(gcra.frequency(), hz);
			try { Thread.sleep(1); } catch (Exception ignore) { }
		}
	}
	
	public void test02ConstantBitRate() {
		int pcr = 500;
		int cdvt = 250;
		Throttle crt = new CellRateThrottle(pcr, cdvt);
		System.out.println("pcr=" + pcr + " cdvt=" + cdvt + " crt=" + crt);
		assertTrue(crt.isValid());
		validateInitialState(crt);
	}
	
	public void test03VariableBitRate() {
		int pcr = 500;
		int cdvt = 250;
		int scr = 200;
		int mbs = 10;
		Throttle crt = new CellRateThrottle(pcr, cdvt, scr, mbs);
		System.out.println("pcr=" + pcr + " cdvt=" + cdvt + " scr=" + scr + " mbs=" + mbs + " crt=" + crt);
		assertTrue(crt.isValid());
		validateInitialState(crt);
	}
	public void test04Conversions() {
		
		assertEquals(CellRateThrottle.delay2ms(0), 0L);
		assertEquals(CellRateThrottle.delay2ms(1), 1L);
		assertEquals(CellRateThrottle.delay2ms(1000), 1L);
		assertEquals(CellRateThrottle.delay2ms(1001), 2L);
		assertEquals(CellRateThrottle.delay2ms(1999), 2L);
		assertEquals(CellRateThrottle.delay2ms(2000), 2L);
		
		assertEquals(CellRateThrottle.delay2ms1(0), 0L);
		assertEquals(CellRateThrottle.delay2ms1(1), 0L);
		assertEquals(CellRateThrottle.delay2ms1(1000), 1L);
		assertEquals(CellRateThrottle.delay2ms1(1001), 1L);
		assertEquals(CellRateThrottle.delay2ms1(1999), 1L);
		assertEquals(CellRateThrottle.delay2ms1(2000), 2L);
		
		assertEquals(CellRateThrottle.delay2ns2(0), 0L);
		assertEquals(CellRateThrottle.delay2ns2(1), 1000L);
		assertEquals(CellRateThrottle.delay2ns2(1000), 0L);
		assertEquals(CellRateThrottle.delay2ns2(1001), 1000L);
		assertEquals(CellRateThrottle.delay2ns2(1999), 999000L);
		assertEquals(CellRateThrottle.delay2ns2(2000), 0L);
		
	}
	
	public void test05Example() {
		int pcr = 1;
		Throttle crt = new CellRateThrottle(pcr);
		System.out.println("crt=" + crt);
		
		long then = System.currentTimeMillis();
		for (int ii = 0; ii < 20; ++ii) {
			long delay = CellRateThrottle.delay2ms(crt.admissible());
			while (delay > 0) {
				crt.rollback();
				try { Thread.sleep(delay); } catch (Exception ignore) { }
				delay = CellRateThrottle.delay2ms(crt.admissible());
			}
			crt.commit();
			assertFalse(crt.isAlarmed());
			long now = System.currentTimeMillis();
			System.out.println("event=" + ii + " elapsed=" + (now - then) + "ms");
			then = now;
		}
	}
	
	public void test06Example() {
		int pcr = 10;
		int cdvt = 250;
		int scr = 1;
		int mbs = 10;
		Throttle crt = new CellRateThrottle(pcr, cdvt, scr, mbs);
		System.out.println("crt=" + crt);
		
		long then = System.currentTimeMillis();
		for (int ii = 0; ii < 20; ++ii) {
			long delay = CellRateThrottle.delay2ms(crt.admissible());
			while (delay > 0) {
				crt.rollback();
				try { Thread.sleep(delay); } catch (Exception ignore) { }
				delay = CellRateThrottle.delay2ms(crt.admissible());
			}
			crt.commit();
			assertFalse(crt.isAlarmed());
			long now = System.currentTimeMillis();
			System.out.println("event=" + ii + " elapsed=" + (now - then) + "ms");
			then = now;
		}
	}
}
