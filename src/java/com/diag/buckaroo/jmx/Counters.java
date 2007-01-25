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

import java.lang.reflect.Method;
import java.util.logging.Level;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import com.diag.buckaroo.jmx.CallBack;

/**
 * This class encapsulates an array of long integers that can be used
 * in an application as simple counters. The array is sized, labelled,
 * and indexed solely by an enumeration provided at time of construction.
 * The array is exposed as a dynamic managed bean with each array position
 * an attribute named by its corresponding enumeration value. This class
 * can be used by an application to easily expose internal counters,
 * which may be read and written, for purposes of development debugging
 * and field troubleshooting.
 * 
 * This class is an example of "Uncle Chip's Instant Managed Beans".
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class Counters extends LifeCycle implements DynamicMBean {

	private final static String RESET = "reset";
	
	private long[] counters;
	private Class<? extends Enum> type;
	private MBeanInfo info;
	private volatile CallBack callback;

	/**
	 * Ctor.
	 * @param type is an enumerated type used to size, label and and index the
	 * array of counters.
	 */
	public Counters(Class<? extends Enum> type) {
		this.type = type;
		Object[] constants = type.getEnumConstants();
		counters = new long[constants.length];
		MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[constants.length];
		String longName = Long.class.getCanonicalName();
		for (int ii = 0; ii < attributes.length; ++ii) {
			Enum enumeration = (Enum)constants[ii];
			String name = enumeration.toString();
			attributes[ii] = new MBeanAttributeInfo(name, longName, name, true, true, false);
		}
		MBeanOperationInfo[] operations = null;
		try {
			operations = new MBeanOperationInfo[1];
			Class[] parameters = new Class[0];
			Method method = this.getClass().getDeclaredMethod(RESET, parameters);
			operations[0] = new MBeanOperationInfo(RESET, method);
		} catch (Exception exception) {
			getLogger().log(Level.WARNING, exception.toString(), exception);
		}
		String thisName = this.getClass().getCanonicalName();
		info = new MBeanInfo(thisName, thisName, attributes, null, operations, null);
	}
	
	/**
	 * Get the current callback.
	 * @return the callback.
	 */
	public CallBack getCallBack() {
		return callback;
	}
	
	/**
	 * Set a callback whose callback method is called when an attribute is changed
	 * through the MBean server.
	 * @param callback is the callback object.
	 * @return this.
	 */
	public Counters setCallBack(CallBack callback) {
		this.callback = callback;
		return this;
	}
	
	// Counters Implementation
	
	/**
	 * Add a value to a counter.
	 * @param enumeration identifies the counter.
	 * @param value is the value to be added (may be negative).
	 * @return the new value of the counter.
	 */
	public synchronized long add(Enum enumeration, long value) {
		int index = enumeration.ordinal();
		return counters[index] = counters[index] + value;
	}
	
	/**
	 * Increment a counter.
	 * @param enumeration identifies the counter.
	 * @return the new value of the counter.
	 */
	public long inc(Enum enumeration) {
		return add(enumeration, 1);
	}
	
	/**
	 * Decrement a counter.
	 * @param enumeration identifies the counter.
	 * @return the new value of the counter.
	 */
	public long dec(Enum enumeration) {
		return add(enumeration, -1);
	}
	
	/**
	 * Set a counter to the minimum of its value and a new value.
	 * @param enumeration identifies the counter.
	 * @param value is the new vlaue.
	 * @return the new value of the counter.
	 */
	public synchronized long min(Enum enumeration, long value) {
		int index = enumeration.ordinal();
		if (value < counters[index]) { counters[index] = value; }
		return counters[index];
	}
	
	/**
	 * Set a counter to the maximum of its value and a new value.
	 * @param enumeration identifies the counter.
	 * @param value is the new value.
	 * @return the new value of the counter.
	 */
	public synchronized long max(Enum enumeration, long value) {
		int index = enumeration.ordinal();
		if (value > counters[index]) { counters[index] = value; }
		return counters[index];
	}
	
	/**
	 * Set a counter to a value.
	 * @param enumeration identifies the counter.
	 * @param value is the new value.
	 * @return the new value of the counter.
	 */
	public synchronized long set(Enum enumeration, long value) {
		return counters[enumeration.ordinal()] = value;
	}
	
	/**
	 * Get the value of a counter.
	 * @param enumeration identifies a counter.
	 * @return the value of the counter.
	 */
	public synchronized long get(Enum enumeration) {
		return counters[enumeration.ordinal()];
	}
	
	/**
	 * Clear a counter by setting it to zero.
	 * @param enumeration identifies the counter.
	 * @return the new value of the counter.
	 */
	public long clear(Enum enumeration) {
		return set(enumeration, 0);
	}
	
	/**
	 * Reset all counters by clearing all of them. This method is exposed
	 * as an operation of the managed bean.
	 */
	public synchronized void reset() {
		for (int ii = 0; ii < counters.length; ++ii) { counters[ii] = 0; }
	}
	
	// DynamicMBean Implementation

	@SuppressWarnings("unchecked") // It kills me but I've spent way too much time on this.
	private int find1(String name) throws AttributeNotFoundException {
		int index;
		try {
			// Enum<E extends Enum<E>>
			// public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name)
			// TODO Figure out how the heck to get rid of the unchecked warning.
			Enum enumeration = Enum.valueOf(type, name);
			index = enumeration.ordinal();
		} catch (Exception exception) {
			throw new AttributeNotFoundException(name);
		}
		return index;
	}
	
	private int find2(String name) {
		int index;
		try { index = find1(name); } catch (Exception exception) { index = -1; }
		return index;
	}

	public synchronized Object getAttribute(String name) throws AttributeNotFoundException {
		return new Long(counters[find1(name)]);
	}
	
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		try {
			String name = attribute.getName();
			long prior;
			long subsequent;
			synchronized (this) {
				int index = find1(name);
				prior = counters[index];
				subsequent = ((Long)attribute.getValue()).longValue();
				counters[index] = subsequent;
			}
			if ((callback != null) && (prior != subsequent)) { callback.callback(name); }
		} catch (AttributeNotFoundException again) {
			throw again;
		} catch (Exception exception) {
			throw new InvalidAttributeValueException(exception.toString());
		}
	}
	
	public AttributeList getAttributes(String[] names) {
		AttributeList attributes = new AttributeList(names.length);
		synchronized (this) {
			for (String name : names) {
				int index = find2(name);
				if (index >= 0) {
					Attribute attribute = new Attribute(name, new Long(counters[index]));
					attributes.add(attribute);
				}
			}
		}
		return attributes;
	}
	
	public AttributeList setAttributes(AttributeList attributes) {
		int size = attributes.size();
		AttributeList results = new AttributeList(size);
		String[] names = new String[counters.length];
		synchronized (this) {
			for (int ii = 0; ii < size; ++ii) {
				Attribute before = (Attribute)attributes.get(ii);
				String name = before.getName();
				int index = find2(name);
				if (index >= 0) {
					Long object = (Long)before.getValue();
					long prior = counters[index];
					long subsequent = object.longValue();
					counters[index] = subsequent;
					if (prior != subsequent) { names[index] = name; }
					Attribute after = new Attribute(name, new Long(subsequent));
					results.add(after);
				}
			}
		}
		if (callback != null) {
			for (String name : names) {
				if (name != null) {
					callback.callback(name);
				}
			}
		}
		return results;
	}
	
	public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		if (actionName.equals(RESET)) {
			Object[] constants = type.getEnumConstants();
			String[] names = new String[counters.length];
			synchronized (this) {
				for (Object constant : constants) {
					Enum enumeration = (Enum)constant;
					int index = enumeration.ordinal();
					long prior = counters[index];
					counters[index] = 0;
					if (prior != 0) { names[index] = enumeration.toString(); }
				}
			}
			if (callback != null) {
				for (String name : names) {
					if (name != null) {
						callback.callback(name);
					}
				}
			}
		}
		return null;
	}

	public MBeanInfo getMBeanInfo() {
		return info;
	}
}