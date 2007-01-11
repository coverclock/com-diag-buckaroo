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

	public void test01() {
		long[] values = new long[] { Long.MIN_VALUE, 0, Long.MAX_VALUE };
		for (long increment : values) {
			for (long limit : values)
			{
				Throttle gcra = new GenericCellRateAlgorithm(increment, limit);
				System.out.println("increment=" + increment + " limit=" + limit + " gcra=" + gcra + "");
			}
		}
	}
	
	public void test02() {
		long increment = new GenericCellRateAlgorithm().frequency() / 500;
		long limit = 250;
		Throttle gcra = new GenericCellRateAlgorithm(increment, limit);
		System.out.println("increment=" + increment + " limit=" + limit + " gcra=" + gcra + "");
	}
	
}
