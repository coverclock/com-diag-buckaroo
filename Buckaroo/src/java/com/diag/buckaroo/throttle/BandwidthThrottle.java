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

import com.diag.buckaroo.throttle.BandwidthAlgorithm;
import com.diag.buckaroo.throttle.CompoundExtendedThrottle;


/**
 * This class implements a Bandwidth Throttle using one or two Bandwidth Algorithms
 * to support either a constant bit rate (CBR) or a variable bit rate (VBR) traffic contract.
 * This throttle tries to construct a usable traffic contract even in the face of
 * questionable parameters. This is clearly more of an embedded mindset than an enterprise
 * mind set in which the constructor would just throw an exception. A CBR traffic contract
 * based just on the Peak Byte Rate (PBR) and the Jitter Tolerance (JT) would use just one
 * BA, BandwidthAlgorithm(1/PBR, JT), where JT may default to zero. A VBR traffic contract
 * based on PBR and JT plus the Sustained Byte Rate (SBR) and the Maximum Burst Size
 * (MBS) would use two of BAs in conjunction, the prior BA, plus BandwidthAlgorithm(1/SBR,
 * (MBS-1)*((1/SBR)-(1/PBR))), where conforming bytes would have to conform to both
 * contracts simultaneously, and where again JT may default to zero. Data streams conforming
 * to a CBR contract must meet the PBR with JT limiting the maximum total jitter in the
 * byte stream. Data streams conforming to a VBR contract must meet the SBR in the long run
 * but may burst as many as MBS bytes at PBR with the specified JT. Admission decisions
 * are made on a per packet basis, rather than on a per byte basis, for reasons of efficiency.
 * For this reason, and because the entire packet is handed to the underlying platform for
 * transmission at its own rate, data streams rate controlled by this Throttle exhibit
 * burstier behavior than cell streams rate controlled by the similar CellRateThrottle.
 * This is similar to the Desperado C++ class BandwidthThrottle.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class BandwidthThrottle extends CompoundExtendedThrottle {

	protected static final long FREQUENCY = new BandwidthAlgorithm().frequency();
	
	/**
	 * Convert the ticks used by the Throttle to the milliseconds used by the JVM,
	 * rounding up by the ceiling, appropriate as the sole parameter for
	 * Thread.sleep(milliseconds).
	 * @param us is ticks.
	 * @return milliseconds.
	 */
	public static long delay2ms(long us) { return BandwidthAlgorithm.delay2ms(us); }
	
	/**
	 * Convert the ticks used by the Throttle to the milliseconds used by the JVM,
	 * extract just the whole number of milliseconds, appropriate for the first parameter
	 * of Thread.sleep(milliseconds,nanoseconds).
	 * @param us is ticks.
	 * @return milliseconds.
	 */
	public static long delay2ms1(long us) { return BandwidthAlgorithm.delay2ms1(us); }
	
	/**
	 * Convert the ticks used by the Throttle to the nanoseconds used by the JVM,
	 * extract just the fractional number of nanoseconds less than a millisecond,
	 * appropriate for the second parameter of Thread.sleep(milliseconds,nanoseconds).
	 * @param us is ticks.
	 * @return nanoseconds.
	 */
	public static int delay2ns2(long us) { return BandwidthAlgorithm.delay2ns2(us); }

	/**
	 * Compute the BA increment for a constant bit rate (CBR) traffic contract.
	 * @param pbr is the peak byte rate in bytes per second.
	 * @param jt is the jitter tolerance in nanoseconds.
	 * @return the increment in ticks.
	 */
	public static long increment(int pbr, int jt) {
		long p = (pbr > 0) ? pbr : 0;
		long i = (p > 0) ? (FREQUENCY + p - 1) / p : Long.MAX_VALUE;
		return (i >= 0) ? i : Long.MAX_VALUE;
	}

	/**
	 * Compute the BA limit for a constant bit rate (CBR) traffic contract.
	 * @param pbr is the peak byte rate in bytes per second.
	 * @param jt is the jitter tolerance in nanoseconds.
	 * @return the limit in ticks.
	 */	
	public static long limit(int pbr, int jt) {
		long j = (jt > 0) ? jt : 0;
		long l = j;
		return l;
	}
	
	/**
	 * Compute the BA increment for a variable bit rate (VBR) traffic contract.
	 * @param pbr is the peak byte rate in bytes per second.
	 * @param jt is the jitter tolerance in nanoseconds.
	 * @param sbr is the sustained byte rate in bytes per second.
	 * @param mbs is the maximum burst size in bytes.
	 * @return the increment in ticks.
	 */
	public static long increment(int pbr, int jt, int sbr, int mbs) {
		long s = (sbr > 0) ? sbr : 0;
		long p = (pbr > 0) ? pbr : 0;
		if (s > p) { s = p; }
		long i = (s > 0) ? (FREQUENCY + s - 1) / s : Long.MAX_VALUE;
		return (i >= 0) ? i : Long.MAX_VALUE;
	}

	/**
	 * Compute the BA limit for a variable bit rate (VBR) traffic contract.
	 * @param pbr is the peak byte rate in bytes per second.
	 * @param jt is the jitter tolerance in nanoseconds.
	 * @param sbr is the sustained byte rate in bytes per second.
	 * @param mbs is the maximum burst size in bytes.
	 * @return the limit in ticks.
	 */		
	public static long limit(int pbr, int jt, int sbr, int mbs) {
		long l = limit(pbr, jt);
		if ((mbs > 1) && (sbr > 0) && (pbr > sbr)) {
			l += (mbs - 1) * (increment(pbr, jt, sbr, mbs) - increment(pbr, jt));
		}
		return (l >= 0) ? l : Long.MAX_VALUE;
	}

	/**
	 * Ctor for a variable bit rate (VBR) traffic contract.
	 * @param pbr is the peak byte rate in bytes per second.
	 * @param jt is the jitter tolerance in nanoseconds.
	 * @param sbr is the sustained byte rate in bytes per second.
	 * @param mbs is the maximum burst size in bytes.
	 */
	public BandwidthThrottle(int pbr, int jt, int sbr, int mbs) {
		super(
			new BandwidthAlgorithm(increment(pbr, jt), limit(pbr, jt)),
		    new BandwidthAlgorithm(increment(pbr, jt, sbr, mbs), limit(pbr, jt, sbr, mbs))
		);
	}

	/**
	 * Ctor for a variable bit rate (VBR) traffic contract with a JT of zero nanoseconds.
	 * @param pbr is the peak byte rate in bytes per second.
	 * @param sbr is the sustained byte rate in bytes per second.
	 * @param mbs is the maximum burst size in bytes.
	 */
	public BandwidthThrottle(int pbr, int sbr, int mbs) {
		this(pbr, 0, sbr, mbs);
	}
	
	/**
	 * Ctor for a constant bit rate (CBR) traffic contract.
	 * @param pbr is the peak byte rate in bytes per second.
	 * @param jt is the jitter tolerance in nanoseconds.
	 */
	public BandwidthThrottle(int pbr, int jt) {
		super(
			new BandwidthAlgorithm(increment(pbr, jt), limit(pbr, jt))
		);
	}
	
	/**
	 * Ctor for a constant bit rate (CBR) traffic contract with a JT of zero nanoseconds.
	 * @param pbr is the peak byte rate in bytes per second.
	 */
	public BandwidthThrottle(int pbr) {
		this(pbr, 0);
	}
	
	/**
	 * Ctor for a constant bit rate (CBR) traffic contract with a peak byte rate (PBR)
	 * set to the maximum possible value and the jitter tolerance (JT) set to zero
	 * nanoseconds.
	 */
	public BandwidthThrottle() {
		this(Integer.MAX_VALUE);
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#toString()
	 */
	public String toString() {
		return BandwidthThrottle.class.getSimpleName() + "{" + super.toString() + "}";
	}
}
