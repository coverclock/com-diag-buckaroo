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

public class GeometricThrottle implements Throttle {
	
	int consecutive = 1;
	int consecutive2 = 1;
	int countdown = 1;
	int countdown2 = 1;
	boolean alarmed = false;
	boolean alarmed2 = false;
	
	public GeometricThrottle() {
	}

	public void reset() {
		consecutive = 1;
		consecutive2 = 1;
		countdown = 1;
		countdown2 = 1;
		alarmed = false;
		alarmed2 = false;
	}

	public void reset(long ticks) { reset(); }

	public long admissable() {
	    long delay = 0;

	    do {
	        countdown2 = countdown;
	        consecutive2 = consecutive;
	        // The Desperado C++ version of this code depends on unsigned
	        // arithmetic, which doesn't exist in Java. For once, the C++
	        // code is more elegant.
	        if (countdown2 > 0) {
	            --countdown2;
	            if (countdown2 == 0) {
	                consecutive2 <<= 1;
	                if (consecutive2 > 0) {
	                	countdown2 = consecutive2 - consecutive;
	                	alarmed2 = false;
	                	break;
	                }
	            }
	        }
	        alarmed2 = true;
	        delay = -1;
	    } while (false);

	    return delay;
	}

	public long admissable(long ticks) { return admissable(); }

	public boolean commit() {
	    alarmed = alarmed2;
	    return rollback();
	}

	public boolean rollback() {
	    countdown = countdown2;
	    consecutive = consecutive2;
		return !alarmed;
	}

	public boolean isAlarmed() { return alarmed; }

	public long frequency() { return -1; }

	public long time() { return -1; }
	
	public String toString() {
		return this.getClass().getName()
		+ "{consecutive=" + consecutive
		+ ",consecutive2=" + consecutive2
		+ ",countdown=" + countdown
		+ ",countdown2=" + countdown2
		+ ",alarmed=" + alarmed
		+ ",alarmed2=" + alarmed2
		+ "}";
	}

}
