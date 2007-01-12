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
 * This class implements a Geometric throttle which is an event-based rather than a
 * time-based throttle. It admits events following a geometric progression: the first
 * event, the second event, the fourth event, the eighth event, up to and including the
 * thirtieth event, after which no more events are admitted until the throttle is reset.
 * This is very useful in embedded systems for controlling error messages to a log or a
 * console; only emit the error message if it is admissible, and reset the Geometric
 * throttle when the error clears. Several error messages are emitted initially, then with
 * ever decreasing frequency, then no more. This is a good pattern if the log is being
 * watched in real-time, while at the same time not flooding the log with closely spaced
 * messages. (This class is a port of the misnamed Desperado C++ class ExponentialThrottle.)
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 *
 * @date $Date$
 *
 */
public class GeometricThrottle implements Throttle {
	
	int consecutive;
	int consecutive1;
	int countdown;
	int countdown1;
	boolean alarmed;
	boolean alarmed1;
	
	/**
	 * Ctor.
	 */
	public GeometricThrottle() {
		reset();
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#reset()
	 */
	public void reset() {
		consecutive = 1;
		consecutive1 = 1;
		countdown = 1;
		countdown1 = 1;
		alarmed = false;
		alarmed1 = false;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#reset(long)
	 */
	public void reset(long ticks) { reset(); }

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#admissible()
	 */
	public long admissible() {
	    long delay = 0;

	    do {
	        countdown1 = countdown;
	        consecutive1 = consecutive;
	        // The Desperado C++ version of this code depends on unsigned
	        // arithmetic, which doesn't exist in Java. For once, the C++
	        // code is more elegant.
	        if (countdown1 > 0) {
	            --countdown1;
	            if (countdown1 == 0) {
	                consecutive1 <<= 1;
	                if (consecutive1 > 0) {
	                	countdown1 = consecutive1 - consecutive;
	                	alarmed1 = false;
	                	break;
	                }
	            }
	        }
	        alarmed1 = true;
	        delay = -1;
	    } while (false);

	    return delay;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#admissible(long)
	 */
	public long admissible(long ticks) { return admissible(); }

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#commit()
	 */
	public boolean commit() {
	    alarmed = alarmed1;
	    return rollback();
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#rollback()
	 */
	public boolean rollback() {
	    countdown = countdown1;
	    consecutive = consecutive1;
		return !alarmed;
	}

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#isAlarmed()
	 */
	public boolean isAlarmed() { return alarmed; }
	
	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#isValid()
	 */
	public boolean isValid() { return true; }

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#frequency()
	 */
	public long frequency() { return -1; }

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#time()
	 */
	public long time() { return -1; }
	
	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#toString()
	 */
	public String toString() {
		return this.getClass().getName()
		+ "{consecutive=0x" + Integer.toHexString(consecutive)
		+ ",consecutive2=0x" + Integer.toHexString(consecutive1)
		+ ",countdown=" + countdown
		+ ",countdown2=" + countdown1
		+ ",alarmed=" + alarmed
		+ ",alarmed2=" + alarmed1
		+ "}";
	}

}
