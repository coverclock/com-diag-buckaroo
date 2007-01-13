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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Provides statistics about the heap. A snapshot of the current heap statistics
 * is taken when this object is constructed. Another such object must be constructed
 * to get subsequent statistics. This approach insures that all four statistics about
 * the heap reflect a consistent state.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class Heap {
	
	static final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
	
	final MemoryUsage usage = memory.getHeapMemoryUsage();
	
	/**
	 * Return the committed (guaranteed) size of the heap in bytes.
	 * @return the committed heap size in bytes.
	 */
	public long getBytesCommitted() { return usage.getCommitted(); }
	
	/**
	 * Return the initial size of the heap in bytes.
	 * @return the initial heap size in bytes.
	 */
	public long getBytesInitial() { return usage.getInit(); }
	
	/**
	 * Return the maximum size of the heap in bytes.
	 * @return the maximum heap size in bytes.
	 */
	public long getBytesMaximum() { return usage.getMax(); }
	
	/**
	 * Return the used size of the heap in bytes.
	 * @return the used heap size in bytes.
	 */
	public long getBytesUsed() { return usage.getUsed(); }
}
