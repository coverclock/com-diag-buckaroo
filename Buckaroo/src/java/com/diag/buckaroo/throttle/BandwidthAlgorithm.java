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

import com.diag.buckaroo.throttle.GenericCellRateAlgorithm;
import com.diag.buckaroo.throttle.ManifoldThrottle;

/**
 * This class extends the Generic Cell Rate Algorithm to accomodate
 * traffic contracts based not on cells per second but on octets per
 * second, and in which admission decisions are not based on the emission
 * of a single cell but on a packet of octets. The math behind the
 * GCRA computes any emission delay based not on the parameters of the
 * current emission but on whether the prior emission is complete.
 * Hence, this Throttle always admits the first packet, regardless
 * of size in octets, but may delay the subsequent packet. This means,
 * somewhat counter-intuitively (to me anyway), that the size of the
 * current packet in octets is passed as a parameter to the commit method,
 * not of the admissible method. The increment and limit are based on the
 * inter-arrival time between successive bytes. (If you want a Throttle
 * that is based on the inter-arrival time between successive packets or
 * some other constant units, you can just use the regular Generic Cell Rate
 * Algorithm or the Cell Rate Throttle.) The frequency of this Throttle
 * is one nanosecond, versus the one microsecond frequency of the Generic
 * Cell Rate Algorithm. Admission decisions are made on a per packet basis,
 * rather than on a per byte basis, for reasons of efficiency. For this reason,
 * and because the entire packet is handed to the underlying platform for transmission
 * at its own rate, data streams rate controlled by this Throttle exhibit burstier
 * behavior than cell streams rate controlled by the similar GenericCellRateAlgorithm.
 * Note that this is not a port of the Desperado C++ class BandwidthThrottle, and
 * differs substantially (it is better) in both design and implementation from that class.
 * Consider the Desperado BandwidthThrottle class to be deprecated and expect a port
 * of this class from Java to C++ sometime in the future.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class BandwidthAlgorithm extends GenericCellRateAlgorithm implements ManifoldThrottle {
	
	public final static int NS_PER_MS = 1000000;
	public final static long NS_PER_S = 1000000000;
	
	/**
	 * Convert the milliseconds used by the JVM to the ticks used by the Throttle,
	 * appropriate for use as an increment.
	 * @param ms is milliseconds.
	 * @return ticks.
	 */
	public static long ms2increment(long ms) { return ms * NS_PER_MS; }
	
	/**
	 * Convert the milliseconds used by the JVM to the ticks used by the Throttle,
	 * appropriate for use as a limit.
	 * @param ms is milliseconds.
	 * @return ticks.
	 */
	public static long ms2limit(long ms) { return ms * NS_PER_MS; }
	
	/**
	 * Convert the nanoseconds used by the JVM to the ticks used by the Throttle,
	 * appropriate for use as an increment.
	 * @param ns is nanoseconds.
	 * @return ticks.
	 */
	public static long ns2increment(long ns) { return ns; }
	
	/**
	 * Convert the nanoseconds used by the JVM to the ticks used by the Throttle,
	 * appropriate for use as a limit.
	 * @param ns is nanoseconds.
	 * @return ticks.
	 */
	public static long ns2limit(long ns) { return ns; }
		
	/**
	 * Convert the ticks used by the Throttle to the milliseconds used by the JVM,
	 * rounding up by the ceiling, appropriate as the sole parameter for
	 * Thread.sleep(milliseconds).
	 * @param ns is ticks.
	 * @return milliseconds.
	 */
	public static long delay2ms(long ns) { return (ns + NS_PER_MS - 1) / NS_PER_MS; }
	
	/**
	 * Convert the ticks used by the Throttle to the milliseconds used by the JVM,
	 * extract just the whole number of milliseconds, appropriate for the first parameter
	 * of Thread.sleep(milliseconds,nanoseconds).
	 * @param ns is ticks.
	 * @return milliseconds.
	 */
	public static long delay2ms1(long ns) { return ns / NS_PER_MS; }
	
	/**
	 * Convert the ticks used by the Throttle to the nanoseconds used by the JVM,
	 * extract just the fractional number of nanoseconds less than a millisecond,
	 * appropriate for the second parameter of Thread.sleep(milliseconds,nanoseconds).
	 * @param ns is ticks.
	 * @return nanoseconds.
	 */
	public static int delay2ns2(long ns) { return (int)(ns % NS_PER_MS); }

	/**
	 * Ctor.
	 * @param increment is the virtual scheduler increment or i in microseconds.
	 * @param limit is the virtual scheduler limit or l in microseconds.
	 */
	public BandwidthAlgorithm(long increment, long limit) {
		super(increment, limit);
	}
	
	/**
	 * Ctor. The limit is zero to zero microseconds.
	 * @param increment is the virtual scheduler increment or i in microseconds.
	 */
	public BandwidthAlgorithm(long increment) {
		super(increment);
	}
	
	/**
	 * Ctor. The increment is set to zero microseconds and the limit is
	 * set to the maximum possible value.
	 */
	public BandwidthAlgorithm() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#commit()
	 */
	public boolean commit() {
		return commit(1);
	}

	/**
	 * Commit the throttle state computing the virtual scheduler expected
	 * elapsed ticks based on the specified number of octets. This is an extension
	 * of the Throttle interface.
	 * @param octets is the number of octets to be emitted.
	 * @return true if the throttle is not currently alarmed, false otherwise.
	 */
	public boolean commit(int octets) {
		then = now;
		x = x1 + (octets * increment);
		alarmed = alarmed1;
		return !alarmed;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#frequency()
	 */
	public long frequency() {
		return NS_PER_S;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#time()
	 */
	public long time() {
		return System.nanoTime();
	}

}
