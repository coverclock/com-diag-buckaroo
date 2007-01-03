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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is a implements a standard syntax for specifying resource locations
 * that may be identified by relative or absolute paths in POSIX or Windows file systems,
 * URLs that may point to local or remote resources, or a resource on the class path.
 * It does not pretend to support all possible valid resource names for any of these
 * categories. It's intended use is for turning strings that are (for example) property
 * values into input streams.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 *
 * @date $Date$
 */
public class Resource {
	
	/**
	 * Return a URL if the resource name is URL-like, null otherwise.
	 * @param name is the resource name.
	 * @return a URL if url-like, null otherwise.
	 */
	static public URL isUrlLike(String name) {
		URL url = null;
		if (name != null) try { url = new URL(name); } catch (MalformedURLException ignore) { }
		return url;
	}
	
	/**
	 * Return a File if the resource name is relative-path-like by the convention
	 * that it starts with the ./ (POSIX) or .\\ (Windows) strings, or a Windows
	 * drive specifier like C: followed by .\\.
	 * @param name is the resource name.
	 * @return a File if relative-path-like, null otherwise.
	 */
	static public File isRelativePathLike(String name) {
		return (name != null) && (
					   name.startsWith("./") ||
					   name.startsWith(".\\") ||
					   name.matches("[a-zA-Z]:\\.\\\\.*")
			   ) ? new File(name) : null;
	}
	
	/**
	 * Return a File if the resource name is absolute-path-like by the convention
	 * that it starts with the / (POSIX) or \\ (Windows) strings, or a Windows
	 * drive specifier like C: followed by \\.
	 * @param name is the resource name.
	 * @return a File if absolute-path-like, null otherwise.
	 */
	static public File isAbsolutePathLike(String name) {
		return (name != null) && (
					name.startsWith("/") ||
		        	name.startsWith("\\") ||
		        	name.matches("[A-Za-z]:\\\\.*")
		        ) ? new File(name) : null;
	}
	
	/**
	 * Return a File if the resource name is either relative-path-like or absolute-path-like.
	 * @param name is the resource name.
	 * @return a File if path-like, null otherwise.
	 */
	static public File isPathLike(String name)
	{
		File file = isRelativePathLike(name);
		if (file == null) { file = isAbsolutePathLike(name); }
		return file;
	}

	/**
	 * Return an input stream for the specified resource name. If the resource name is URL-like, the
	 * resulting URL is used to locate the resource on the network. If the resource name is path-like,
	 * the relative or absolute path is used to locate the resource. Otherwise, the resource name is
	 * assumed to identify a resource on the class path, and the class loader of this class is used
	 * to locate the resource along the classpath. To reference a resource with a relative path name,
	 * prepend its path with "./" (POSIX) or ".\\" (Windows). To reference a resource with an absolute
	 * path name, begin its path with "/" (POSIX) or "\\" (Windows) or something like "C:\\" (Windows).
	 * @param name is the resource name.
	 * @return an input stream connected to the resource.
	 * @throws FileNotFoundException if the file cannot be found.
	 */
	public static InputStream createInputStream(String name) throws FileNotFoundException {
		InputStream stream = null;
		do {
			URL url = isUrlLike(name);
			if (url != null) {
				try { stream = url.openStream(); } catch (Exception ignore) { }
			}
			if (stream != null) { break; }
			File file = isPathLike(name);
			if (file != null) {
				try { stream = new FileInputStream(file); } catch (Exception ignore) { }
			}
			if (stream != null) { break; }
			if (name != null) {
				ClassLoader loader = Resource.class.getClassLoader();
				if (loader == null) { loader = ClassLoader.getSystemClassLoader(); }
				stream = loader.getResourceAsStream(name);
			}
			if (stream != null) { break; }
			throw new FileNotFoundException(name);
		} while (false);
		return stream;
	}
}
