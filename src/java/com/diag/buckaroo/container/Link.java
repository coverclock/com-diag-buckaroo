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
 * This generic class implements a rooted doubly-linked circular list which has some useful
 * properties, such as, with the exception of its reference to its payload object, it never
 * contains a null reference, and the root of any queue or stack that it is used to implement
 * can be found in constant time. It can be used as part of a standalone container, or it can
 * be embedded inside as part of the state of its own payload object (although "embedded" here
 * has fewer useful implications in Java than it does in C++). A single payload object can be
 * on multiple Links simultaneously. Link is a Java port of the equally tragically misnamed
 * Desperado C++ class Link. The basic idea was stolen shamelessly from a C implementation
 * found in the Linux kernel, as discussed in [ T. Aivanzian, <I>Linux Kernel 2.4
 * Internals</I>, August 2001, pp. 19-21 ]. This data structure is probably less useful in
 * Java than in C++, but it still has applications in certain memory-constained designs,
 * if such things exist in the Java world. If nothing else, it may hava some pedagogical
 * value regarding generic types, anonymous inner classes, and functional programming.
 * Important safety tip: objects of this class are not thread safe. Synchronization is
 * the responsibility of the application. Developers may wish to consider synchronizing
 * on the root of a chain when doing insert or remove operations.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 *
 * @date $Date$
 *
 * @param <Type> is the type of the payload object to which this type of ChainLink may point.
 */
public class Link<Type> {

	Link<Type> next = this;
	Link<Type> previous = this;
	Link<Type> root = this;
	Type payload;
	
	/**
	 * Ctor. The payload reference is set to null.
	 */
	public Link() { }
	
	/**
	 * Ctor. The payload reference is set to the specified value.
	 * @param payload refers to an object of type Type.
	 */
	public Link(Type payload) { this.payload = payload; }
	
	/**
	 * Returns the reference to the next Link.
	 * @return the reference to the next Link.
	 */
	public Link<Type> getNext() { return next; }
	
	/**
	 * Returns the reference to the previous Link.
	 * @return the reference to the previous Link.
	 */
	public Link<Type> getPrevious() { return previous; }
	
	/**
	 * Returns the reference to the root Link.
	 * @return the reference to the root Link.
	 */
	public Link<Type> getRoot() { return root; }
	
	/**
	 * Returns the reference to the payload object.
	 * @return the reference to the payload object.
	 */
	public Type getPayload() { return payload; }
	
	/**
	 * Sets the reference to the specified payload object.
	 * @param payload refers to an object of type Type. 
	 * @return a reference to this Link.
	 */
	public Link<Type> setPayload(Type payload) { this.payload = payload; return this; }
	
	/**
	 * Returns true if this Link is chained to another Link, false if it is chained only
	 * to itself. This check is done in O(1) time.
	 * @return true if this Link is chained, false otherwise.
	 */
	public boolean isChained() { return ((next != this) || (previous != this)); }
	
	/**
	 * Returns true if this Link is a root, which includes being unchained and hence a root
	 * to just itself. This check is done in O(1) time.
	 * @return true if this Link is a root, false otherwise.
	 */
	public boolean isRoot() { return (root == this); }

	/**
	 * Returns true if this Link has that Link has its root, false otherwise.
	 * @param that refers to another Link. This check is done in O(1) time.
	 * @return true if this Link has that Link as its root, false otherwise.
	 */
	public boolean hasRoot(Link<Type> that) { return (root == that); }
	
	/**
	 * If this Link is chained to one or more other Links, remove it from that chain.
	 * Upon completion, this will be a valid unchained Link and the the chain that it was on
	 * will be intact without this Link. If this Link was the root of a chain of just two
	 * Links, the other Link is rooted back to itself. If this Link was the root of a chain
	 * with more than two Links, the resulting chain is left still rooted to this Link, which
	 * is unlikely to be what you want. A reference to this Link is returned. If this Link is
	 * not chained to other Links, null is returned. A remove is done in O(1) time.
	 * @return a reference to this Link if the remove was successful, false otherwise.
	 */
	public Link<Type> remove() {
		if (isChained()) {
			next.previous = previous;
			if (next.previous == next) { next.root = next; }
			previous.next = next;
			if (previous.next == previous) { previous.root = previous; }
			next = this;
			previous = this;
			root = this;
			return this;
		}
		return null;
	}

	/**
	 * Removes this Link from any chain upon finalization. Since the only way this
	 * Link could be garbage collected is for all references to it or to any Link
	 * on its chain to be dropped (except for the references in the Links themselves),
	 * this really is not necessary. (Depending on how the mark-and-sweep garbage
	 * collector works, this may actually slow down the recovery of the Links.)
	 */
	protected void finalize() throws Throwable {
		try {
			remove();
		} finally {
			super.finalize();
		}
	}
	
	/**
	 * If this Link is not chained to one or more other Links, insert it into a chain
	 * after that Link, including a chain in which that Link is the only Link. This
	 * Link inherits the root of that Link. Upon completion, this Link will be part of
	 * a chain just after that Link. A reference to this Link is returned. If this Link
	 * is already chained to other Links, null is returned; you must remove this Link
	 * from its own chain before inserting it into another chain. An insert is done in
	 * O(1) time. A Link could be inserted after itself (which would have no effect
	 * on its state), but null is returned if this is attempted.
	 * @param that refers to a Link that this Link is to be inserted after.
	 * @return a reference to this Link if the insert was successful, false otherwise.
	 */
	public Link<Type> insert(Link<Type> that) {
		if (!isChained() && (this != that)) {
			next = that.next;
			previous = that;
			root = that.root;
			next.previous = this;
			that.next = this;
			return this;
		}
		return null;
	}
	
	/**
	 * Sets this Link as the root of the chain (if any) that it is on. A root is done
	 * in O(N) time.
	 * @return a reference to this Link.
	 */
	public Link<Type> root() {
		Link<Type> that = this;
		do { that.root = this; that = that.next; } while (that != this);
		return this;
	}
	
	/**
	 * A Functor is an object containing an evaluate method which can be applied to
	 * Links in a chain by the Link apply method.
	 *
	 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
	 *
	 * @version $Revision$
	 *
	 * @date $Date$
	 *
	 * @param <Type> is the type of the payload object that Links of this type contain.
	 */
	public interface Functor<Type> {
		public Link<Type> evaluate(Link<Type> that);
	}
	
	/**
	 * Applies a Functor to the Links in a chain starting with this Link. The Functor decides
	 * how to traverse the chain by returned a reference to the next Link to visit, typically
	 * the next Link or the previous Link. The apply method continuously applies the Functor
	 * to the indicated Link until the Functor returns either null, or the Link that was its
	 * argument. When this occurs, this method returns that final value returned by the
	 * Functor. The apply method makes no attempt to insure that the Functor visits every
	 * Link in a chain, or that it visits a particular Link only once. An apply is done
	 * roughly in O(N) time, depending on how the Functor traverse the chain.
	 * @param functor refers to a Functor.
	 * @return a reference to a Link or null.
	 */
	public Link<Type> apply(Functor<Type> functor) {
		Link<Type> that = this;
		Link<Type> result;
		while (true) {
			result = functor.evaluate(that);
			if ((result == null) || (result == that)) { return result; }
			that = result;
		}
	}
	
	/**
	 * Returns true if this Link examined more or less in isolation appears to be valid.
	 * This method is used mostly in unit testing. This check is done in O(1) time.
	 * @return true if this Link appears valid, false otherwise.
	 */
	public boolean isValid() {
		if ((next == this) && (previous == this) && (root == this)) { return true; }
		if ((next == null) || (previous == null) || (root == null)) { return false; }
		if ((next == this) || (previous == this)) { return false; }
		if ((previous.root != root) || (root != next.root)) { return false; }
		if ((previous.next != this) || (this != next.previous)) { return false; }
		return true;
	}
	
	/**
	 * Audits an entire chain starting at this Link by validating every Link on the chain.
	 * This method is used mostly in unit testing. This audit is done in O(N) time.
	 * @return null if the chain appears valid, a reference to the first invalid Link
	 * otherwise.
	 */
	public Link<Type> audit() {
		return apply(
				new Functor<Type>() {
					
					Link<Type> origin;
					
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
	
	/**
	 * Finds the first Link on a chain, starting with this Link, that contains the indicated
	 * payload object. This can be used to remove the first Link containing the payload
	 * object on chains in which the Link is not part of the payload object itself. If the
	 * Link is part of the payload object iself, it can be removed from the chain directly
	 * in O(1) time. A find is done in O(N) time. If a Link containing the specified payload
	 * object could not be found on the chain, null is returned.
	 * @param payload refers to a payload object.
	 * @return a reference to a Link that contains the payload object, or null if such a Link
	 * could not be found.
	 */
	public Link<Type> find(Type payload) {
		return apply(
				new Functor<Type>() {
					
					Link<Type> origin;
					Type payload;
					
					public Functor<Type> setTarget(Type payload) {
						this.payload = payload;
						return this;
					}
					
					public Link<Type> evaluate(Link<Type> that) {
						if (origin == null) { origin = that; }
						if (that.getPayload() == payload) { return that; }
						that = that.getNext();
						if (that == origin) { return null; }
						return that;
					}
					
				}.setTarget(payload)
		);
	}
}
