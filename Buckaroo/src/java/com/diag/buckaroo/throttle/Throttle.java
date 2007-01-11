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
 * $$Name$$
 *
 * $$Id$$
 */
package com.diag.buckaroo.throttle;

/**
 * Defines the standard interface to a rate control mechanism.
 * 
 * Both time of day values and time duration values are kept in sixty-four bit longwords
 * and are measured in "ticks" relative to some epoch, that is, in time units specific to
 * the derived class and measured relative to some fixed point in time also specific to the
 * derived class. Hence throttles of different types are not required to be interoperable.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 *
 * @date $Date$
 */
public interface Throttle {

	/**
	 * Reset this throttle to its just constructed state using
	 * the current time of day. Resetting a throttle may cause
	 * subsequent events to be emitted outside of the traffic
	 * contract of the throttle since all prior state is lost.
	 */
	public void reset();

	/**
	 * Reset this throttle to its just constructed state using
	 * the specified time of day in ticks since the epoch. Resetting
	 * a throttle may cause subsequent events to be emitted outside
	 * of the traffic contract of the throttle since all prior state
	 * is lost.
	 * @param ticks is the time of day in the number of ticks since the epoch.
	 */
	public void reset(long ticks);

	/**
	 * Compute the number of ticks from the current time of day until the next event
	 * is admissable. If the event is immediately admissable, zero is returned. The
	 * event is guaranteed to be admissable if the caller delays the returned number
	 * of ticks before calling this method again. This method computes a new throttle
	 * state which must later be either committed if the event is emitted, or rolled back
	 * if the event is not emitted.
	 * @return the number of ticks from the current time until the next event is admissable
	 * or negative if this cannot be determined by this type of throttle.
	 */
	public long admissable();

	/**
	 * Compute the number of ticks from the specified time of day until the next event
	 * is admissable. If the event is immediately admissable, zero is returned. The
	 * event is guaranteed to be admissable if the caller delays the returned number
	 * of ticks before calling this method again. This method computes a new throttle
	 * state which must later be either committed if the event is emitted, or rolled back
	 * if the event is not emitted.
	 * @param ticks is the time of day in the number of ticks since the epoch.
	 * @return the number of ticks from the current time until the next event is admissable
	 * or a negative number if this cannot be determined by this type of throttle (such
	 * throttles typically return either zero meaning admissable now, or a negative number
	 * meaning not yet).
	 */
	public long admissable(long ticks);

	/**
	 * Commit the current throttle state computed by the prior call to the begin
	 * method. This method must be called if the event was emitted regardless of whether
	 * or not it was admissable. Committing an event which is not admissable alarms the
	 * throttle. The alarm may clear if later event emissions conform to its traffic contract.
	 * @return true if the throttle is not currently alarmed, false otherwise.
	 */
	public boolean commit();

	/**
	 * Rollback the current throttle state computed by the prior call to the begin
	 * method. Call this method if the event was not emitted regardless of whether or
	 * not it was admissable. The throttle state is guaranteed not to be modified.
	 * @return true if the throttle is not currently alarmed, false otherwise.
	 */
	public boolean rollback();
	
	/**
	 * Returns true if the throtthe is currently alarmed, false otherwise.
	 * @return true if the throttle is currently alarmed, false otherwise.
	 */
	public boolean isAlarmed();
	
	/**
	 * Returns true if the throttle is in a valid state, false otherwise.
	 * This is used to audit the throttle state during unit testing. Unless
	 * there is a bug in the implementation, this method will always return
	 * true.
	 * @return true if the throttle is in a valid state, false otherwise.
	 */
	public boolean isValid();
	
	/**
	 * Return the number of ticks that are in one second. For example,
	 * if this throttle measures time to one millisecond granularity, return
	 * one thousand; to one microsecond, one million. Frequencies that are
	 * not a power of ten are possible (for example, based on the frequency of
	 * the CPU clock). Frequencies that are not integers are not possible.
	 * @return the number of ticks that are in one second or a negative number if
	 * this throttle is not time based.
	 */
	public long frequency();
	
	/**
	 * Return the time of day in ticks since the epoch.
	 * @return the time of day in ticks since the epoch or a negative number if this
	 * throttle is not time based.
	 */
	public long time();

	/**
	 * Convert the throttle state into a printable string.
	 * @return a printable string representing the throttle state.
	 */
	public String toString();

}
