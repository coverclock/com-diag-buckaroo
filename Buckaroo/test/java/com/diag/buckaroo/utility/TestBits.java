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
package com.diag.buckaroo.utility;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * This class tests some handy little utilities for dealing with bits.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class TestBits {

	public int nextPowerOf2(int value) {
		int result = Bits.msb(value);
		return (result == 0) ? 1 : (result << 1);
	}

	@Test
	public void testNextPowerOf2() {
	    assertEquals(8, nextPowerOf2(7)); 
	    assertEquals(256, nextPowerOf2(248)); 
	    assertEquals(4096, nextPowerOf2(3015)); 
	}
	
	public void testMsbByteRange(byte first, byte last, byte expected) {
		byte word = first;
		for (;;) {
			assertEquals(Bits.msb(word), expected);
			if (word == last) { break; }
			++word;
		}
	}

	@Test
	public void testMsbByte00() {
		assertEquals(Bits.msb((byte)0x00), (byte)0x00);
		assertEquals(Bits.msb((byte)0x01), (byte)0x01);
		assertEquals(Bits.msb((byte)0x02), (byte)0x02);
		assertEquals(Bits.msb((byte)0x04), (byte)0x04);
		assertEquals(Bits.msb((byte)0x08), (byte)0x08);
		assertEquals(Bits.msb((byte)0x10), (byte)0x10);
		assertEquals(Bits.msb((byte)0x20), (byte)0x20);
		assertEquals(Bits.msb((byte)0x40), (byte)0x40);
		assertEquals(Bits.msb((byte)0x80), (byte)0x80);
		testMsbByteRange((byte)0x02, ( byte)0x03, (byte)0x02);
		testMsbByteRange((byte)0x04, ( byte)0x07, (byte)0x04);
		testMsbByteRange((byte)0x08, ( byte)0x0f, (byte)0x08);
		testMsbByteRange((byte)0x10, ( byte)0x1f, (byte)0x10);
		testMsbByteRange((byte)0x20, ( byte)0x3f, (byte)0x20);
		testMsbByteRange((byte)0x40, ( byte)0x7f, (byte)0x40);
		testMsbByteRange((byte)0x80, ( byte)0xff, (byte)0x80);
	}
	
	public void testMsbShortRange(short first, short last, short expected) {
		short word = first;
		for (;;) {
			assertEquals(Bits.msb(word), expected);
			if (word == last) { break; }
			++word;
		}
	}

	@Test
	public void testMsbShort00() {
		assertEquals(Bits.msb((short)0x0000), (short)0x0000);
		assertEquals(Bits.msb((short)0x0001), (short)0x0001);
		assertEquals(Bits.msb((short)0x0002), (short)0x0002);
		assertEquals(Bits.msb((short)0x0004), (short)0x0004);
		assertEquals(Bits.msb((short)0x0008), (short)0x0008);
		assertEquals(Bits.msb((short)0x0010), (short)0x0010);
		assertEquals(Bits.msb((short)0x0020), (short)0x0020);
		assertEquals(Bits.msb((short)0x0040), (short)0x0040);
		assertEquals(Bits.msb((short)0x0080), (short)0x0080);
		assertEquals(Bits.msb((short)0x0100), (short)0x0100);
		assertEquals(Bits.msb((short)0x0200), (short)0x0200);
		assertEquals(Bits.msb((short)0x0400), (short)0x0400);
		assertEquals(Bits.msb((short)0x0800), (short)0x0800);
		assertEquals(Bits.msb((short)0x1000), (short)0x1000);
		assertEquals(Bits.msb((short)0x2000), (short)0x2000);
		assertEquals(Bits.msb((short)0x4000), (short)0x4000);
		assertEquals(Bits.msb((short)0x8000), (short)0x8000);
		testMsbShortRange((short)0x0002, (short)0x0003, (short)0x0002);
		testMsbShortRange((short)0x0004, (short)0x0007, (short)0x0004);
		testMsbShortRange((short)0x0008, (short)0x000f, (short)0x0008);
		testMsbShortRange((short)0x0010, (short)0x001f, (short)0x0010);
		testMsbShortRange((short)0x0020, (short)0x003f, (short)0x0020);
		testMsbShortRange((short)0x0040, (short)0x007f, (short)0x0040);
		testMsbShortRange((short)0x0080, (short)0x00ff, (short)0x0080);
		testMsbShortRange((short)0x0100, (short)0x01ff, (short)0x0100);
		testMsbShortRange((short)0x0200, (short)0x03ff, (short)0x0200);
		testMsbShortRange((short)0x0400, (short)0x07ff, (short)0x0400);
		testMsbShortRange((short)0x0800, (short)0x0fff, (short)0x0800);
		testMsbShortRange((short)0x1000, (short)0x1fff, (short)0x1000);
		testMsbShortRange((short)0x2000, (short)0x3fff, (short)0x2000);
		testMsbShortRange((short)0x4000, (short)0x7fff, (short)0x4000);
		testMsbShortRange((short)0x8000, (short)0xffff, (short)0x8000);
	}
	
	public void testMsbIntRange(int first, int last, int expected) {
		int word = first;
		for (;;) {
			assertEquals(Bits.msb(word), expected);
			if (word == last) { break; }
			++word;
		}
	}

	@Test
	public void testMsbInt00() {
		assertEquals(Bits.msb((int)0x00000000), (int)0x00000000);
		assertEquals(Bits.msb((int)0x00000001), (int)0x00000001);
		assertEquals(Bits.msb((int)0x00000002), (int)0x00000002);
		assertEquals(Bits.msb((int)0x00000004), (int)0x00000004);
		assertEquals(Bits.msb((int)0x00000008), (int)0x00000008);
		assertEquals(Bits.msb((int)0x00000010), (int)0x00000010);
		assertEquals(Bits.msb((int)0x00000020), (int)0x00000020);
		assertEquals(Bits.msb((int)0x00000040), (int)0x00000040);
		assertEquals(Bits.msb((int)0x00000080), (int)0x00000080);
		assertEquals(Bits.msb((int)0x00000100), (int)0x00000100);
		assertEquals(Bits.msb((int)0x00000200), (int)0x00000200);
		assertEquals(Bits.msb((int)0x00000400), (int)0x00000400);
		assertEquals(Bits.msb((int)0x00000800), (int)0x00000800);
		assertEquals(Bits.msb((int)0x00001000), (int)0x00001000);
		assertEquals(Bits.msb((int)0x00002000), (int)0x00002000);
		assertEquals(Bits.msb((int)0x00004000), (int)0x00004000);
		assertEquals(Bits.msb((int)0x00008000), (int)0x00008000);
		assertEquals(Bits.msb((int)0x00010000), (int)0x00010000);
		assertEquals(Bits.msb((int)0x00020000), (int)0x00020000);
		assertEquals(Bits.msb((int)0x00040000), (int)0x00040000);
		assertEquals(Bits.msb((int)0x00080000), (int)0x00080000);
		assertEquals(Bits.msb((int)0x00100000), (int)0x00100000);
		assertEquals(Bits.msb((int)0x00200000), (int)0x00200000);
		assertEquals(Bits.msb((int)0x00400000), (int)0x00400000);
		assertEquals(Bits.msb((int)0x00800000), (int)0x00800000);
		assertEquals(Bits.msb((int)0x01000000), (int)0x01000000);
		assertEquals(Bits.msb((int)0x02000000), (int)0x02000000);
		assertEquals(Bits.msb((int)0x04000000), (int)0x04000000);
		assertEquals(Bits.msb((int)0x08000000), (int)0x08000000);
		assertEquals(Bits.msb((int)0x10000000), (int)0x10000000);
		assertEquals(Bits.msb((int)0x20000000), (int)0x20000000);
		assertEquals(Bits.msb((int)0x40000000), (int)0x40000000);
		assertEquals(Bits.msb((int)0x80000000), (int)0x80000000);
		testMsbIntRange((int)0x00000002, (int)0x00000003, (int)0x00000002);
		testMsbIntRange((int)0x00000004, (int)0x00000007, (int)0x00000004);
		testMsbIntRange((int)0x00000008, (int)0x0000000f, (int)0x00000008);
		testMsbIntRange((int)0x00000010, (int)0x0000001f, (int)0x00000010);
		testMsbIntRange((int)0x00000020, (int)0x0000003f, (int)0x00000020);
		testMsbIntRange((int)0x00000040, (int)0x0000007f, (int)0x00000040);
		testMsbIntRange((int)0x00000080, (int)0x000000ff, (int)0x00000080);
		testMsbIntRange((int)0x00000100, (int)0x000001ff, (int)0x00000100);
		testMsbIntRange((int)0x00000200, (int)0x000003ff, (int)0x00000200);
		testMsbIntRange((int)0x00000400, (int)0x000007ff, (int)0x00000400);
		testMsbIntRange((int)0x00000800, (int)0x00000fff, (int)0x00000800);
		testMsbIntRange((int)0x00001000, (int)0x00001fff, (int)0x00001000);
		testMsbIntRange((int)0x00002000, (int)0x00003fff, (int)0x00002000);
		testMsbIntRange((int)0x00004000, (int)0x00007fff, (int)0x00004000);
		testMsbIntRange((int)0x00008000, (int)0x0000ffff, (int)0x00008000);
		testMsbIntRange((int)0x00010000, (int)0x0001ffff, (int)0x00010000);
		testMsbIntRange((int)0x00020000, (int)0x0003ffff, (int)0x00020000);
		testMsbIntRange((int)0x00040000, (int)0x0007ffff, (int)0x00040000);
		testMsbIntRange((int)0x00080000, (int)0x000fffff, (int)0x00080000);
		testMsbIntRange((int)0x00100000, (int)0x001fffff, (int)0x00100000);
		testMsbIntRange((int)0x00200000, (int)0x003fffff, (int)0x00200000);
		testMsbIntRange((int)0x00400000, (int)0x007fffff, (int)0x00400000);
		testMsbIntRange((int)0x00800000, (int)0x00ffffff, (int)0x00800000);
		testMsbIntRange((int)0x01000000, (int)0x01ffffff, (int)0x01000000);
		testMsbIntRange((int)0x02000000, (int)0x03ffffff, (int)0x02000000);
		testMsbIntRange((int)0x04000000, (int)0x07ffffff, (int)0x04000000);
		testMsbIntRange((int)0x08000000, (int)0x0fffffff, (int)0x08000000);
		testMsbIntRange((int)0x10000000, (int)0x1fffffff, (int)0x10000000);
		testMsbIntRange((int)0x20000000, (int)0x3fffffff, (int)0x20000000);
		testMsbIntRange((int)0x40000000, (int)0x7fffffff, (int)0x40000000);
		testMsbIntRange((int)0x80000000, (int)0xffffffff, (int)0x80000000);
	}

}
