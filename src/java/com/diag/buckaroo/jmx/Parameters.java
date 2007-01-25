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
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import com.diag.buckaroo.jmx.CallBack;

/**
 * This class encapsulates a Properties of Strings that can be used in an
 * application as simple parameters. The array is sized, labelled, and
 * indexed solely by a Properties object provided at time of construction.
 * The array is exposed as a dynamic managed bean with each array position
 * an attribute named by its corresponding property keyword. This class
 * can be used by an application to easily expose parameter Strings,
 * which may be read and written, for purposes of application configuration
 * and field troubleshooting.
 * 
 * This class is an example of "Uncle Chip's Instant Managed Beans".
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class Parameters extends LifeCycle implements DynamicMBean {

	private final static String REMOVE = "remove";
	private final static String SET = "set";
	private final static String GET = "get";

	private Properties properties;
	private MBeanInfo info;
	private volatile CallBack callback;

	/**
	 * Ctor.
	 * @param properties is a Properties object from which the parameter names
	 * and values may be inferred for those properties keys and values that are of type
	 * String.
	 */
	public Parameters(Properties properties) {
		this.properties = properties;
		init();
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
	public Parameters setCallBack(CallBack callback) {
		this.callback = callback;
		return this;
	}
	
	/**
	 * Return the properties object.
	 * @return the properties object.
	 */
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * Initialize the MBean info from the current properties object. This
	 * is called automatically whenever the properties object changes. The
	 * MBean viewer (for example, jconsole) will likely have to be restarted
	 * to see any changes.
	 */
	public synchronized void init() {
		MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[properties.size()];
		Set<Map.Entry<Object,Object>> entries = properties.entrySet();
		String stringName = String.class.getCanonicalName();
		int ii = 0;
		for (Map.Entry entry : entries) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			if ((key instanceof String) && (value instanceof String)) {
				String name = (String)key;
				attributes[ii++] = new MBeanAttributeInfo(name, stringName, name, true, true, false);
			}
		}
		MBeanOperationInfo[] operations = new MBeanOperationInfo[3];
		int index = 0;
		try {
			Class[] parameters = new Class[1];
			parameters[0] = String.class;
			Method method = Parameters.class.getDeclaredMethod(REMOVE, parameters);
			operations[index++] = new MBeanOperationInfo(REMOVE, method);
		} catch (Exception exception) {
			getLogger().log(Level.WARNING, exception.toString(), exception);
		}
		try {
			Class[] parameters = new Class[2];
			parameters[0] = String.class;
			parameters[1] = String.class;
			Method method = Parameters.class.getDeclaredMethod(SET, parameters);
			operations[index++] = new MBeanOperationInfo(SET, method);
		} catch (Exception exception) {
			getLogger().log(Level.WARNING, exception.toString(), exception);
		}
		try {
			Class[] parameters = new Class[1];
			parameters[0] = String.class;
			Method method = Parameters.class.getDeclaredMethod(GET, parameters);
			operations[index++] = new MBeanOperationInfo(GET, method);
		} catch (Exception exception) {
			getLogger().log(Level.WARNING, exception.toString(), exception);
		}
		String thisName = Parameters.class.getCanonicalName();
		info = new MBeanInfo(thisName, thisName, attributes, null, operations, null);		
	}
	
	// Parameters Implementation
		
	/**
	 * Set a parameter to a value.
	 * @param name is a String which identifies the parameter.
	 * @param value is the new parameter value.
	 * @return the prior value of the parameter if it existed and was a String, null otherwise.
	 */
	public String set(String name, String value) {
		Object object;
		synchronized (this) { object = properties.setProperty(name, value); }
		if (object == null) { init(); }
		return (object == null) ? null : (object instanceof String) ? (String)object : null;
	}
	
	/**
	 * Get the value of a parameter.
	 * @param name is a String which identifies the parameter.
	 * @return the value of the parameter if it is a String, null otherwise.
	 */
	public String get(String name) {
		Object object;
		synchronized (this) { object = properties.getProperty(name); }
		return (object == null) ? null : (object instanceof String) ? (String)object : null;
	}
	
	/**
	 * Remove a parameter.
	 * @param name is a String which identifies the parameter.
	 * @return the value of the parameter if it was a String, null otherwise.
	 */
	public String remove(String name) {
		Object object;
		synchronized (this) { object = properties.remove(name); }
		if (object != null) { init(); }
		return (object == null) ? null : (object instanceof String) ? (String)object : null;
	}
	
	// DynamicMBean Implementation

	public Object getAttribute(String name) throws AttributeNotFoundException {
		String value;
		synchronized (this) { value = properties.getProperty(name); }
		if (value == null) {
			throw new AttributeNotFoundException(name);
		}
		return value;
	}
	
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		String name = attribute.getName();
		Object object = attribute.getValue().toString();
		if (!(object instanceof String)) {
			throw new InvalidAttributeValueException(name);
		}
		String subsequent = (String)object;
		Object prior;
		synchronized (this) { prior = properties.setProperty(name, subsequent); }
		if (prior == null) { init(); }
		if ((callback != null) && ((prior == null) || (!prior.equals(subsequent)))) { callback.callback(name); }
	}
	
	public AttributeList getAttributes(String[] names) {
		AttributeList attributes = new AttributeList(names.length);
		synchronized (this) {
			for (String name : names) {
				String value = properties.getProperty(name);
				if (value != null) {
					Attribute attribute = new Attribute(name, value);
					attributes.add(attribute);
				}
			}
		}
		return attributes;
	}
	
	public AttributeList setAttributes(AttributeList attributes) {
		int size = attributes.size();
		AttributeList results = new AttributeList(size);
		boolean reinit = false;
		String names[] = new String[size];
		synchronized (this) {
			for (int ii = 0; ii < size; ++ii) {
				Attribute before = (Attribute)attributes.get(ii);
				String name = before.getName();
				String subsequent = before.getValue().toString();
				Object prior = properties.setProperty(name, subsequent);
				if (prior == null) { reinit = true; }
				if ((prior == null) || (!prior.equals(subsequent))) { names[ii] = name; }
				Attribute after = new Attribute(name, subsequent);
				results.add(after);
			}
			if (reinit) { init(); }
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
		Object prior = null;
		if (actionName.equals(REMOVE)) {
			if (params.length == 1) {
				if (params[0] instanceof String)
				{
					String name = (String)params[0];
					synchronized (this) { prior = properties.remove(name); }
					if ((callback != null) && (prior != null)) { callback.callback(name); }
				}
			}
		} else if (actionName.equals(SET)) {
			if (params.length == 2) {
				if (params[0] instanceof String)
				{
					String name = (String)params[0];
					String subsequent = params[1].toString();
					synchronized (this) { prior = properties.setProperty(name, subsequent); }
					if ((callback != null) && ((prior == null) || (!prior.equals(subsequent)))) { callback.callback(name); }
				}
			}
		} else if (actionName.equals(GET)) {
			if (params.length == 1) {
				if (params[0] instanceof String)
				{
					String name = (String)params[0];
					synchronized (this) { prior = properties.getProperty(name); }
				}
			}
		}
		return prior;
	}
	
	public MBeanInfo getMBeanInfo() {
		return info;
	}
}