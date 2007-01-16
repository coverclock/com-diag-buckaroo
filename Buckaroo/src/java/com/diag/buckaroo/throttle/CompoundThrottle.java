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

import com.diag.buckaroo.throttle.ManifoldThrottle;
import com.diag.buckaroo.throttle.PromiscuousThrottle;
import com.diag.buckaroo.throttle.Throttle;

/**
 * Combines one or two Throttles together to form a compound Throttle.
 * If there are two Throttles, conformant events must meet the traffic
 * contracts of both Throttles. Typically if one Throttle is used it
 * implements a Constant Bit Rate (CBR) contract with just a peak emission
 * rate, and if two Throttles are used it implements a Variable Bit Rate (VBR)
 * contract with both a peak and a sustained emission rate.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class CompoundThrottle implements Throttle {

	static final ManifoldThrottle PROMISCUOUS = new PromiscuousThrottle();
	
	Throttle peak;
	Throttle sustained;

	/**
	 * Ctor for a variable bit rate (VBR) traffic contract.
	 * @param peak is the Throttle with the contract for the peak emission rate.
	 * @param sustained is the Throttle with the contract for the sustained emission rate.
	 */
	public CompoundThrottle(Throttle peak, Throttle sustained) {
		this.peak = peak;
		this.sustained = sustained;
	}
	
	/**
	 * Ctor for a constant bit rate (CBR) traffic contract.
	 * @param peak is the Throttle with the contract for the peak emission rate.
	 */
	public CompoundThrottle(Throttle peak) {
		this(peak, PROMISCUOUS);
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
		peak.reset(ticks);
		sustained.reset(ticks);
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
		long peakAdmissible = peak.admissible(ticks);
		long sustainedAdmissible = sustained.admissible(ticks);
		return (peakAdmissible > sustainedAdmissible) ? peakAdmissible : sustainedAdmissible;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#commit()
	 */
	public boolean commit() {
		boolean peakCommit = peak.commit();
		boolean sustainedCommit = sustained.commit();
		return peakCommit && sustainedCommit;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#rollback()
	 */
	public boolean rollback() {
		boolean peakRollback = peak.rollback();
		boolean sustainedRollback = sustained.rollback();
		return peakRollback && sustainedRollback;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#isAlarmed()
	 */
	public boolean isAlarmed() {
		boolean peakAlarm = peak.isAlarmed();
		boolean sustainedAlarm = sustained.isAlarmed();
		return peakAlarm || sustainedAlarm;
	}
	
	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#isValid()
	 */
	public boolean isValid() {
		boolean peakValid = peak.isValid();
		boolean sustainedValid = sustained.isValid();
		return peakValid && sustainedValid;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#frequency()
	 */
	public long frequency() {
		return peak.frequency();
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#time()
	 */
	public long time() {
		return peak.time();
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#toString()
	 */
	public String toString() {
		return this.getClass().getName()
			+ "{peak=" + peak.toString()
			+ ",sustained=" + sustained.toString()
			+ "}";
	}
	
}
