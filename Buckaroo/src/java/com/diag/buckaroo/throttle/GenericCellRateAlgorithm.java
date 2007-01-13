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

/**
 * This class implements a Generic Cell Rate Algorithm (GCRA) as specified in
 * the "Traffic Management Specification 4.0" specification [ Giroux, N., et al.,
 * af-tm-0056.000, ATM Forum, April 1996, pp. 62-67 ]. As specified in the standard,
 * ticks are in microseconds. See also "ATM Traffic Management" [ J. L. Sloan,
 * http://www.diag.com/reports/ATMTrafficManagement.html, Digital Aggregates Corp.,
 * August 2005,  ]. Although this throttle is for historical reasons defined in terms
 * of emission units of ATM cells, you can think of cells as any kind of event: packets, log
 * messages, requests, etc. This throttle tries to construct a usable traffic contract even
 * in the face of questionable parameters. This is clearly more of an embedded mindset
 * than an enterprise mind set in which the constructor would just throw an exception.
 * The parameters of the traffic contract are the Increment (the interarrival time in
 * ticks between conforming cells) and the Limit (the total time in ticks that a cell
 * stream may deviate from conformance). A constant bit rate (CBR) traffic contract based
 * just on the Peak Cell Rate (PCR) and the Cell Delay Variation Tolerance (CDVT) would use
 * just one of these throttles, GenericCellRateAlgorithm(PCR,CDVT). A variable bit rate
 * (VBR) traffic contract based on PCR and CDVT plus the Sustainable Cell Rate (SCR) and the
 * Maximum Burst Size (MBS) would use two of these throttles in conjunction, the prior
 * throttle and GenericCellRateAlgorithm(1/SCR,(MBS-1)*((1/PCR)-(1/SCR))) where conforming
 * cells would have to conform to both contracts simultaneously.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class GenericCellRateAlgorithm implements Throttle {
	
	/**
	 * Convert the milliseconds used by the JVM to the microseconds used by the Throttle,
	 * appropriate for use as an increment.
	 * @param ms is milliseconds.
	 * @return microseconds.
	 */
	public static long ms2increment(long ms) { return ms * 1000; }
	
	/**
	 * Convert the milliseconds used by the JVM to the microseconds used by the GCRA,
	 * appropriate for use as a limit.
	 * @param ms is milliseconds.
	 * @return microseconds.
	 */
	public static long ms2limit(long ms) { return ms * 1000; }
	
	/**
	 * Convert the nanoseconds used by the JVM to the microseconds used by the Throttle,
	 * rounding up by the ceiling, appropriate for use as an increment.
	 * @param ns is nanoseconds.
	 * @return microseconds.
	 */
	public static long ns2increment(long ns) { return (ns + 1000 - 1) / 1000; }
	
	/**
	 * Convert the nanoseconds used by the JVM to the microseconds used by the Throttle,
	 * truncating by the floor, appropriate for use as a limit.
	 * @param ns is nanoseconds.
	 * @return microseconds.
	 */
	public static long ns2limit(long ns) { return ns  / 1000; }
	
	/**
	 * Convert the microseconds used by the Throttle to the milliseconds used by the JVM,
	 * rounding up by the ceiling, appropriate as the sole parameter for
	 * Thread.sleep(milliseconds).
	 * @param us is microseconds.
	 * @return milliseconds.
	 */
	public static long delay2ms(long us) { return (us + 1000 - 1) / 1000; }
	
	/**
	 * Convert the microseconds used by the Throttle to the milliseconds used by the JVM,
	 * extract just the whole number of milliseconds, appropriate for the first parameter
	 * of Thread.sleep(milliseconds,nanoseconds).
	 * @param us is microseconds.
	 * @return milliseconds.
	 */
	public static long delay2ms1(long us) { return us / 1000; }
	
	/**
	 * Convert the microseconds used by the Throttle to the nanoseconds used by the JVM,
	 * extract just the fractional number of nanoseconds less than a millisecond,
	 * appropriate for the second parameter of Thread.sleep(milliseconds,nanoseconds).
	 * @param us is microseconds.
	 * @return nanoseconds.
	 */
	public static int delay2ns2(long us) { return ((int)(us % 1000)) * 1000; }

	long now;			// time of the most recent attempted emission in usec since the epoch
	long then;			// time of the most recent committed emission in usec since the epoch
	long increment;		// virtual scheduler increment, from TM 4.0
	long limit;			// virtual scheduler limit, from TM 4.0
	long x;				// virtual scheduler expected elapsed ticks, from TM 4.0
	long x1;			// virtual scheduler actual elapsed ticks, from TM 4.0
	boolean alarmed;	// alarm state
	boolean alarmed1;	// candidate alarm state
	
	/**
	 * Ctor.
	 * @param increment is the virtual scheduler increment or i in microseconds.
	 * @param limit is the virtual scheduler limit or l in microseconds.
	 */
	public GenericCellRateAlgorithm(long increment, long limit) {
		this.increment = (increment > 0) ? increment : 0;
		this.limit = (limit > 0) ? limit : 0;
		reset();
	}
	
	/**
	 * Ctor. The limit is zero to zero microseconds.
	 * @param increment is the virtual scheduler increment or i in microseconds.
	 */
	public GenericCellRateAlgorithm(long increment) {
		this(increment, 0);
	}
	
	/**
	 * Ctor. The increment is set to zero microseconds and the limit is
	 * set to the maximum possible value.
	 */
	public GenericCellRateAlgorithm() {
		this(0, Long.MAX_VALUE);
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#reset()
	 */
	public void reset() {
		reset(time());
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#reset(long)
	 */
	public void reset(long ticks) {
		x = 0;
		x1 = 0;
		now = ticks;
		then = ticks - increment;
		alarmed = false;
		alarmed1 = false;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#admissible()
	 */
	public long admissible() {
		return admissible(time());
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#admissible(long)
	 */
	public long admissible(long ticks) {
		long delay = 0;
		alarmed1 = false;
		now = ticks;
		long elapsed = now - then;
		if (x <= elapsed) {
			x1 = 0;
		} else {
			x1 = x - elapsed;
			if (x1 > limit)
			{
				delay = x1 - limit;
				alarmed1 = true;
			}
		}
		return delay;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#commit()
	 */
	public boolean commit() {
		then = now;
		x = x1 + increment;
		alarmed = alarmed1;
		return !alarmed;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#rollback()
	 */
	public boolean rollback() {
		return !alarmed;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#isAlarmed()
	 */
	public boolean isAlarmed() {
		return alarmed;
	}
	
	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#isValid()
	 */
	public boolean isValid() {
		return (now >= 0) || (increment >= 0) && (limit >= 0) && (x >= 0) && (x1 >= 0);
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#frequency()
	 */
	public long frequency() {
		return 1000000;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#time()
	 */
	public long time() {
		return System.nanoTime() / 1000;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#toString()
	 */
	public String toString() {
		return this.getClass().getName()
			+ "{now=" + now
			+ ",then=" + then
			+ ",i=" + increment
			+ ",l=" + limit
			+ ",x=" + x
			+ ",x1=" + x1
			+ ",alarmed=" + alarmed
			+ ",alarmed2=" + alarmed1
			+ "}";
	}
}