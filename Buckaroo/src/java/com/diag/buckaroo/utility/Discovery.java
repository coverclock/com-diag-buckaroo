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
package com.diag.buckaroo.utility;

import com.diag.buckaroo.utility.UrlUtf8;
import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Discovers information about or from the platform on which the Java Virtual Machine executes.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 *
 * @date $Date$
 *
 */
public class Discovery {
	
	/**
	 * Returns a String containing the path to the specified resource bundle.
	 * @param name is the name of the resource bundle.
	 * @return a String or null if it cannot be determined.
	 */
	public static String getResourceBundlePath(String name) {
		if (!name.startsWith("/")) { name = "/" + name; }
		URL url = Discovery.class.getResource(name);
		String path = null;
		if (url != null) {
			String unnormalized = url.getPath();
			String decoded = UrlUtf8.decode(unnormalized);
			File file = new File(decoded);
			path = file.toString();
		}
		return path;
	}
	
	/**
	 * Returns a String containing the name of the jar file or the class file containing the specified class.
	 * @param klass is the class.
	 * @return a String or null if it cannot be determined.
	 */
	public static String getClassFilePath(Class klass) {
		String path = null;
		do {
			String name = klass.getName().replace('.', '/') + ".class";
			URL url = klass.getResource("/" + name);
			if (url != null) {
				URLConnection connection = null;
				try { connection = url.openConnection(); } catch (Exception ignore) { }
				if (connection != null) {
					if (connection instanceof JarURLConnection) {
						JarURLConnection juc = (JarURLConnection)connection;
						JarFile jar = null;
						try { jar = juc.getJarFile(); } catch (Exception ignore) { }
						if (jar != null) {
							String unnormalized = jar.getName();
							String decoded = UrlUtf8.decode(unnormalized);
							File file = new File(decoded);
							path = file.toString();
							break;
						}
					}
				}
				String unnormalized = url.getPath();
				String decoded = UrlUtf8.decode(unnormalized);
				File file = new File(decoded);
				path = file.toString();
				break;
			}
		} while (false);
		return path;
	}
	
	/**
	 * Returns a String containing the name of the jar file or the class file containing the class of the
	 * specified object.
	 * @param object is the object.
	 * @return a String or null if it cannot be determined.
	 */
	public static String getClassFilePath(Object object) {
		return getClassFilePath(object.getClass());
	}
	
	/**
	 * Returns the class path in the form of an array of Files.
	 * @return an array of Files.
	 */
	public static File[] getClassPathFiles() {
		File[] files = null;
		String classpath = System.getProperty("java.class.path");
		if (classpath != null) {
			String[] paths = classpath.split(";");
			if (paths != null) {
				files = new File[paths.length];
				for (int ii = 0; ii < paths.length; ++ii) {
					files[ii] = new File(paths[ii]);
				}
			}
		}
		return files;
	}
	
	/**
	 * Returns the class path in the form of an array of Strings.
	 * @return an array of Strings.
	 */
	public static String[] getClassPathStrings() {
		String[] paths = null;
		File[] files = getClassPathFiles();
		if (files != null) {
			paths = new String[files.length];
			for (int ii = 0; ii < files.length; ++ii) {
				paths[ii] = files[ii].toString();
			}
		}
		return paths;
	}
	
	/**
	 * Returns the Date of when the specified class was compiled.
	 * @param klass is the class.
	 * @return a Date or null if it cannot be determined.
	 */
	public static Date getClassCompilationDate(Class klass) {
		Date date = null;
		do {
			String name = klass.getName().replace('.', '/') + ".class";
			URL url = klass.getResource("/" + name);
			if (url != null) {
				URLConnection connection = null;
				try { connection = url.openConnection(); } catch (Exception ignore) { }
				if (connection != null) {
					if (connection instanceof JarURLConnection) {
						JarURLConnection juc = (JarURLConnection)connection;
						JarFile jar = null;
						try { jar = juc.getJarFile(); } catch (Exception ignore) { }
						if (jar != null) {
							ZipEntry entry = jar.getEntry(name);
							if (entry != null) {
								long epoch = entry.getTime();
								date = new Date(epoch);
								break;
							}
						}
					}
				}
				String unnormalized = url.getPath();
				String decoded = UrlUtf8.decode(unnormalized);
				File file = new File(decoded);
				long epoch = file.lastModified();
				date = new Date(epoch);
				break;
			}
		} while (false);
		return date;
	}
	
	/**
	 * Returns the Date of when the class of the specified object was compiled.
	 * @param object is the object.
	 * @return a Date or null if it cannot be determined.
	 */
	public static Date getClassCompilationDate(Object object) {
		return getClassCompilationDate(object.getClass());
	}
	
	/**
	 * Returns the ClassLoader for the specified class.
	 * @param klass is the class.
	 * @return a ClassLoader; if it cannot be determined, the system class loader is returned.
	 */
	public static ClassLoader getClassLoader(Class klass) {
		ClassLoader loader = null;
		if (klass != null) { loader = klass.getClassLoader(); }
		if (loader == null) { loader = ClassLoader.getSystemClassLoader(); }
		return loader;
	}
	
	/**
	 * Returns the ClassLoader for the class of the specified object.
	 * @param object is the object.
	 * @return a ClassLoader; if it cannot be determined, the system class loader is returned.
	 */
	public static ClassLoader getClassLoader(Object object) {
		return getClassLoader((object != null) ? object.getClass() : (Class)null);
	}
	
	/**
	 * Returns a String containing the path of the current working directory.
	 * @return a path String or null if it cannot be determined.
	 */
	public static String getCurrentWorkingDirectoryPath() {
		String path = null;
		try { path = new File(".").getCanonicalPath(); } catch (Exception ignore) { }
		return path;
	}
}
