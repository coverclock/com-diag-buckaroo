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
import java.util.Date;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;

/**
 * This class implements a simple single-threaded HTTP server that can be
 * embedded inside an application. It is based on prior work by Jon Berg at
 * TurtleMeat. This isn't intended to be a production web server; it's
 * intended to be an embeddable web server that can be modified and extended.
 * When modifying and extending this class, remember that HTTP servers ideally
 * should be stateless (which this one is).
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class Server {

	private int port = 8080;
	private String root = null;
	private boolean enabled = false;
	private Listener listener = null;
	private ResourceBundle bundle = null;
	private SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
	
	// I use this instead of an Enum to make it easier to port to 1.4 for CVM.
	static int UNSUPPORTED = 0, GET = 1, HEAD = 2;
	static String defaults[] = { "index.html", "index.htm", "default.htm" };

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
					output.close();
					input.close();
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
	 * Map a file name suffix (e.g. ".jpg") to a content type (e.g.
	 * "image/jpeg"). If the file name suffix can't be found in the mime
	 * resource bundle, the content type for suffix ".bin" is returned.
	 * @param name is the file name String including the file suffix.
	 * @return a content type String.
	 */
	public String mapNameToType(String name) {
		String contenttype = bundle.getString(".bin");
		String mimetype = null;
		for (Enumeration<String> e = bundle.getKeys(); e.hasMoreElements(); ) {
			mimetype = e.nextElement();
			if (name.endsWith(mimetype)) {
				contenttype = bundle.getString(mimetype);
				break;
			}
		}
		return contenttype;
	}
	
	/**
	 * Maps the file or directory name String to a path String.
	 * @param name is the file name String.
	 * @return a path String.
	 */
	public String mapNameToPath(String name) {
		return (root != null) ? root + name : name;
	}
	
	/**
	 * Return true if the name is a directory, false if it is a file. This
	 * method can be overridden to provide other behavior, for example for
	 * names that do not map to the local file system.
	 * @param name is the directory or file name String.
	 * @return true if directory, false if file.
	 * @throws FileNotFoundException if the file or directory does not exist.
	 */
	public boolean isDirectory(String name) throws FileNotFoundException {
		String path = mapNameToPath(name);
		File metadata = new File(path);
		if (!metadata.exists()) {
			throw new FileNotFoundException(path);
		}
		return metadata.isDirectory();
	}
	
	/**
	 * Handle sending a file from the local file system to the client. This
	 * method can be overridden if other behavior is desired. For example,
	 * certain file names can be generated dynamically.
	 * @param output is the output stream to the client.
	 * @param name is the file name String.
	 */
	protected void doFile(DataOutputStream output, String name) {
		try {
			
			String path = mapNameToPath(name);
			log("File " + path);
			
			String contenttype = mapNameToType(path);
			log("Type " + contenttype);
			
			File metadata = new File(path);
			FileInputStream data = new FileInputStream(metadata);
			
			output.writeBytes(header(200, contenttype, metadata.length()));
			
			int octet;
			int sent = 0;
			try {
				while (true) {
	
					octet = data.read();
					if (octet == -1) { break; }
					output.write(octet);
					++sent;
				}
			} catch (Exception exception) {
				// The far end may close the socket before we complete sending
				// the file if it doesn't like the file content. Usually I
				// find that I've botched the HTTP headers somehow.
				log(exception);
			}
			
			data.close();
			
			log("Sent " + sent);
			
		} catch (Exception exception) {
			// Typically this occurs because the file is not found.
			log(exception);
			try {
				output.writeBytes(header(404));
			} catch (Exception exception2) {
				log(exception2);
			}
		}
	}
	
	/**
	 * Handle sending a directory listing from the local file system to the
	 * client. This method can be overridden if other behavior is desired. For
	 * example, certain directory names can be generated dynamically.
	 * @param output is the output stream to the client.
	 * @param name is the directory name String.
	 */
	protected void doDirectory(DataOutputStream output, String name) {
		try {
			
			String path = mapNameToPath(name);
			log("Directory " + path);
			
			String contenttype = "text/html";
			log("Type " + contenttype);
			
			File metadata = new File(path);
			File files[] = metadata.listFiles();
			
			String parent = null;
			if (name.endsWith("/")) {
				output.writeBytes(header(200, contenttype));
				parent = name.substring(0, name.length() - 1);
			} else {
				output.writeBytes(header(301, contenttype, name + "/"));
				parent = name;
			}
			int index = parent.lastIndexOf('/');
			parent = (index <= 0) ? "/" : parent.substring(0, index + 1);
			
			output.writeBytes("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">");
			output.writeBytes("<HTML>\r\n");
			output.writeBytes("<HEAD><TITLE>Index of " + name + "</TITLE></HEAD>\r\n");
			output.writeBytes("<BODY>\r\n");
			output.writeBytes("<H1>Index of " + name + "</H1><HR><PRE>\r\n");
			output.writeBytes("<A HREF=\"" + parent + "\">..</A>\r\n");
			// I'm using the old form of for() to expedite port to CVM.
			for (int ii = 0; ii < files.length; ++ii) {
				if (files[ii].isDirectory()) {
					output.writeBytes("<A HREF=\"" + files[ii].getName() + "/\">" + files[ii].getName() + "/</A>\r\n");
				} else {
					output.writeBytes("<A HREF=\"" + files[ii].getName() + "\">" + files[ii].getName() + "</A>\r\n");
				}				}
			output.writeBytes("</PRE><HR>\r\n");
			output.writeBytes("<ADDRESS>" + this.getClass().getName() + "</ADDRESS>\r\n");
			output.writeBytes("</BODY></HTML>\r\n");
			
		} catch (Exception exception) {
			// Typically this occurs because the directory is not found.
			log(exception);
			try {
				output.writeBytes(header(404));
			} catch (Exception exception2) {
				log(exception2);
			}
		}
	}

	/**
	 * Services a single HTTP request.
	 * @param input is the HTTP input stream.
	 * @param output is the HTTP output stream.
	 */
	protected void http(BufferedReader input, DataOutputStream output) {
		try {
		
			//This is the two types of request we can handle
			//GET /index.html HTTP/1.0
			//HEAD /index.html HTTP/1.0
			int method = UNSUPPORTED;
			String line = input.readLine();
			StringTokenizer tokenizer = new StringTokenizer(line);
			String command = tokenizer.nextToken().toUpperCase();
			if (command.equals("GET")) {
				method = GET;
			} else if (command.equals("HEAD")) {
				method = HEAD;
			} else {
				method = UNSUPPORTED;
			}
			
			StringBuffer request = new StringBuffer(line);
			request.append("\r\n");
			try {
				while (input.ready()) {
					request.append(input.readLine());
					request.append("\r\n");
				}
			} catch (Exception exception) {
				log(exception);
			}			
			log("Request " + request.toString());

			if (method == UNSUPPORTED) {
				output.writeBytes(header(501));
				return;
			}

			String name = tokenizer.nextToken();
			log("Name " + name);

			boolean directory = false;
			try {
				directory = isDirectory(name);
			} catch (Exception exception) {
				log(exception);
				output.writeBytes(header(404));
				return;
			}
			
			if (directory) {
				for (int ii = 0; ii < defaults.length; ++ii) {
					String effective = name.endsWith("/") ? name + defaults[ii] : name + "/" + defaults[ii];
					try {
						if (!isDirectory(effective)) {
							log("Effective " + effective);
							name = effective;
							directory = false;
							break;
						}
					} catch (Exception exception) {
					}
				}
			}
			
			if (method == GET) {
				if (!directory) {
					doFile(output, name);
				} else {
					doDirectory(output, name);
				}
			}

		} catch (Exception exception) {
			log(exception);
			try {
				output.writeBytes(header(500));
			} catch (Exception exception2) {
				log(exception2);
			}
		}
	}

	/**
	 * Generates an appropriate HTTP header for the output data stream.
	 * @param code is the HTTP return code.
	 * @return the header as a String.
	 */
	protected String header(int code) {
		return header(code, null, -1, null);
	}

	/**
	 * Generates an appropriate HTTP header for the output data stream.
	 * @param code is the HTTP return code.
	 * @param type is the content type of data being returned.
	 * @return the header as a String.
	 */
	protected String header(int code, String type) {
		return header(code, type, -1, null);
	}

	/**
	 * Generates an appropriate HTTP header for the output data stream.
	 * @param code is the HTTP return code.
	 * @param type is the content type of data being returned.
	 * @param location is the new Location of the web page.
	 * @return the header as a String.
	 */
	protected String header(int code, String type, String location) {
		return header(code, type, -1, location);
	}

	/**
	 * Generates an appropriate HTTP header for the output data stream.
	 * @param code is the HTTP return code.
	 * @param type is the content type of data being returned.
	 * @param length is the content length of the data being returned.
	 * @return the header as a String.
	 */
	protected String header(int code, String type, long length) {
		return header(code, type, length, null);
	}

	/**
	 * Generates an appropriate HTTP header for the output data stream.
	 * @param code is the HTTP return code.
	 * @param type is the content type of data being returned.
	 * @param length is the content length of the data being returned.
	 * @param location is the new Location of the web page.
	 * @return the header as a String.
	 */
	protected String header(int code, String type, long length, String location) {
		StringBuffer response = new StringBuffer("HTTP/1.0 ");
		
		switch (code) {
		case 200:
			response.append("200 OK");
			break;
		case 301:
			response.append("301 Moved Permanently");
			break;
		case 400:
			response.append("400 Bad Request");
			break;
		case 403:
			response.append("403 Forbidden");
			break;
		case 404:
			response.append("404 Not Found");
			break;
		case 500:
			response.append("500 Internal Server Error");
			break;
		case 501:
			response.append("501 Not Implemented");
			break;
		default:
			log("Code " + code);
			response.append("000 Unknown Error");
			break;
		}
		response.append("\r\n");
		
		response.append("Connection: close\r\n");
		
		response.append("Server: ");
		response.append(this.getClass().getName());
		response.append("\r\n");
		
		Date date = new Date();
		
		response.append("Date: ");
		String d = format.format(date);
		response.append(d);
		response.append("\r\n");	
		
		if (location != null) {
			response.append("Location: ");
			response.append(location);
			response.append("\r\n");
		}
		
		if (type !=  null) {
			response.append("Last-Modified: ");
			response.append(d);
			response.append("\r\n");
			response.append("Content-Type: ");
			response.append(type);
			response.append("\r\n");
		}
		
		if (length >= 0) {
			response.append("Content-Length: ");
			response.append(new Long(length).toString());
			response.append("\r\n");
		}
		
		response.append("\r\n"); // Separates header from following data.

		String r = response.toString();
		log("Response " + r);
		
		return r;
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
