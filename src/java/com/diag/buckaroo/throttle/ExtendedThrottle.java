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
 * $$Name$$
 *
 * $$Id$$
 */
package com.diag.buckaroo.throttle;

import com.diag.buckaroo.throttle.Throttle;

/**
 * This interface extends the Throttle interface by adding a
 * commit method that can specify a count of events to be emitted.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public interface ExtendedThrottle extends Throttle {
	
	/**
	 * Commit the current throttle state computed by the prior call to the begin
	 * method. This method must be called if the event was emitted regardless of whether
	 * or not it was admissible. Committing an event which is not admissible alarms the
	 * throttle. The alarm may clear if later event emissions conform to its traffic contract.
	 * @param count is a count of events that will be emitted by the caller.
	 * @return true if the throttle is not currently alarmed, false otherwise.
	 */
	public boolean commit(int count);

}
