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
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class Oid implements Comparable<Oid> {

	private static final Logger log = LoggerFactory.getLogger(Oid.class);

	private static boolean checkRelationship(String[] ancestor, String[] descendant) {

		// The descendant cannot be shorter than the ancestor
		if (descendant.length < ancestor.length) {
			return false;
		}

		descendant = descendant.clone();
		ancestor = ancestor.clone();

		// Make any generic (empty) entries in ancestor also generic in the descendant
		for (int i = 0; i < ancestor.length; i++) {
			if (ancestor[i].isEmpty()) {
				descendant[i] = "";
			}
		}

		// Make any generic (empty) entries in descendant also generic in the ancestor
		for (int i = 0; i < ancestor.length; i++) {
			if (descendant[i].isEmpty()) {
				ancestor[i] = "";
			}
		}

		int index = Arrays.mismatch(descendant, ancestor);
		return index == -1 || index == ancestor.length;
	}

	public static Oid of(String oid, String... resolutions) {
		Objects.requireNonNull(oid);

		String namespace;
		String[] path;

		var components = oid.split(":");
		if (components.length == 1) {
			namespace = "com.sandpolis.core.instance";
			path = oid.replaceAll("^/+", "").split("/");
		} else if (components.length == 2) {
			namespace = components[0];
			path = components[1].replaceAll("^/+", "").split("/");
		} else {
			throw new IllegalArgumentException("Invalid namespace");
		}

		// If the only path element is blank, this is the root OID
		if (path.length == 1 && path[0].isEmpty()) {
			path = new String[] {};
		}

		int i = 0;
		for (var r : resolutions) {
			for (; i < path.length; i++) {
				if (path[i].isEmpty()) {
					path[i++] = r;
					break;
				}
			}
		}

		return new Oid(namespace, path);
	}

	/**
	 * The OID unique namespace.
	 */
	protected final String namespace;

	/**
	 * The OID path.
	 */
	protected final String[] path;

	public final Optional<Long> timestampSelectorStart;

	public final Optional<Long> timestampSelectorEnd;

	public final Optional<Integer> indexSelectorStart;

	public final Optional<Integer> indexSelectorEnd;

	protected Oid(String namespace, String[] path) {
		this.namespace = namespace;
		this.path = path;

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

				indexSelectorStart = Optional.empty();
				indexSelectorEnd = Optional.empty();

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

				timestampSelectorStart = Optional.empty();
				timestampSelectorEnd = Optional.empty();

			} else {
				timestampSelectorStart = Optional.empty();
				timestampSelectorEnd = Optional.empty();
				indexSelectorStart = Optional.empty();
				indexSelectorEnd = Optional.empty();
			}
		} else {
			timestampSelectorStart = Optional.empty();
			timestampSelectorEnd = Optional.empty();
			indexSelectorStart = Optional.empty();
			indexSelectorEnd = Optional.empty();
		}
	}

	public Oid child(String id) {
		String[] childPath = Arrays.copyOf(path, path.length + 1);
		childPath[childPath.length - 1] = id;
		return new Oid(namespace, childPath);
	}

	@Override
	public int compareTo(Oid oid) {
		return Arrays.compare(path(), oid.path());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Oid other) {
			return namespace == other.namespace && Arrays.equals(path, other.path);
		}
		return false;
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

	@Override
	public int hashCode() {
		return namespace.hashCode() * path.hashCode();
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
		return !Arrays.stream(path()).anyMatch(String::isEmpty);
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

	/**
	 * @return The OID's unique namespace.
	 */
	public String namespace() {
		return namespace;
	}

	/**
	 * Get the OID's path.
	 *
	 * @return The path
	 */
	public String[] path() {
		return path;
	}

	@Override
	public String toString() {
		return namespace + ":/" + Arrays.stream(path).collect(Collectors.joining("/"));
	}

	public Oid relative(String path) {
		return new Oid(namespace, ObjectArrays.concat(this.path, path.split("/"), String.class));
	}
}
