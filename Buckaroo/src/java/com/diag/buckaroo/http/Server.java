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

	protected static final Logger DEFAULT_LOGGER = Logger.getLogger(Server.class.getName());

	private Logger log = DEFAULT_LOGGER;
	private int port = 80;
	private String root = "";
	private boolean enabled = false;
	private Listener listener = null;
	
	enum Method {
		UNSUPPORTED,
		GET,
		HEAD
	}

	enum Type {
		UNSUPPORTED,
		JPG,
		GIF,
		ZIP,
		PDF,
		TXT,
		HTML
	}
	
	/**
	 * Defines the listener thread that waits for incoming HTTP requests.
	 */
	public class Listener extends Thread {	
		
		/**
		 * Implements listener thread body.
		 */
		public void run() {
			getLogger().fine("Running.");
			
			ServerSocket listensocket = null;
			
			try {
				getLogger().fine("Binding " + port + ".");
				listensocket = new ServerSocket(port);
			} catch (Exception exception) {
				getLogger().log(Level.WARNING, exception.toString(), exception);
				return;
			}

			while (enabled) {
				getLogger().fine("Listening.");
				try {
					Socket connectionsocket = listensocket.accept();
					InetAddress client = connectionsocket.getInetAddress();
					getLogger().fine("Connected " + client.getHostName() + ".");
					BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
					DataOutputStream output = new DataOutputStream(connectionsocket.getOutputStream());
					http(input, output);
					input.close();
					output.close();
					connectionsocket.close();
				} catch (Exception exception) {
					getLogger().log(Level.WARNING, exception.toString(), exception);
				}
			}
			
			try {
				listensocket.close();
			} catch (Exception exception) {
				getLogger().log(Level.WARNING, exception.toString(), exception);
			}
		}
		
	}
	
	/**
	 * Constructor.
	 */
	public Server() {}
	
	/**
	 * Constructor.
	 * @param port is an HTTP listening port.
	 */
	public Server(int port) {
		this.port = port;
	}
	
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
	public Server start() {
		if (listener == null)
		{
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
	public Server stop() {
		if (listener != null) {
			enabled = false;
			try {
				listener.join(1000);
			} catch (Exception exception) {
				getLogger().log(Level.WARNING, exception.toString(), exception);
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
		Method method = Method.UNSUPPORTED;
		String path = new String();
		
		try {
			//This is the two types of request we can handle
			//GET /index.html HTTP/1.0
			//HEAD /index.html HTTP/1.0
			String tmp = input.readLine();
			String tmp2 = new String(tmp);
			tmp.toUpperCase();
			if (tmp.startsWith("GET")) {
				method = Method.GET;
			} else if (tmp.startsWith("HEAD")) {
				method = Method.HEAD;
			} else {
				method = Method.UNSUPPORTED;
			}

			if (method == Method.UNSUPPORTED) {
				try {
					output.writeBytes(header(501, Type.UNSUPPORTED));
					return;
				} catch (Exception exception) {
					getLogger().log(Level.WARNING, exception.toString(), exception);
				}
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
			path = root + tmp2.substring(start + 2, end);
		} catch (Exception exception) {
			getLogger().log(Level.WARNING, exception.toString(), exception);
		}

		getLogger().fine("Requested \"" + new File(path).getAbsolutePath() + "\".");
		FileInputStream requested = null;
		try {
			requested = new FileInputStream(path);
		} catch (Exception exception) {
			try {
				output.writeBytes(header(404, Type.UNSUPPORTED));
			} catch (Exception ignored) {}
			getLogger().log(Level.WARNING, exception.toString(), exception);
		}

		try {
			Type type = Type.UNSUPPORTED;
			if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
				type = Type.JPG;
			} else if (path.endsWith(".gif")) {
				type = Type.GIF;
			} else if (path.endsWith(".zip") || path.endsWith(".exe") || path.endsWith(".tar")) {
				type = Type.ZIP;
			} else if (path.endsWith(".pdf")) {
				type = Type.PDF;
			} else if (path.endsWith(".txt")) {
				type = Type.TXT;
			} else {
				type = Type.HTML;
			}
			output.writeBytes(header(200, type));

			if (method == Method.GET) {
				while (true) {
					int b = requested.read();
					if (b == -1) {
						break;
					}
					output.write(b);
				}
			}

			requested.close();
		} catch (Exception ignored) {}

	}

	/**
	 * Generates an appropriate HTTP header for the output data stream.
	 * @param code is the HTTP return code.
	 * @param type is the type of data being returned.
	 * @return
	 */
	protected String header(int code, Type type) {
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

		if (type == Type.JPG) {
			s = s + "Content-Type: image/jpeg\r\n";
		} else if (type == Type.GIF) {
			s = s + "Content-Type: image/gif\r\n";
		} else if (type == Type.ZIP) {
			s = s + "Content-Type: application/x-zip-compressed\r\n";
		} else if (type == Type.PDF) {
			s = s + "Content-Type: application/pdf\r\n";
		} else if (type == Type.TXT) {
			s = s + "Content-Type: text/plain\r\n";
		} else if (type == Type.HTML) {
			s = s + "Content-Type: text/html\r\n";
		}

		s = s + "\r\n";

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
