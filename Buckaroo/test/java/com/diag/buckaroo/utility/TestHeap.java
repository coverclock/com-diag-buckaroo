/**
 * Copyright 2006 Digital Aggregates Corp., Arvada CO 80001-0597, USA.
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
package com.diag.buckaroo.utility;

import junit.framework.TestCase;
import com.diag.buckaroo.utility.Heap;

public class TestHeap extends TestCase {
	
	int[] array;

	public void test01Heap() {
		Heap heap = new Heap();
		long mm = heap.getBytesMaximum();
		long ii = heap.getBytesInitial();
		long cc = heap.getBytesCommitted();
		long uu = heap.getBytesUsed();
		System.out.println(ii + " " + uu + " " + cc + " " + mm);
		assertTrue((ii <= uu) && (uu <= cc) && (cc <= mm));
	}
	
	public void test02Heap() {
		array = new int[1024];
	}

	public void test03Heap() {
		Heap heap = new Heap();
		long mm = heap.getBytesMaximum();
		long ii = heap.getBytesInitial();
		long cc = heap.getBytesCommitted();
		long uu = heap.getBytesUsed();
		System.out.println(ii + " " + uu + " " + cc + " " + mm);
		assertTrue((ii <= uu) && (uu <= cc) && (cc <= mm));
	}
}
