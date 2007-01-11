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

import com.diag.buckaroo.throttle.GenericCellRateAlgorithm;
import com.diag.buckaroo.throttle.OptimisticThrottle;
import com.diag.buckaroo.throttle.Throttle;

/**
 * Implements a cell rate throttle using two Generic Cell Rate Algorithms to support
 * either a constant bit rate (CBR) or a variable bit rate (VBR) traffic contract.
 * As per "Traffic Management Specification 4.0" specification [ Giroux, N., et al.,
 * af-tm-0056.000, ATM Forum, April 1996 ] all time durations are in microseconds.
 * Although this throttle is for historical reasons defined in terms of emission
 * units of ATM cells, you can think of cells as any kind of event: packets, log
 * messages, requests, etc. This throttle tries to construct a usable traffic contract
 * even in the face of questionable parameters. This is clearly more of an embedded
 * mindset than an enterprise mind set.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 *
 * @date $Date$
 */
public class CellRateThrottle implements Throttle {

	static final Throttle GCRA = new GenericCellRateAlgorithm();
	static final Throttle OPTIMIST = new OptimisticThrottle();
	
	Throttle peak;
	Throttle sustained;

	/**
	 * Compute the GCRA increment for a constant bit rate (CBR) traffic contract.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param cdvt is the cell delay variation tolerance in microseconds.
	 * @return the increment in microseconds.
	 */
	public static long increment(int pcr, int cdvt) {
		long p = (pcr > 0) ? pcr : 0;
		long f = GCRA.frequency();
		long i = (p > 0) ? (f + p - 1) / p : Long.MAX_VALUE;
		return (i >= 0) ? i : Long.MAX_VALUE;
	}

	/**
	 * Compute the GCRA limit for a constant bit rate (CBR) traffic contract.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param cdvt is the cell delay variation tolerance in microseconds.
	 * @return the limit in microseconds.
	 */	
	public static long limit(int pcr, int cdvt) {
		long c = (cdvt >= 0) ? cdvt : 0;
		long l = c;
		return l;
	}
	
	/**
	 * Compute the increment for a variable bit rate (VBR) traffic contract.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param cdvt is the cell delay variation tolerance in microseconds.
	 * @param scr is the sustainable cell rate in cells per second.
	 * @param mbs is the maximum burst size in cells.
	 * @return the increment in microseconds.
	 */
	public static long increment(int pcr, int cdvt, int scr, int mbs) {
		long s = scr;
		long f = GCRA.frequency();
		long i = (s > 0) ? (f + s - 1) / s : Long.MAX_VALUE;
		return (i >= 0) ? i : Long.MAX_VALUE;
	}

	/**
	 * Compute the GCRA limit for a variable bit rate (VBR) traffic contract.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param cdvt is the cell delay variation tolerance in microseconds.
	 * @param scr is the sustainable cell rate in cells per second.
	 * @param mbs is the maximum burst size in cells.
	 * @return the limit in microseconds.
	 */		
	public static long limit(int pcr, int cdvt, int scr, int mbs) {
		long l = limit(pcr, cdvt);
		if ((mbs > 1) && (scr > 0) && (pcr > scr)) {
			l += (mbs - 1) * (increment(pcr, cdvt, scr, mbs) - increment(pcr, cdvt));
		}
		return (l >= 0) ? l : Long.MAX_VALUE;
	}
	
	/**
	 * Ctor for a constant bit rate (CBR) traffic contract.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param cdvt is the cell delay variation (jitter) tolerance in microseconds.
	 */
	public CellRateThrottle(int pcr, int cdvt) {
		peak = new GenericCellRateAlgorithm(increment(pcr, cdvt), limit(pcr, cdvt));
		sustained = OPTIMIST;
	}
	
	/**
	 * Ctor for a constant bit rate (CBR) traffic contract with a CDVT of zero.
	 * @param pcr is the peak cell rate in cells per second.
	 */
	public CellRateThrottle(int pcr) {
		this(pcr, 0);
	}

	/**
	 * Ctor for a variable bit rate (VBR) traffic contract.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param cdvt is the cell delay variation (jitter) tolerance in microseconds.
	 * @param scr is the sustainable cell rate in cells per second.
	 * @param mbs is the maximum burst size in cells.
	 */
	public CellRateThrottle(int pcr, int cdvt, int scr, int mbs) {
		this(pcr, cdvt);
		sustained = new GenericCellRateAlgorithm(increment(pcr, cdvt, scr, mbs), limit(pcr, cdvt, scr, mbs));
	}

	/**
	 * Ctor for a variable bit rate (VBR) traffic contract with a CDVT of zero.
	 * @param pcr is the peak cell rate in cells per second.
	 * @param scr is the sustainable cell rate in cells per second.
	 * @param mbs is the maximum burst size in cells.
	 */
	public CellRateThrottle(int pcr, int scr, int mbs) {
		this(pcr, 0, scr, mbs);
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
	 * @see com.diag.buckaroo.throttle.Throttle#begin()
	 */
	public long admissable() {
		return admissable(time());
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#begin(long)
	 */
	public long admissable(long ticks) {
		long peakAdmissable = peak.admissable(ticks);
		long sustainedAdmissable = sustained.admissable(ticks);
		return (peakAdmissable > sustainedAdmissable) ? peakAdmissable : sustainedAdmissable;
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

	public String toString() {
		return this.getClass().getName()
			+ "{peak=" + peak.toString()
			+ ",sustained=" + sustained.toString()
			+ "}";
	}
}
