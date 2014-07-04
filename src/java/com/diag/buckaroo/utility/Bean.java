/**
 * Copyright 2006-2013 Digital Aggregates Corporation, Colorado, USA.
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
package com.diag.buckaroo.utility;

import java.lang.reflect.Method;

/**
 * This class provides some handy little utilities for dealing with beans: Java
 * objects that are simply containers with gettors and settors to access and modify
 * them.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class Bean {

	/**
	 * Return a string representation of the specified bean object, using reflection to
	 * determine the value of the fields in the bean by accessing its gettors. Note that
	 * having a bean gettor does not imply that the object actually has a field by that
	 * name; for example int getValue() does not imply that the bean has a field int value.
	 * @param object is the bean object.
	 * @return a string representation of the bean object.
	 */
	public static String toString(Object object) {
		StringBuffer buffer = new StringBuffer(64);
		Class<?> type = object.getClass();
		buffer.append('{');
		Method[] methods = type.getMethods();
		int count = 0;
		for (Method method : methods) {
			if (method.getName().startsWith("get") && (method.getParameterTypes().length == 0)) {
				try {
					if ((count++) != 0) { buffer.append(' '); }
					String name = method.getName();
					String first = name.substring(3, 4).toLowerCase();
					String last = name.substring(4);
					buffer.append(first);
					buffer.append(last);
					buffer.append('=');
					Object value = method.invoke(object, (Object[])null);
					if (value instanceof String) {
						buffer.append('"');
						buffer.append(value.toString());
						buffer.append('"');						
					} else if ((value instanceof Number) || (value instanceof Boolean)) {
						buffer.append(value.toString());
					} else if (value instanceof Character) {
						buffer.append('\'');
						buffer.append(value.toString());
						buffer.append('\'');
					} else {
						buffer.append('{');
						buffer.append(value.toString());
						buffer.append('}');
					}
				} catch (Exception ignore) {
				}
			}
		}
		buffer.append('}');
		return buffer.toString();
	}
}
