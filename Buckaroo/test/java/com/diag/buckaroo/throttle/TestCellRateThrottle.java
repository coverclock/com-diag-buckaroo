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

	public void test01() {
		int[] values = new int[] { Integer.MIN_VALUE, 0, Integer.MAX_VALUE };
		for (int pcr : values) {
			Throttle crt = new CellRateThrottle(pcr);
			System.out.println("pcr=" + pcr + " crt=" + crt);
			for (int cdvt : values)
			{
				crt = new CellRateThrottle(pcr, cdvt);
				System.out.println("pcr=" + pcr + " cdvt=" + cdvt + " crt=" + crt);
				for (int scr : values) {
					for (int mbs : values) {
						crt = new CellRateThrottle(pcr, cdvt, scr, mbs);
						System.out.println("pcr=" + pcr + " cdvt=" + cdvt + " scr=" + scr + " mbs=" + mbs + " crt=" + crt);
					}
				}
			}
			for (int scr : values) {
				for (int mbs : values) {
					crt = new CellRateThrottle(pcr, scr, mbs);
					System.out.println("pcr=" + pcr + " scr=" + scr + " mbs=" + mbs + " crt=" + crt);
				}
			}
		}
	}
	
	public void test02() {
		int pcr = 500;
		int cdvt = 250;
		Throttle crt = new CellRateThrottle(pcr, cdvt);
		System.out.println("pcr=" + pcr + " cdvt=" + cdvt + " crt=" + crt);
	}
	
	public void test03() {
		int pcr = 500;
		int cdvt = 250;
		int scr = 200;
		int mbs = 10;
		Throttle crt = new CellRateThrottle(pcr, cdvt, scr, mbs);
		System.out.println("pcr=" + pcr + " cdvt=" + cdvt + " scr=" + scr + " mbs=" + mbs + " crt=" + crt);
	}
	
}
