/**
 * Copyright 2007-2013 Digital Aggregates Corporation, Colorado, USA.
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

import com.diag.buckaroo.throttle.CompoundThrottle;
import com.diag.buckaroo.throttle.ExtendedThrottle;

/**
 * Combines one or two Extended Throttles together to form a compound 
 * Extended Throttle. If there are two Throttles, conformant events must
 * meet the traffic contracts of both Throttles. Typically if one Throttle
 * is used it implements a Constant Bit Rate (CBR) contract with just a peak
 * Extended Throttle, and if two Throttles are used it implements a Variable Bit
 * Rate (VBR) contract with both a peak and a sustained Extended Throttle. A Compound
 * Extended Throttle returns the frequency and time of the peak Extended Throttle.
 * This is similar to the Desperado C++ class CompoundThrottle.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class CompoundExtendedThrottle extends CompoundThrottle implements ExtendedThrottle {

	private ExtendedThrottle peak;
	private ExtendedThrottle sustained;
	
	/**
	 * Ctor for a variable bit rate (VBR) traffic contract.
	 * @param peak is the Extended Throttle with the contract for the peak emission rate.
	 * @param sustained is the Extended Throttle with the contract for the sustained emission rate.
	 */
	public CompoundExtendedThrottle(ExtendedThrottle peak, ExtendedThrottle sustained) {
		super(peak, sustained);
		this.peak = peak;
		this.sustained = sustained;
	}
	
	/**
	 * Ctor for a constant bit rate (CBR) traffic contract.
	 * @param peak is the Extended Throttle with the contract for the peak emission rate.
	 */
	public CompoundExtendedThrottle(ExtendedThrottle peak) {
		this(peak, CompoundThrottle.PROMISCUOUS);
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.ExtendedThrottle#commit(int)
	 */
	public boolean commit(int count) {
		boolean peakCommit = peak.commit(count);
		boolean sustainedCommit = sustained.commit(count);
		return peakCommit && sustainedCommit;
	}
	
	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#toString()
	 */
	public String toString() {
		return CompoundExtendedThrottle.class.getSimpleName()
			+ "{" + super.toString()
			+ ",peak=" + peak.toString()
			+ ",sustained=" + sustained.toString()
			+ "}";
	}

}
