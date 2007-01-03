/**
 * Copyright 2006 Digital Aggregates Corp., Arvada CO 80001-0597, USA.
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

import com.diag.buckaroo.jmx.LifeCycle;
import com.diag.buckaroo.utility.Discovery;
import com.diag.buckaroo.utility.Heap;

/**
 * This class exposes information about the application as a standard
 * managed bean (MBean). It also provides methods callable via a JMX
 * MBean browser to determine the class file path or the compilation
 * date of any defined class along the class path.
 * 
 * This class is an example of "Uncle Chip's Instant Managed Beans".
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 *
 * @date $Date$
 *
 */
public class Platform extends LifeCycle implements PlatformMBean {
	
	Class klass;
	
	/**
	 * Ctor. The class instrumented by this managed bean is the class
	 * of this object.
	 */
	public Platform() {
		this(Platform.class);
	}
	
	/**
	 * Ctor. The class instrumented by this managed bean is the class
	 * of the specified object.
	 * @param object is the object.
	 */
	public Platform(Object object) {
		this(object.getClass());
	}
	
	/**
	 * Ctor. The class instrumented by this managed bean is the one
	 * specified.
	 * @param klass is the class.
	 */
	public Platform(Class klass) {
		this.klass = klass;
	}
	
	/**
	 * Return the committed (guaranteed) size of the heap in bytes.
	 * @return the committed heap size in bytes.
	 */
	public long getCommittedHeapBytes() {
		return new Heap().getBytesCommitted();
	}
	
	/**
	 * Return the initial size of the heap in bytes.
	 * @return the initial heap size in bytes.
	 */
	public long getInitialHeapBytes() {
		return new Heap().getBytesInitial();
	}
	
	/**
	 * Return the maximum size of the heap in bytes.
	 * @return the maximum heap size in bytes.
	 */
	public long getMaximumHeapBytes() {
		return new Heap().getBytesMaximum();
	}
	
	/**
	 * Return the used size of the heap in bytes.
	 * @return the used heap size in bytes.
	 */
	public long getUsedHeapBytes() {
		return new Heap().getBytesUsed();
	}
	
	/**
	 * Return the path name of the class file or the jar file containing the
	 * class file of the instrumented class.
	 * @return a path name.
	 */
	public String getClassFilePath() {
		return getClassFilePath(klass.getName());
	}
	
	/**
	 * Return the compilation date of the class file or the jar file containing
	 * the class file of the instrumented class.
	 * @return a date stamp.
	 */
	public String getClassCompilationDate() {
		return getClassCompilationDate(klass.getName());
	}
	
	/**
	 * Return the path name of the class file or the jar file containing the
	 * class file of the specified class.
	 * @param name is the name of the class.
	 * @return a path name.
	 */
	public String getClassFilePath(String name) {
		try {
			return Discovery.getClassFilePath(Class.forName(name));
		} catch (Exception exception) {
			return exception.toString();
		}
	}
	
	/**
	 * Return the compilation date of the class file or the jar file containing
	 * the class file of the specified class.
	 * @param name is the name of the class.
	 * @return a date stamp.
	 */
	public String getClassCompilationDate(String name) {
		try {
			return Discovery.getClassCompilationDate(Class.forName(name)).toString();
		} catch (Exception exception) {
			return exception.toString();
		}
	}
	
	/**
	 * Return the path name of the specified resource bundle.
	 * @param name is the name of the resource bundle.
	 * @return a path name.
	 */
	public String getResourceBundlePath(String name) {
		try {
			return Discovery.getResourceBundlePath(name);
		} catch (Exception exception) {
			return exception.toString();
		}
	}
}
