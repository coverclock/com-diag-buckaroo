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

import java.lang.management.ManagementFactory;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.management.MBeanServer;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ObjectInstance;

/**
 * This class provides basic support for registering and unregistering
 * a managed bean with an MBean server. A usable default object name and
 * MBean server (the platform MBean server) are provided during construction.
 * Settors and gettors are provided to change this prior to starting the MBean.
 * 
 * This class is part of "Uncle Chip's Instant Managed Beans".
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class LifeCycle {

	protected static final Logger DEFAULT_LOGGER = Logger.getLogger(LifeCycle.class.getName());
	
	private Logger log = DEFAULT_LOGGER;
	private MBeanServer server = ManagementFactory.getPlatformMBeanServer();
	
	private ObjectName name;
	
	/**
	 * Ctor. A default logger (the Java logger), MBean name (based on the name of this class),
	 * and MBean server (the platform MBean server) are inferred.
	 */
	public LifeCycle() {
		try {
			setMBeanName(this);
		} catch (Exception exception) {
			getLogger().log(Level.WARNING, exception.toString(), exception);
		}
	}
	
	/**
	 * If this connector is garbage collected, stop it beforehand. It is
	 * not an error to stop an already stopped connector.
	 */
	protected void finalize() throws Throwable {
		try {
			stop();
		} finally {
			super.finalize();
		}
	}
	
	/**
	 * Set the object name of the MBean.
	 * @param name is a object name.
	 * @return this object.
	 */
	public LifeCycle setMBeanName(ObjectName name) {
		this.name = name;
		return this;
	}
	
	/**
	 * Set the object name of the MBean using information about a class.
	 * @param klass is a class.
	 * @return this object.
	 * @throws MalformedObjectNameException 
	 */
	public LifeCycle setMBeanName(Class klass) throws MalformedObjectNameException {
		String domain = klass.getPackage().getName();
		String type = klass.getSimpleName();
		String name = "0x" + Integer.toHexString(this.hashCode());
		ObjectName objectname = new ObjectName(domain + ":type=" + type + ",name=" + name);
		setMBeanName(objectname);
		return this;
	}
	
	/**
	 * Set the object name of the MBean using information about an object.
	 * @param object is an object.
	 * @return this object.
	 */
	public LifeCycle setMBeanName(Object object) throws MalformedObjectNameException {
		Class klass = object.getClass();
		String domain = klass.getPackage().getName();
		String type = klass.getSimpleName();
		String name = "0x" + Integer.toHexString(object.hashCode());
		ObjectName objectname = new ObjectName(domain + ":type=" + type + ",name=" + name);
		setMBeanName(objectname);
		return this;
	}
	
	/**
	 * Set the object name of the MBean using a String as its name.
	 * @param string is the name portion of an object name.
	 * @return this object.
	 */
	public LifeCycle setMBeanName(String string) throws MalformedObjectNameException {
		Class klass = this.getClass();
		String domain = klass.getPackage().getName();
		String type = klass.getSimpleName();
		String name = string;
		ObjectName objectname = new ObjectName(domain + ":type=" + type + ",name=" + name);
		setMBeanName(objectname);
		return this;
	}
	
	/**
	 * Get the object name of the MBean.
	 * @return an object name.
	 */
	public ObjectName getMBeanName() {
		return name;
	}
	
	/**
	 * Set the Java logger used by the MBean.
	 * @param log is a java logger.
	 * @return this object.
	 */
	public LifeCycle setLogger(Logger log) {
		this.log = log;
		return this;
	}
	
	/**
	 * Get the Java logger used by the MBean.
	 * @return a java logger.
	 */
	public Logger getLogger() {
		return log;
	}
	
	/**
	 * Set the MBean Server used by the MBean.
	 * @param server is an MBean server.
	 * @return this object.
	 */
	public LifeCycle setMBeanServer(MBeanServer server) {
		this.server = server;
		return this;
	}
	
	/**
	 * Get the MBean server used by the MBean.
	 * @return an MBean server.
	 */
	public MBeanServer getMBeanServer() {
		return server;
	}
	
	/**
	 * Start the MBean by registering it with its MBean server.
	 * @return true if successful, false otherwise.
	 */
	public boolean start() {
		boolean result = false;
		ObjectName name = getMBeanName();
		if (name != null) {
			if (server != null) {
				stop();
				if (!server.isRegistered(name)) {
					try {
						ObjectInstance instance = server.registerMBean(this, name);
						String classname = instance.getClassName();
						String objectname = instance.getObjectName().toString();
						getLogger().fine("start: registerMBean " + name + " " + classname + " " + objectname);
						result = true;
					} catch (InstanceAlreadyExistsException ignore) {
					} catch (Exception exception) {
						getLogger().log(Level.WARNING, exception.toString(), exception);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Stop the MBean by unregistering it with its MBean server.
	 * @return true if successful, false otherwise.
	 */
	public boolean stop() {
		boolean result = false;
		ObjectName name = getMBeanName();
		if (name != null) {
			if (server != null) {
				if (server.isRegistered(name)) {
					try {
						server.unregisterMBean(name);
						result = true;
					} catch (InstanceNotFoundException ignore) {
					} catch (Exception exception) {
						getLogger().log(Level.WARNING, exception.toString(), exception);
					}
				}
			}
		}
		return result;
	}
}
