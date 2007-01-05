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
package com.diag.buckaroo.container;

/**
 * This class implements a rooted doubly-linked list which has some unusual properties,
 * such as, with the exception of its reference to its payload object, it never contains
 * a null reference, and the root of any queue or stack that it is used to implement can be
 * found in constant time. It can be used as part of a standalone container, or it can be
 * embedded inside as part of the state of its own payload object (although "embedded" here
 * has fewer useful implications in Java than it does in C++). A single payload object can be
 * on multiple Links simultaneously. Link is a Java port of the equally tragically misnamed
 * Desperado C++ class Link. The basic idea was stolen shamelessly from a C implementation
 * found in the Linux kernel, as discussed in [ T. Aivanzian, <I>Linux Kernel 2.4
 * Internals</I>, August 2001, pp. 19-21 ]. This data structure is probably less useful in
 * Java than in C++, but it still has applications in certain memory-constained designs,
 * if such things exist in the Java world. If nothing else, it may hava some pedagogical
 * value.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 *
 * @date $Date$
 *
 * @param <Type> is the type of the payload object to which this type of ChainLink may
 * point.
 */
public class Link<Type> {

	Link<Type> next = this;
	Link<Type> previous = this;
	Link<Type> root = this;
	Type payload = null;
	
	public Link() { }
	
	public Link(Type payload) { this.payload = payload; }
	
	public Link<Type> getNext() { return next; }
	
	public Link<Type> getPrevious() { return previous; }
	
	public Link<Type> getRoot() { return root; }
	
	public Type getPayload() { return payload; }
	
	public Link<Type> setPayload(Type payload) { this.payload = payload; return this; }
	
	public boolean isChained() { return ((next != this) || (previous != this)); }
	
	public boolean isRoot() { return (root == this); }

	public boolean hasRoot(Link<Type> that) { return (root == that); }
	
	public Link<Type> remove() {
		if (isChained()) {
			next.previous = previous;
			previous.next = next;
			next = this;
			previous = this;
			root = this;
			return this;
		}
		return null;
	}

	protected void finalize() throws Throwable {
		try {
			remove();
		} finally {
			super.finalize();
		}
	}
	
	public Link<Type> insert(Link<Type> that) {
		if (!isChained()) {
			next = that.next;
			previous = that;
			root = that.root;
			next.previous = this;
			that.next = this;
			return this;
		}
		return null;
	}
	
	public interface Functor<Payload> {
		public Link<Payload> evaluate(Link<Payload> that);
	}
	
	public Link<Type> apply(Functor<Type> functor) {
		Link<Type> that = this;
		Link<Type> result;
		while (true) {
			result = functor.evaluate(that);
			if ((result == null) || (result == that)) { return result; }
			that = result;
		}
	}
	
	public boolean isValid() {
		if ((next == this) && (previous == this) && (root == this)) { return true; }
		if ((next == null) || (previous == null) || (root == null)) { return false; }
		if ((next == this) || (previous == this)) { return false; }
		if ((previous.root != root) || (root != next.root)) { return false; }
		if ((previous.next != this) || (this != next.previous)) { return false; }
		return true;
	}
	
	public Link<Type> audit() {
		return apply(
				new Functor<Type>() {
					Link<Type> origin = null;
					public Link<Type> evaluate(Link<Type> that) {
						if (origin == null) { origin = that; }
						if (!that.isValid()) { return that; }
						that = that.getNext();
						if (that == origin) { return null; }
						return that;
					}
				}
		);
	}
}
