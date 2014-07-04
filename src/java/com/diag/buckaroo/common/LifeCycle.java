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
package com.diag.buckaroo.common;

/**
 * This class defines an interface for a generic life cycle object.
 * (This is an experiment to see if it makes sense to have a common
 * life cycle interface for all objects which have a life cycle.)
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public interface LifeCycle<Context> {

	/**
	 * Signal a life cycle initialization.
	 * @param context is a initialization context associated with this instance.
	 */
	public void init(Context context);
	
	/**
	 * Signal a life cycle start.
	 */
	public void start();
	
	/**
	 * Signal a life cycle stop.
	 */
	public void stop();
	
	/**
	 * Signal a life cycle shutdown, releasing all resources associated with this instance.
	 */
	public void shutDown();
	
	/**
	 * Returns the initialization context associated with this instance.
	 * @return the context passed during initialization.
	 */
	public Context getContext();
	
}
