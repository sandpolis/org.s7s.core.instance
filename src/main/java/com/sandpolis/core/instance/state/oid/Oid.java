//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.state.oid;

import static com.sandpolis.core.instance.state.STStore.STStore;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.sandpolis.core.instance.state.st.STObject;

/**
 * <p>
 * An {@link Oid} corresponds to one or more objects in a real or virtual state
 * tree. OIDs locate objects in the state tree with an immutable sequence of
 * strings called the "path".
 *
 * <p>
 * When represented as a String, OID components are joined with "/" and prefixed
 * with the namespace followed by a ":".
 *
 * <h3>Concrete/Generic</h3>
 * <p>
 * An OID is either "concrete", meaning that it corresponds to exactly one
 * virtual object, or "generic" which means the OID corresponds to multiple
 * objects of the same type.
 */
public record Oid(

		/**
		 * 
		 */
		String namespace,

		/**
		 * 
		 */
		String[] path,

		/**
		 * 
		 */
		Optional<Integer> indexSelectorEnd,

		/**
		 * 
		 */
		Optional<Integer> indexSelectorStart,

		/**
		 * 
		 */
		Optional<Long> timestampSelectorEnd,

		/**
		 * 
		 */
		Optional<Long> timestampSelectorStart) {

	private static final String DEFAULT_NAMESPACE = "com.sandpolis.core.instance";

	private static final Logger log = LoggerFactory.getLogger(Oid.class);

	private static final Predicate<String> NAMESPACE_VALIDATOR = Pattern.compile("^[a-z\\.]+$").asMatchPredicate();

	private static final Predicate<String> PATH_VALIDATOR = Pattern.compile("^[a-z0-9_\\-*]+$").asMatchPredicate();

	private static boolean checkRelationship(String[] ancestor, String[] descendant) {

		// The descendant cannot be shorter than the ancestor
		if (descendant.length < ancestor.length) {
			return false;
		}

		descendant = descendant.clone();
		ancestor = ancestor.clone();

		// Make any generic entries in ancestor also generic in the descendant
		for (int i = 0; i < ancestor.length; i++) {
			if (ancestor[i].equals("*")) {
				descendant[i] = "*";
			}
		}

		// Make any generic entries in descendant also generic in the ancestor
		for (int i = 0; i < ancestor.length; i++) {
			if (descendant[i].equals("*")) {
				ancestor[i] = "*";
			}
		}

		int index = Arrays.mismatch(descendant, ancestor);
		return index == -1 || index == ancestor.length;
	}

	public static Oid of(String oid, String... resolutions) {
		Objects.requireNonNull(oid);

		String namespace;
		String[] path;
		Optional<Integer> indexSelectorEnd = Optional.empty();
		Optional<Integer> indexSelectorStart = Optional.empty();
		Optional<Long> timestampSelectorEnd = Optional.empty();
		Optional<Long> timestampSelectorStart = Optional.empty();

		// Determine namespace and path
		var components = oid.split(":");
		if (components.length == 1) {
			namespace = DEFAULT_NAMESPACE;
			path = oid.replaceAll("^/+", "").split("/");
		} else if (components.length == 2) {
			namespace = components[0];
			path = components[1].replaceAll("^/+", "").split("/");
		} else {
			throw new IllegalArgumentException("Invalid namespace");
		}

		// Validate namespace
		if (namespace == null || !NAMESPACE_VALIDATOR.test(namespace)) {
			throw new IllegalArgumentException("Illegal namespace: " + namespace);
		}

		// If the only path element is blank, this is the root OID
		if (path.length == 1 && path[0].isEmpty()) {
			path = new String[] {};
		}

		// Perform resolutions
		int i = 0;
		for (var r : resolutions) {
			for (; i < path.length; i++) {
				if (path[i].equals("*")) {
					path[i++] = r;
					break;
				}
			}
		}

		// Validate path
		for (i = 0; i < path.length; i++) {
			if (!PATH_VALIDATOR.test(path[i])) {
				throw new IllegalArgumentException("Illegal path element: " + path[i]);
			}
		}

		// Parse temporal selectors at end
		if (path.length > 0) {
			String last = path[path.length - 1];
			if (last.endsWith(")")) {
				int s = last.lastIndexOf('(');
				if (s == -1) {
					throw new IllegalArgumentException("Expected timestamp range selector '('");
				}
				var range = last.substring(s + 1, last.length() - 1);
				if (range.isBlank()) {
					throw new IllegalArgumentException("Empty timestamp range selector");
				}

				// Remove selector from path
				path[path.length - 1] = last.substring(0, s);

				var parts = range.split("\\.\\.");
				if (parts.length == 2) {
					if (!parts[0].isBlank()) {
						timestampSelectorStart = Optional.of(Long.parseLong(parts[0]));
					} else {
						timestampSelectorStart = Optional.of(0L);
					}

					if (!parts[1].isBlank()) {
						timestampSelectorEnd = Optional.of(Long.parseLong(parts[1]));
					} else {
						timestampSelectorEnd = Optional.of(Long.MAX_VALUE);
					}
				} else {
					timestampSelectorStart = Optional.of(Long.parseLong(parts[0]));
					timestampSelectorEnd = Optional.of(Long.parseLong(parts[0]));
				}

			} else if (last.endsWith("]")) {
				int s = last.lastIndexOf('[');
				if (s == -1) {
					throw new IllegalArgumentException("Expected index range selector '['");
				}
				var range = last.substring(s + 1, last.length() - 1);
				if (range.isBlank()) {
					throw new IllegalArgumentException("Empty index range selector");
				}

				// Remove selector from path
				path[path.length - 1] = last.substring(0, s);

				var parts = range.split("\\.\\.");
				if (parts.length == 2) {
					if (!parts[0].isBlank()) {
						indexSelectorStart = Optional.of(Integer.parseInt(parts[0]));
					} else {
						indexSelectorStart = Optional.of(0);
					}

					if (!parts[1].isBlank()) {
						indexSelectorEnd = Optional.of(Integer.parseInt(parts[1]));
					} else {
						indexSelectorEnd = Optional.of(Integer.MAX_VALUE);
					}
				} else {
					indexSelectorStart = Optional.of(Integer.parseInt(parts[0]));
					indexSelectorEnd = Optional.of(Integer.parseInt(parts[0]));
				}
			}
		}

		return new Oid(namespace, path, indexSelectorEnd, indexSelectorStart, timestampSelectorEnd,
				timestampSelectorStart);
	}

	public Oid child(String id) {
		String[] childPath = Arrays.copyOf(path, path.length + 1);
		childPath[childPath.length - 1] = id;
		return new Oid(namespace, childPath, indexSelectorEnd, indexSelectorStart, timestampSelectorEnd,
				timestampSelectorStart);
	}

	public String first() {
		if (path.length > 0) {
			return path[0];
		} else {
			return null;
		}
	}

	public <E extends STObject> E get() {
		return (E) STStore.get(this);
	}

	/**
	 * Determine whether this OID is an ancestor of the given OID.
	 *
	 * @param descendant The descendant OID
	 * @return Whether this OID is an ancestor
	 */
	public boolean isAncestorOf(Oid descendant) {
		Objects.requireNonNull(descendant);

		return isAncestorOf(descendant.path);
	}

	/**
	 * Determine whether this OID is an ancestor of the given OID.
	 *
	 * @param oid The descendant OID
	 * @return Whether this OID is an ancestor
	 */
	public boolean isAncestorOf(String[] descendant) {
		Objects.requireNonNull(descendant);

		return checkRelationship(this.path, descendant);
	}

	/**
	 * Determine whether the OID corresponds to exactly one entity (concrete) or
	 * multiple entities (generic). The OID is generic if it contains at least one
	 * empty component.
	 *
	 * @return Whether the OID is concrete
	 */
	public boolean isConcrete() {
		return !Arrays.stream(path()).anyMatch(c -> c.equals("*"));
	}

	/**
	 * Determine whether this OID is a descendant of the given OID.
	 *
	 * @param ancestor The ancestor OID
	 * @return Whether this OID is a descendant
	 */
	public boolean isDescendantOf(Oid ancestor) {
		Objects.requireNonNull(ancestor);

		return isDescendantOf(ancestor.path);
	}

	/**
	 * Determine whether this OID is a descendant of the given OID.
	 *
	 * @param ancestor The ancestor OID
	 * @return Whether this OID is a descendant
	 */
	public boolean isDescendantOf(String[] ancestor) {
		Objects.requireNonNull(ancestor);

		return checkRelationship(ancestor, this.path);
	}

	public String last() {
		if (path.length > 0) {
			return path[path.length - 1];
		} else {
			return null;
		}
	}

	public String pathString() {
		return Arrays.stream(path).collect(Collectors.joining("/"));
	}

	public Oid relative(String path) {
		return new Oid(namespace, ObjectArrays.concat(this.path, path.split("/"), String.class), indexSelectorEnd,
				indexSelectorStart, timestampSelectorEnd, timestampSelectorStart);
	}

	public Oid resolve(String... resolutions) {

		String[] path = this.path;

		int i = 0;
		for (var r : resolutions) {
			for (; i < path.length; i++) {
				if (path[i].equals("*")) {
					path[i++] = r;
					break;
				}
			}
		}

		return new Oid(namespace, path, indexSelectorEnd, indexSelectorStart, timestampSelectorEnd,
				timestampSelectorStart);
	}

	public Oid resolveLast(String... resolutions) {

		String[] path = this.path;

		int i = path.length - 1;
		for (var r : Lists.reverse(Arrays.asList(resolutions))) {
			for (; i > 0; i--) {
				if (path[i].equals("*")) {
					path[i--] = r;
					break;
				}
			}
		}

		return new Oid(namespace, path, indexSelectorEnd, indexSelectorStart, timestampSelectorEnd,
				timestampSelectorStart);
	}

	@Override
	public String toString() {
		return namespace + ":/" + pathString();
	}
}
