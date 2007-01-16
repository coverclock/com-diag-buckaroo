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

/**
 * This class implements a Throttle that always admits every event. It
 * can be used as either a Throttle or a Manifold Throttle.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class PromiscuousThrottle implements ManifoldThrottle {
	
	/**
	 * Ctor.
	 */
	public PromiscuousThrottle() { }

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#reset()
	 */
	public void reset() { }

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#reset(long)
	 */
	public void reset(long ticks) { }

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#admissible()
	 */
	public long admissible() { return 0; }

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#admissible(long)
	 */
	public long admissible(long ticks) { return 0; }

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#commit()
	 */
	public boolean commit() { return true; }

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.ManifoldThrottle#commit(int)
	 */
	public boolean commit(int count) { return true; }

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#rollback()
	 */
	public boolean rollback() { return true; }

	/* (non-Javadoc)
	 * @see com.diag.buckaroo.throttle.Throttle#isAlarmed()
	 */
	public boolean isAlarmed() { return false; }
	
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
	public String toString() { return this.getClass().getName() + "{}"; }
	
}
