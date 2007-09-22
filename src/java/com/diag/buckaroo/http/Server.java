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
package com.diag.buckaroo.http;

import java.io.*;
import java.net.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ResourceBundle;
import java.util.Enumeration;

/**
 * This class implements a simple single threader (and unsecure) HTTP server
 * that can be embedded inside an application. It is based on prior work by Jon
 * Berg <jon.berg at turtlemeat.com>. See http://www.turtlemeat.com for more
 * information.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class Server {

	private int port = 80;
	private String root = null;
	private boolean enabled = false;
	private Listener listener = null;
	private ResourceBundle bundle = null;
	
	// I use this instead of a Enum to make it easier to port to 1.4 for CVM.
	static int	UNSUPPORTED = 0,
				GET = 1,
				HEAD = 2;

	/**
	 * Defines the listener thread that waits for incoming HTTP requests.
	 */
	public class Listener extends Thread {	
		
		/**
		 * Implements listener thread body.
		 */
		public void run() {
			log("Starting");
			
			ServerSocket listensocket = null;
			
			try {
				log("Binding " + port);
				listensocket = new ServerSocket(port);
			} catch (Exception exception) {
				log(exception);
				return;
			}

			while (enabled) {
				log("Listening");
				try {
					Socket connectionsocket = listensocket.accept();
					InetAddress client = connectionsocket.getInetAddress();
					log("Serving " + client.getHostName());
					BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
					DataOutputStream output = new DataOutputStream(connectionsocket.getOutputStream());
					http(input, output);
					connectionsocket.shutdownInput();
					connectionsocket.shutdownOutput();
					input.close();
					output.close();
					connectionsocket.close();
				} catch (Exception exception) {
					log(exception);
				}
			}
			
			try {
				listensocket.close();
			} catch (Exception exception) {
				log(exception);
			}

			log("Ending");
		}
		
	}
	
	/**
	 * Constructor.
	 */
	public Server() {}
	
	protected static final Logger DEFAULT_LOGGER = Logger.getLogger(Server.class.getName());
	private Logger log = DEFAULT_LOGGER;
	
	/**
	 * Set the Java logger used by this Server.
	 * @param log is a Java logger.
	 * @return this object.
	 */
	public Server setLogger(Logger log) {
		this.log = log;
		return this;
	}
	
	/**
	 * Get the Java logger used by this Server.
	 * @return a Java logger.
	 */
	public Logger getLogger() {
		return log;
	}
	
	/**
	 * Log a string as info.
	 * @param string is the string to log.
	 */
	protected void log(String string) {
		// System.out.println(string);
		getLogger().fine(string);
	}
	
	/**
	 * Log an exception as a warning.
	 * @param exception is the exception to log.
	 */
	protected void log(Exception exception) {
		// System.err.println(exception.toString());
		getLogger().log(Level.WARNING, exception.toString(), exception);
	}
	
	/**
	 * Returns the HTTP listening port used by this Server.
	 * @return the HTTP listening port.
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Sets the HTTP listening port used by this Server prior to being started.
	 * @param port is an HTTP listening port.
	 * @return this object.
	 */
	public Server setPort(int port) {
		this.port = port;
		return this;
	}
	
	/**
	 * Returns the path root used by this Server.
	 * @return the path root.
	 */
	public String getRoot() {
		return root;
	}
	
	/**
	 * Sets the path prefix used by this Server.
	 * @param port is a path prefix.
	 * @return this object.
	 */
	public Server setRoot(String root) {
		this.root = root;
		return this;
	}
	
	/**
	 * Starts this HTTP server.
	 * @return this object.
	 */
	public synchronized Server start() {
		if (listener == null)
		{
			bundle = ResourceBundle.getBundle(this.getClass().getPackage().getName() + ".mime");
			enabled = true;
			listener = new Listener();
			listener.start();
		}
		return this;
	}
	
	/**
	 * Stops this HTTP server.
	 * @return this object.
	 */
	public synchronized Server stop() {
		if (listener != null) {
			enabled = false;
			listener.interrupt();
			try {
				listener.join(5000);
			} catch (Exception exception) {
				log(exception);
				return null;
			}
			listener = null;
		}
		return this;
	}

	/**
	 * Services a single HTTP request.
	 * @param input is the HTTP input stream.
	 * @param output is the HTTP output stream.
	 */
	protected void http(BufferedReader input, DataOutputStream output) {
		int method = UNSUPPORTED;
		String path = null;
		
		try {
		
			//This is the two types of request we can handle
			//GET /index.html HTTP/1.0
			//HEAD /index.html HTTP/1.0
			String tmp = input.readLine();
			log("Request " + tmp);
			String tmp2 = new String(tmp);
			tmp.toUpperCase();
			if (tmp.startsWith("GET")) {
				method = GET;
			} else if (tmp.startsWith("HEAD")) {
				method = HEAD;
			} else {
				method = UNSUPPORTED;
			}

			if (method == UNSUPPORTED) {
				output.writeBytes(header(501, null));
				return;
			}

			// Given "GET /index.html HTTP/1.0 ......."
			// find first space
			// find next space
			// copy what is between minus slash, then you get "index.html"
			int start = 0;
			int end = 0;
			for (int a = 0; a < tmp2.length(); a++) {
				if (tmp2.charAt(a) == ' ' && start != 0) {
					end = a;
					break;
				}
				if (tmp2.charAt(a) == ' ' && start == 0) {
					start = a;
				}
			}
			path = tmp2.substring(start + 2, end);
			
			log("Path " + path);
			
			if (root != null) {
				path = root + path;
				log("Effective " + path);
			}
			
			FileInputStream requested = null;
			try {
				requested = new FileInputStream(path);
			} catch (Exception exception) {
				log(exception);
				output.writeBytes(header(404, null));
				return;
			}
	
			String contenttype = null;
			String mimetype = null;
			for (Enumeration<String> e = bundle.getKeys(); e.hasMoreElements(); ) {
				mimetype = e.nextElement();
				if (path.endsWith(mimetype)) {
					contenttype = bundle.getString(mimetype);
					break;
				}
			}
			
			if (contenttype == null) {
				contenttype = bundle.getString(".html");
			}
			
			log("Type " + contenttype);
			
			output.writeBytes(header(200, contenttype));

			if (method == GET) {
				int octets = 0;
				try {
					int b;
					while (true) {
						b = requested.read();
						if (b == -1) { break; }
						output.write(b);
						octets++;
					}
					log("Complete " + octets);
				} catch (Exception ignored) {
					// Happens when the peer closes its end of the socket before
					// we have sent the entire file; typically happens with JPEG
					// and the like where the peer doesn't need the entire file.
					log("Partial " + octets);
				}
			}

			requested.close();

		} catch (Exception exception) {
			log(exception);
			try {
				output.writeBytes(header(500, null));
			} catch (Exception ignored) {}
		}
	}

	/**
	 * Generates an appropriate HTTP header for the output data stream.
	 * @param code is the HTTP return code.
	 * @param type is the type of data being returned.
	 * @return
	 */
	protected String header(int code, String type) {
		String s = "HTTP/1.0 ";
		
		switch (code) {
		case 200:
			s = s + "200 OK";
			break;
		case 400:
			s = s + "400 Bad Request";
			break;
		case 403:
			s = s + "403 Forbidden";
			break;
		case 404:
			s = s + "404 Not Found";
			break;
		case 500:
			s = s + "500 Internal Server Error";
			break;
		case 501:
			s = s + "501 Not Implemented";
			break;
		}
		s = s + "\r\n";
		s = s + "Connection: close\r\n";
		s = s + "Server: " + Server.class.getName() + "\r\n";
		if (type !=  null) {
			s = s + "Content-Type: " + type + "\r\n";
		}

		return s;
	}
	
	/**
	 * This is a main so that a server can be invoked from the command line.
	 * The first optional positional argument is the path prefix to use,
	 * the defaulting being "".
	 * The second optional positional argument is the number of milliseconds to
	 * run, the default being 60000.
	 * The third optional positional argument is the port to use, the default
	 * being 80.
	 * @param args is the argument array.
	 */
	public static void main(String args[]) {
		String prefix = (args.length > 0) ? args[0] : "";
		long delay = (args.length > 1) ? Long.parseLong(args[1]) : 60000;
		int port = (args.length > 2) ? Integer.parseInt(args[2]) : 80;
		Server server = new Server();
		server.setRoot(prefix);
		server.setPort(port);
		server.start();
		try { Thread.sleep(delay); } catch (Exception interrupted) {}
		server.stop();
	}
}
