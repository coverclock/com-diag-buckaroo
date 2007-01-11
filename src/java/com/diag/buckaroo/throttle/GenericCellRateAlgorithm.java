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
 * af-tm-0056.000, ATM Forum, April 1996 ]. As specified in the standard,
 * ticks are in microseconds. See also "ATM Traffic Management" [ J. L. Sloan,
 * Digital Aggregates Corp., August 2005 ]. This throttle tries to construct a
 * usable traffic contract even in the face of questionable parameters. This is
 * clearly more of an embedded mindset than an enterprise mind set.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 *
 * @date $Date$
 */
public class GenericCellRateAlgorithm implements Throttle {

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
	 */
	public GenericCellRateAlgorithm() {
		this(0, Long.MAX_VALUE);
	}
	
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
	 * @see com.diag.buckaroo.throttle.Throttle#begin()
	 */
	public long admissable() {
		return admissable(time());
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#begin(long)
	 */
	public long admissable(long ticks) {
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
