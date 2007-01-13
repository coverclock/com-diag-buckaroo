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

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 * This class creates a JMX Connector with specific application-specified ports for both
 * the server port and the registry port. Exposing a JMX management capability remotely
 * from behind a firewall can be problematic because you have to be able to access not only
 * the JMX server port but also the RMI registry port. This approach is described in
 * "Mimicking Out-of-the-Box Management Using the JMX Remote API" in the
 * <I>Java SE Monitoring and Management Guide</I>. The Connector supports both the SSL
 * encryption and the authentication password file System properties. The server and
 * registry port can be set programmatically via settors after construction but
 * before starting the connector, or by specifying them in System properties. This
 * capability can also be turned off completely by setting either port to zero. Only
 * one Connector per MBean server is needed. Each Connector is smart enough to allow
 * itself to be started once and only once until it is stopped. So, for example, a
 * single static Connector can be used by many MBeans in the same JVM and each
 * MBean can start the same Connector as part of its start life cycle. Only the first
 * started MBean will start the Connector.
 *
 * Shamelessly, unapologetically, and gratefully cribbed mostly from
 * http://www.archivum.info/dev@tomcat.apache.org/2006-03/msg00843.html.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class Connector {
	
	public final static String PROPERTY_JMX_PORT = "com.diag.buckaroo.jmx.port";
	public final static String PROPERTY_JNDI_PORT = "com.diag.buckaroo.jndi.port";
	public final static String PROPERTY_JNDI_REBIND = "com.diag.buckaroo.jndi.rebind";
	
	final static Logger DEFAULT_LOGGER = Logger.getLogger(Connector.class.getName());
	final static int DEFAULT_JMX_PORT = 32110;
	final static int DEFAULT_JNDI_PORT = 32111;
	final static String DEFAULT_PATH = "buckaroo";
	
	final static String PROPERTY_SSL = "com.sun.management.jmxremote.ssl";
	final static String PROPERTY_AUTHENTICATE = "com.sun.management.jmxremote.authenticate";
	final static String PROPERTY_PASSWORD_FILE = "com.sun.management.jmxremote.password.file";
	
	final static String PARAMETER_PASSWORD_FILE = "jmx.remote.x.password.file";
	
	final static String ENVIRONMENT_JAVA_HOME = "JAVA_HOME";
	final static String ENVIRONMENT_JRE_HOME = "JRE_HOME";
	
	final static String PATH_JRE = "/jre";
	final static String PATH_PASSWORD_FILE = "/lib/management/jmxremote.password";
	
	Logger logger = DEFAULT_LOGGER;
	String urlPath = DEFAULT_PATH;
	int jmxPort = DEFAULT_JMX_PORT;
	int jndiPort = DEFAULT_JNDI_PORT;
	boolean jndiRebind = Boolean.getBoolean(PROPERTY_JNDI_REBIND);
	MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	String passwordFilePath;

	JMXConnectorServer connectorServer;
	String canonicalUrlString;
	
	private AtomicBoolean started = new AtomicBoolean(false);
			
	/**
	 * Ctor.
	 */
	public Connector() {
		
		String value = System.getProperty(PROPERTY_JMX_PORT);
		if (value != null) { jmxPort = Integer.parseInt(value); }
		
		value = System.getProperty(PROPERTY_JNDI_PORT);
		if (value != null) { jndiPort = Integer.parseInt(value); }
		
		value = System.getProperty(PROPERTY_PASSWORD_FILE);
		if (value != null) {
			passwordFilePath = value;
		} else {
			value = System.getenv(ENVIRONMENT_JAVA_HOME);
			if (value != null) {
				passwordFilePath = value + PATH_JRE + PATH_PASSWORD_FILE;
			} else {
				value = System.getenv(ENVIRONMENT_JRE_HOME);
				if (value != null) {
					passwordFilePath = value + PATH_PASSWORD_FILE;
				}
			}
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
	 * Sets the logger. The default logger is one created specifically for this class.
	 * @param logger is a Logger.
	 * @return this object.
	 */
	public Connector setLogger(Logger logger) {
		this.logger = logger;
		return this;
	}

	/**
	 * Sets the path portion of the resulting URL for this Connector. The default path is
	 * "buckaroo". Typical paths used in the literature include "jmxrmi" and "server".
	 * @param path is a path string.
	 * @return this object.
	 */
	public Connector setPath(String path) {
		this.urlPath = path;
		return this;
	}
	
	/**
	 * Sets the server port number. The default server port number is DEFAULT_JMX_PORT. Setting
	 * the server port number to a value equal to or less than zero prevents this Connector from
	 * starting. Calling this method overrides any value that may have been set by the System
	 * property
	 * <CODE>com.diag.buckaroo.jmx.server.port</CODE>.
	 * @param jmxPort is a port number.
	 * @return this object.
	 */
	public Connector setJMXPort(int jmxPort) {
		this.jmxPort = jmxPort;
		return this;
	}
	
	/**
	 * Sets the registry port number. The default registry port number is DEFAULT_JNDI_PORT. Setting
	 * the registry port number to a value equal to or less than zero prevents this Connector from
	 * starting. Calling this method overrides any value that may have been set by the System
	 * property
	 * <CODE>com.diag.buckaroo.jmx.registry.port</CODE>.
	 * @param jndiPort is a port number.
	 * @return this object.
	 */
	public Connector setJNDIPort(int jndiPort) {
		this.jndiPort = jndiPort;
		return this;
	}
	
	/**
	 * Sets the rebind flag for the registry port. If this flag is true, this Connector will bind
	 * to the registry port even if another Connector is already bound to it, effectively hijacking
	 * the port. If false and the port is already bound, this Connector will fail to start.
	 * @param jndiRebind is true if rebinding is enabled, false otherwise.
	 * @return this object.
	 */
	public Connector setJNDIRebind(boolean jndiRebind) {
		this.jndiRebind = jndiRebind;
		return this;
	}
	
	/**
	 * Sets the managed bean server serviced by this Connector. The default MBean server is
	 * the Platform MBean server. Some application containers create their own MBean server
	 * separate from the Platform server.
	 * @param server is an MBean server.
	 * @return this object.
	 */
	public Connector setMBeanServer(MBeanServer server) {
		this.mBeanServer = server;
		return this;
	}
	
	/**
	 * Return the Logger used by this Connector.
	 * @return a Logger.
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Return a String representation of this Connector. If this Connector started successfully,
	 * this value is the URL used to connect to the MBean server it is servicing. If not, this
	 * value is null.
	 * @return a URL string or null.
	 */
	public String toString() {
		return canonicalUrlString;
	}
	
	/**
	 * Start this Connector. Returns true if this Connector was successfully started, false if
	 * it was not started for any reason, including a failure in the implementation or if it
	 * was already started (perhaps by a different Thread or some other MBean). If the Connector
	 * was already successfully started, its toString() method will return non-null.
	 * @return true if started by this invocation, false otherwise.
	 */
	public boolean start() {
		boolean result = false;
		
		final boolean fine = logger.isLoggable(Level.FINE);
		
		if ((jndiPort > 0) && (jmxPort > 0) && started.compareAndSet(false, true)) {
			
			try {
				
				InetAddress address = InetAddress.getLocalHost();
				String hostname = address.getCanonicalHostName();
				
				String urlString = "service:jmx:rmi://" + hostname + ":" + jmxPort + "/jndi/rmi://" + hostname + ":" + jndiPort + "/" + urlPath;
				if (fine) { logger.log(Level.FINE, "JMXServiceURL=" + urlString); }
	
				LocateRegistry.createRegistry(jndiPort);
	
				Map<String, Object> map = new HashMap<String, Object>();
				
				if (jndiRebind) {
					if (fine) { logger.log(Level.FINE, PROPERTY_JNDI_REBIND + "=true"); }
					map.put(RMIConnectorServer.JNDI_REBIND_ATTRIBUTE, "true");
				}
				
				if (Boolean.getBoolean(PROPERTY_SSL)) {
					if (fine) { logger.log(Level.FINE, PROPERTY_SSL + "=true"); }
					SslRMIClientSocketFactory clientFactory = new SslRMIClientSocketFactory();
					SslRMIServerSocketFactory serverFactory = new SslRMIServerSocketFactory();
					map.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, clientFactory);
					map.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, serverFactory);
				}

				if (Boolean.getBoolean(PROPERTY_AUTHENTICATE)) {
					if (fine) { logger.log(Level.FINE, PROPERTY_AUTHENTICATE + "=true"); }
					if (passwordFilePath != null) {
						String canonicalPath = new File(passwordFilePath).getCanonicalPath();
						if (fine) { logger.log(Level.FINE, PROPERTY_PASSWORD_FILE + "=" + canonicalPath); }
						map.put(PARAMETER_PASSWORD_FILE, canonicalPath);
					}
				}
	
				JMXServiceURL url = new JMXServiceURL(urlString);
				connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, map, mBeanServer);
	
				try {
					connectorServer.start();
					canonicalUrlString = url.toString();
					result = true;
				} catch (Exception exception) {
					logger.log(Level.WARNING, exception.toString());
				}
				
			} catch (Exception exception) {
				logger.log(Level.WARNING, exception.toString());
			}
		}
		
		return result;
	}
	
	/**
	 * Stop this Connector. Returns true if this Connector was successfully stopped, false if
	 * it was not stopped for any reason, including a failure in the implementation or if it
	 * was already stopped (perhaps by a different Thread or some other MBean). If this
	 * Connector was already stopped, its toString() method will return null.
	 * @return true if stopped by this invocation, false otherwise.
	 */
	public boolean stop() {
		boolean result = false;
		
		if ((connectorServer != null) && started.compareAndSet(true, false)) {
			try {
				connectorServer.stop();
				connectorServer = null;
				canonicalUrlString = null;
				result = true;
			} catch (Exception exception) {
				logger.log(Level.WARNING, exception.toString(), exception);
			}
		}
		
		return result;
	}

}
