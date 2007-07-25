/**
 * Copyright 2006-2007 Digital Aggregates Corp., Arvada CO 80001-0597, USA.
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
package com.diag.buckaroo.jmx;

/**
 * This interface describes the managed bean exposed by an object
 * of type Platform.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public interface PlatformMBean {

	public long getCommittedHeapBytes();
	
	public long getInitialHeapBytes();
	
	public long getMaximumHeapBytes();
	
	public long getUsedHeapBytes();
	
	public String getClassFilePath();
	
	public String getClassCompilationDate();

	public String getClassFilePath(String name);
	
	public String getClassCompilationDate(String name);
	
	public String getResourceBundlePath(String name);
}
