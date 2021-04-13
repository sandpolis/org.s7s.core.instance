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

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

	private static final Pattern ATTR_QUANTIFIER = Pattern.compile(".+\\[(.*)\\.\\.(.*)\\]$");

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

		int i = 0;
		for (var r : resolutions) {
			for (; i < path.length; i++) {
				if (path[i].isEmpty()) {
					path[i] = r;
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

	/**
	 * A timestamp range that selects values from attribute history.
	 */
	protected final long[] quantifier;

	protected Oid(String namespace, String[] path) {
		this.namespace = namespace;
		this.path = path;

		// Parse quantifier if present
		var matcher = ATTR_QUANTIFIER.matcher(path[path.length - 1]);
		if (matcher.matches()) {
			quantifier = new long[] { 0, Long.MAX_VALUE };

			String start = matcher.group(1);
			String end = matcher.group(2);

			if (!start.isBlank()) {
				quantifier[0] = Long.parseLong(start);
			}
			if (!end.isBlank()) {
				quantifier[1] = Long.parseLong(end);
			}
		} else {
			quantifier = null;
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
		return path[0];
	}

	public <E extends STObject> E get() {
		return null;
	}

	@Override
	public int hashCode() {
		return namespace.hashCode() * path.hashCode();
	}

	/**
	 * Determine whether this OID is a descendant of the given OID.
	 *
	 * @param oid The ancestor OID
	 * @return Whether this OID is a descendant
	 */
	public boolean isDescendantOf(Oid oid) {
		Objects.requireNonNull(oid);

		return isDescendantOf(oid.path);
	}

	/**
	 * Determine whether this OID is an ancestor of the given OID.
	 *
	 * @param oid The descendant OID
	 * @return Whether this OID is an ancestor
	 */
	public boolean isAncestorOf(Oid oid) {
		Objects.requireNonNull(oid);

		return isAncestorOf(oid.path);
	}

	/**
	 * Determine whether this OID is a descendant of the given OID.
	 *
	 * @param oid The ancestor OID
	 * @return Whether this OID is a descendant
	 */
	public boolean isDescendantOf(String[] path) {
		Objects.requireNonNull(path);

		// TODO
		return Arrays.mismatch(this.path, path) == Math.min(this.path.length, path.length);
	}

	/**
	 * Determine whether this OID is an ancestor of the given OID.
	 *
	 * @param oid The descendant OID
	 * @return Whether this OID is an ancestor
	 */
	public boolean isAncestorOf(String[] path) {
		Objects.requireNonNull(path);

		// TODO
		return Arrays.mismatch(this.path, path) == Math.min(this.path.length, path.length);
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

	public String last() {
		return path[path.length - 1];
	}

	/**
	 * Get the OID's unique namespace.
	 *
	 * @return The namespace
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

	public long[] quantifier() {
		return quantifier;
	}

	public String[] relativize(Oid oid) {
		return Arrays.copyOfRange(path(), oid.path.length, path.length);
	}

	@Override
	public String toString() {
		return namespace + ":/" + Arrays.stream(path).collect(Collectors.joining("/"));
	}
}
