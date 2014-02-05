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
 * This class encapsulates an array of int integers that can be used
 * in an application as simple counters. The array is sized, labeled,
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

	protected final static String RESET = "reset";
	
	private int[] counters;
	private Enum<?>[] constants;
	private MBeanInfo info;
	private volatile CallBack callback;

	/**
	 * Ctor.
	 * @param type is an enumerated type used to size, label and and index the
	 * array of counters.
	 */
	public Counters(Class<? extends Enum<?>> type) {
		constants = type.getEnumConstants();
		counters = new int[constants.length];
		MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[constants.length];
		String intName = Integer.class.getCanonicalName();
		for (int ii = 0; ii < attributes.length; ++ii) {
			Enum<? extends Enum<?>> constant = constants[ii];
			String name = constant.toString();
			attributes[ii] = new MBeanAttributeInfo(name, intName, name, true, true, false);
		}
		MBeanOperationInfo[] operations = null;
		try {
			operations = new MBeanOperationInfo[1];
			Method method = this.getClass().getDeclaredMethod(RESET);
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
	public synchronized int add(Enum<? extends Enum<?>> enumeration, int value) {
		int index = enumeration.ordinal();
		return counters[index] = counters[index] + value;
	}
	
	/**
	 * Increment a counter.
	 * @param enumeration identifies the counter.
	 * @return the new value of the counter.
	 */
	public int inc(Enum<? extends Enum<?>> enumeration) {
		return add(enumeration, 1);
	}
	
	/**
	 * Decrement a counter.
	 * @param enumeration identifies the counter.
	 * @return the new value of the counter.
	 */
	public int dec(Enum<? extends Enum<?>> enumeration) {
		return add(enumeration, -1);
	}
	
	/**
	 * Set a counter to the minimum of its value and a new value.
	 * @param enumeration identifies the counter.
	 * @param value is the new vlaue.
	 * @return the new value of the counter.
	 */
	public synchronized int min(Enum<? extends Enum<?>> enumeration, int value) {
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
	public synchronized int max(Enum<? extends Enum<?>> enumeration, int value) {
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
	public synchronized int set(Enum<? extends Enum<?>> enumeration, int value) {
		return counters[enumeration.ordinal()] = value;
	}
	
	/**
	 * Get the value of a counter.
	 * @param enumeration identifies a counter.
	 * @return the value of the counter.
	 */
	public synchronized int get(Enum<? extends Enum<?>> enumeration) {
		return counters[enumeration.ordinal()];
	}
	
	/**
	 * Clear a counter by setting it to zero.
	 * @param enumeration identifies the counter.
	 * @return the new value of the counter.
	 */
	public int clear(Enum<? extends Enum<?>> enumeration) {
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
	
	private int find1(String name) throws AttributeNotFoundException {
		/*
		 * Ghod, who is more stupid, me or the Java 6 Enum.valueOf()?
		 * I don't see how you can use Enum.valueOf() with a type known
		 * only at run-time.
		 */
		for (Enum<?> constant : constants) {
			if (name.equals(constant.toString())) {
				return constant.ordinal();
			}
		}
		throw new AttributeNotFoundException(name);
	}
	
	private int find2(String name) {
		int index;
		try { index = find1(name); } catch (Exception exception) { index = -1; }
		return index;
	}

	public synchronized Object getAttribute(String name) throws AttributeNotFoundException {
		return new Integer(counters[find1(name)]);
	}
	
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		try {
			String name = attribute.getName();
			int prior;
			int subsequent;
			synchronized (this) {
				int index = find1(name);
				prior = counters[index];
				subsequent = ((Integer)attribute.getValue()).intValue();
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
					Attribute attribute = new Attribute(name, new Integer(counters[index]));
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
					Integer object = (Integer)before.getValue();
					int prior = counters[index];
					int subsequent = object.intValue();
					counters[index] = subsequent;
					if (prior != subsequent) { names[index] = name; }
					Attribute after = new Attribute(name, new Integer(subsequent));
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
			String[] names = new String[counters.length];
			synchronized (this) {
				for (Enum<?> constant : constants) {
					int index = constant.ordinal();
					int prior = counters[index];
					counters[index] = 0;
					if (prior != 0) {
						names[index] = constant.toString();
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
		}
		return null;
	}

	public MBeanInfo getMBeanInfo() {
		return info;
	}
}