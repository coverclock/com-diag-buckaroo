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
 * Defines the standard interface to a rate control mechanism. Before emitting an event
 * (whatever "emit" and "event" means in the context of the application), the application
 * calls the throttle to ask if the event is "admissible". If it is, the application emits
 * the event and commits the throttle state. If it is not, the application rolls back the
 * throttle state and does not emit the event. Throttles may be event-based or time-based.
 * Event-based throttles (like the Geometric Throttle) base admission decisions on something
 * other than time, such as the number of events already admitted. Time-based throttles
 * (such as the Generic Cell Rate Algorithm or GCRA) base admission decisions on chronological
 * factors such as the interarrival time between events. Throttles may be compounded together
 * to form a composite throttle (such as the Cell Rate Throttle, which is implemented using
 * two Generic Cell Rate Algorithms). If the application commits the Throttle for an event
 * which was not admissible, the Throttle becomes "alarmed". There are legitimate reasons
 * for doing this, such as choosing to violate the Throttle's traffic contract rather than
 * allow an internal buffer to overflow. Throttles may be "reset" at any time to return them
 * to their initial state. The first event for any Throttle is guaranteed to be admissible.
 * Event streams which conform to whatever traffic contract the Throttle implements are
 * guaranteed to not alarm the Throttle. Time-based throttles make implement different
 * time granularities, for example, milliseconds, microseconds, or even nanoseconds,
 * depending on their underlying implementation. This granularity is known as the Throttle's
 * frequency, and any Throttle may be queried as to its frequency. Every time-based Throttle
 * implements a method that returns the current time in units appropriate to its frequency.
 * The current time may be equivalent to wall clock time relative to some epoch, or merely a
 * monotonically increasing counter suitable for measuring arbitrary time intervals.
 * 
 * Both time of day values and time duration values are kept in sixty-four bit longwords
 * and are measured in "ticks" relative to some epoch, that is, in time units specific to
 * the derived class and measured relative to some fixed point in time also specific to the
 * derived class. Hence throttles of different types are not required to be interoperable.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
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
	 * is admissible. If the event is immediately admissible, zero is returned. The
	 * event is guaranteed to be admissible if the caller delays the returned number
	 * of ticks before calling this method again. The very first event submitted to
	 * this throttle is guaranteed to be admissible. This method computes a new throttle
	 * state which must later be either committed if the event is emitted, or rolled back
	 * if the event is not emitted. This is done to accomodate different throttle
	 * implementations which may require this behavior.
	 * @return the number of ticks from the current time until the next event is admissible
	 * or the maximum possible value if this cannot be determined by this type of throttle.
	 */
	public long admissible();

	/**
	 * Compute the number of ticks from the specified time of day until the next event
	 * is admissible. If the event is immediately admissible, zero is returned. The
	 * event is guaranteed to be admissible if the caller delays the returned number
	 * of ticks before calling this method again.  The very first event submitted to
	 * this throttle is guaranteed to be admissible. This method computes a new throttle
	 * state which must later be either committed if the event is emitted, or rolled back
	 * if the event is not emitted. This is done to accomodate different throttle
	 * implementations which may require this behavior.
	 * @param ticks is the time of day in the number of ticks since the epoch.
	 * @return the number of ticks from the current time until the next event is admissible
	 * or the maximum possible value if this cannot be determined by this type of throttle.
	 */
	public long admissible(long ticks);

	/**
	 * Commit the current throttle state computed by the prior call to the begin
	 * method. This method must be called if the event was emitted regardless of whether
	 * or not it was admissible. Committing an event which is not admissible alarms the
	 * throttle. The alarm may clear if later event emissions conform to its traffic contract.
	 * @return true if the throttle is not currently alarmed, false otherwise.
	 */
	public boolean commit();

	/**
	 * Rollback the current throttle state computed by the prior call to the begin
	 * method. Call this method if the event was not emitted regardless of whether or
	 * not it was admissible. The throttle state is guaranteed not to be modified.
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
	 * this throttle is not time-based.
	 */
	public long frequency();
	
	/**
	 * Return the elapsed ticks since an epoch. Since the epoch may lie in the
	 * future, this value may legitimately be negative. It may also be negative due to
	 * numeric rollover. The value returned is only useful for computing elapsed time
	 * between calls and may have no relationship to actual wall clock time. In the event
	 * that the value rolls over and becomes negative, computations for values of elapsed
	 * ticks less than 2^63 will still be correct. For a tick equal to a nanosecond,
	 * equivalent to a Throttle frequency of one gigahertz, this yields a maximum elapsed
	 * time of about 292 years. The value returned by a Throttle which is not time-based
	 * (the Throttle reports a frequency that is is a negative number) is not defined.
	 * @return elapsed ticks since an epoch.
	 */
	public long time();

	/**
	 * Convert the throttle state into a printable string.
	 * @return a printable string representing the throttle state.
	 */
	public String toString();

}
