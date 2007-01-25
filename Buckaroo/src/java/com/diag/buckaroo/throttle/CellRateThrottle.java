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

import com.diag.buckaroo.throttle.CompoundThrottle;
import com.diag.buckaroo.throttle.GenericCellRateAlgorithm;

/**
 * This class implements a Cell Rate Throttle using one or two Generic Cell Rate Algorithms
 * to support either a constant bit rate (CBR) or a variable bit rate (VBR) traffic contract.
 * As per "Traffic Management Specification 4.0" specification [ Giroux, N., et al.,
 * af-tm-0056.000, ATM Forum, April 1996 ] all time durations are in microseconds.
 * Although this throttle is for historical reasons defined in terms of emission
 * units of ATM cells, you can think of cells as any kind of event: packets, log
 * messages, requests, etc. This throttle tries to construct a usable traffic contract
 * even in the face of questionable parameters. This is clearly more of an embedded
 * mindset than an enterprise mind set in which the constructor would just throw an
 * exception. A CBR traffic contract based just on the Peak Cell Rate (PCR) and the Cell
 * Delay Variation Tolerance (CDVT) would use just one GCRA, GenericCellRateAlgorithm(1/PCR,
 * CDVT), where CDVT may default to zero. A VBR traffic contract based on PCR and CDVT plus
 * the Sustained Cell Rate (SCR) and the Maximum Burst Size (MBS) would use two of GCRAs
 * in conjunction, the prior GCRA, plus GenericCellRateAlgorithm(1/SCR,
 * CDVT+(MBS-1)*((1/SCR)-(1/PCR))), where conforming cells would have to meet both contracts
 * simultaneously, and where again CDVT may default to zero. Cell streams conforming to a
 * CBR contract must meet the PCR with CDVT limiting the maximum total jitter in the cell
 * stream. Cell streams conforming to a VBR contract must meet the SCR in the long run but
 * may burst as many as MBS cells at PCR with the specified CDVT. This class is a port not
 * of the Desperado C++ class CellRateThrottle but rather is more similar to the class
 * CompoundCellRateThrottle.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class CellRateThrottle extends CompoundThrottle {

	protected static final long FREQUENCY = new GenericCellRateAlgorithm().frequency();
	
	/**
	 * Convert the ticks used by the Throttle to the milliseconds used by the JVM,
	 * rounding up by the ceiling, appropriate as the sole parameter for
	 * Thread.sleep(milliseconds).
	 * @param us is ticks.
	 * @return milliseconds.
	 */
	public static long delay2ms(long us) { return GenericCellRateAlgorithm.delay2ms(us); }
	
	/**
	 * Convert the ticks used by the Throttle to the milliseconds used by the JVM,
	 * extract just the whole number of milliseconds, appropriate for the first parameter
	 * of Thread.sleep(milliseconds,nanoseconds).
	 * @param us is ticks.
	 * @return milliseconds.
	 */
	public static long delay2ms1(long us) { return GenericCellRateAlgorithm.delay2ms1(us); }
	
	/**
	 * Convert the ticks used by the Throttle to the nanoseconds used by the JVM,
	 * extract just the fractional number of nanoseconds less than a millisecond,
	 * appropriate for the second parameter of Thread.sleep(milliseconds,nanoseconds).
	 * @param us is ticks.
	 * @return nanoseconds.
	 */
	public static int delay2ns2(long us) { return GenericCellRateAlgorithm.delay2ns2(us); }
	
	/**
	 * Compute the GCRA increment for a constant bit rate (CBR) traffic contract.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param cdvt is the cell delay variation tolerance in microseconds.
	 * @return the increment in ticks.
	 */
	public static long increment(int pcr, int cdvt) {
		long p = (pcr > 0) ? pcr : 0;
		long i = (p > 0) ? (FREQUENCY + p - 1) / p : Long.MAX_VALUE;
		return (i >= 0) ? i : Long.MAX_VALUE;
	}

	/**
	 * Compute the GCRA limit for a constant bit rate (CBR) traffic contract.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param cdvt is the cell delay variation tolerance in microseconds.
	 * @return the limit in ticks.
	 */	
	public static long limit(int pcr, int cdvt) {
		long c = (cdvt >= 0) ? cdvt : 0;
		long l = c;
		return l;
	}
	
	/**
	 * Compute the GCRA increment for a variable bit rate (VBR) traffic contract.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param cdvt is the cell delay variation tolerance in microseconds.
	 * @param scr is the sustained cell rate in cells per second which must be less than or equal to the pcr.
	 * @param mbs is the maximum burst size in cells.
	 * @return the increment in ticks.
	 */
	public static long increment(int pcr, int cdvt, int scr, int mbs) {
		long s = (scr > 0) ? scr : 0;
		long p = (pcr > 0) ? pcr : 0;
		if (s > p) { s = p; }
		long i = (s > 0) ? (FREQUENCY + s - 1) / s : Long.MAX_VALUE;
		return (i >= 0) ? i : Long.MAX_VALUE;
	}

	/**
	 * Compute the GCRA limit for a variable bit rate (VBR) traffic contract.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param cdvt is the cell delay variation tolerance in microseconds.
	 * @param scr is the sustained cell rate in cells per second.
	 * @param mbs is the maximum burst size in cells.
	 * @return the limit in ticks.
	 */		
	public static long limit(int pcr, int cdvt, int scr, int mbs) {
		long l = limit(pcr, cdvt);
		if ((mbs > 1) && (scr > 0) && (pcr > scr)) {
			l += (mbs - 1) * (increment(pcr, cdvt, scr, mbs) - increment(pcr, cdvt));
		}
		return (l >= 0) ? l : Long.MAX_VALUE;
	}

	/**
	 * Ctor for a variable bit rate (VBR) traffic contract.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param cdvt is the cell delay variation (jitter) tolerance in microseconds.
	 * @param scr is the sustained cell rate in cells per second.
	 * @param mbs is the maximum burst size in cells.
	 */
	public CellRateThrottle(int pcr, int cdvt, int scr, int mbs) {
		super(
		    new GenericCellRateAlgorithm(increment(pcr, cdvt), limit(pcr, cdvt)),
			new GenericCellRateAlgorithm(increment(pcr, cdvt, scr, mbs), limit(pcr, cdvt, scr, mbs))
		);
	}

	/**
	 * Ctor for a variable bit rate (VBR) traffic contract with a CDVT of zero microseconds.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param scr is the sustained cell rate in cells per second.
	 * @param mbs is the maximum burst size in cells.
	 */
	public CellRateThrottle(int pcr, int scr, int mbs) {
		this(pcr, 0, scr, mbs);
	}
	
	/**
	 * Ctor for a constant bit rate (CBR) traffic contract.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param cdvt is the cell delay variation (jitter) tolerance in microseconds.
	 */
	public CellRateThrottle(int pcr, int cdvt) {
		super(
	        new GenericCellRateAlgorithm(increment(pcr, cdvt), limit(pcr, cdvt))
		);
	}
	
	/**
	 * Ctor for a constant bit rate (CBR) traffic contract with a CDVT of zero microseconds.
	 * @param pcr is the peak cell rate in cells per second.
	 */
	public CellRateThrottle(int pcr) {
		this(pcr, 0);
	}
	
	/**
	 * Ctor for a constant bit rate (CBR) traffic contract with a peak cell rate (PCR)
	 * set to the maximum possible value and the cell delay variation tolerance (CDVT)
	 * set to zero microseconds.
	 */
	public CellRateThrottle() {
		this(Integer.MAX_VALUE);
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#toString()
	 */
	public String toString() {
		return CellRateThrottle.class.getSimpleName() + "{" + super.toString() + "}";
	}

}
