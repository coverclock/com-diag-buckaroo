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
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.diag.buckaroo.jmx.LifeCycle;

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
public class Server extends Thread {

	protected static final Logger DEFAULT_LOGGER = Logger.getLogger(Server.class.getName());

	private Logger log = DEFAULT_LOGGER;
	private int port;

	/**
	 * Constructor.
	 * @param port
	 */
	public Server(int port) {
		this.port = port;
		this.start();
	}
	
	/**
	 * Set the Java logger used by the Server.
	 * @param log is a java logger.
	 * @return this object.
	 */
	public Server setLogger(Logger log) {
		this.log = log;
		return this;
	}
	
	/**
	 * Get the Java logger used by the Server.
	 * @return a java logger.
	 */
	public Logger getLogger() {
		return log;
	}

	public void run() {
		ServerSocket serversocket = null;
		getLogger().fine("The simple httpserver v. 0000000000\nCoded by Jon Berg" + "<jon.berg[on server]turtlemeat.com>\n\n");
		try {
			getLogger().fine("Trying to bind to localhost on port " + Integer.toString(port) + "...");
			serversocket = new ServerSocket(port);
		} catch (Exception e) {
			getLogger().fine("\nFatal Error:" + e.getMessage());
			return;
		}

		getLogger().fine("OK!\n");

		while (true) {
			getLogger().fine("\nReady, Waiting for requests...\n");
			try {
				Socket connectionsocket = serversocket.accept();
				InetAddress client = connectionsocket.getInetAddress();
				getLogger().fine(client.getHostName() + " connected to server.\n");
				BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
				DataOutputStream output = new DataOutputStream(connectionsocket.getOutputStream());
				http(input, output);
			} catch (Exception e) {
				getLogger().fine("\nError:" + e.getMessage());
			}
		}
	}

	private void http(BufferedReader input, DataOutputStream output) {
		int method = 0; //1 get, 2 head, 0 not supported
		String http = new String();
		String path = new String();
		String file = new String();
		String user_agent = new String();
		try {
			//This is the two types of request we can handle
			//GET /index.html HTTP/1.0
			//HEAD /index.html HTTP/1.0
			String tmp = input.readLine();
			String tmp2 = new String(tmp);
			tmp.toUpperCase();
			if (tmp.startsWith("GET")) {
				method = 1;
			} else if (tmp.startsWith("HEAD")) {
				method = 2;
			} else {
				method = 0;
			}

			if (method == 0) {
				try {
					output.writeBytes(header(501, 0));
					output.close();
					return;
				} catch (Exception e3) {
					getLogger().fine("error:" + e3.getMessage());
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
			path = tmp2.substring(start + 2, end);
		} catch (Exception e) {
			getLogger().fine("error " + e.getMessage());
		}

		getLogger().fine("\nClient requested:" + new File(path).getAbsolutePath() + "\n");
		FileInputStream requestedfile = null;

		try {
			requestedfile = new FileInputStream(path);
		} catch (Exception e) {
			try {
				output.writeBytes(header(404, 0));
				output.close();
			} catch (Exception e2) {}
			getLogger().fine("error " + e.getMessage());
		}

		try {
			int type_is;
			if (path.endsWith(".zip") || path.endsWith(".exe") || path.endsWith(".tar")) {
				type_is = 3;
			} else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
				type_is = 1;
			} else if (path.endsWith(".gif")) {
				type_is = 2;
			} else {
				type_is = 0;
			}
			output.writeBytes(header(200, 5));

			// 1 is GET, 2 is HEAD which skips the body.
			if (method == 1) {
				while (true) {
					int b = requestedfile.read();
					if (b == -1) {
						break; //end of file
					}
					output.write(b);
				}
			}

			output.close();
			requestedfile.close();
		}

		catch (Exception e) {}

	}

	private String header(int code, int type) {
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
		s = s + "Server: SimpleHTTPtutorial v0\r\n";

		switch (type) {
		case 0:
			break;
		case 1:
			s = s + "Content-Type: image/jpeg\r\n";
			break;
		case 2:
			s = s + "Content-Type: image/gif\r\n";
		case 3:
			s = s + "Content-Type: application/x-zip-compressed\r\n";
		default:
			s = s + "Content-Type: text/html\r\n";
		break;
		}

		s = s + "\r\n";

		return s;
	}
}
