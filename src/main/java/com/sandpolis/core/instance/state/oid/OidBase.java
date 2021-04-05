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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class OidBase implements Oid {

	private Map<OidData<?>, Object> data = new HashMap<>();

	/**
	 * The OID unique namespace.
	 */
	protected final String namespace;

	/**
	 * The OID path.
	 */
	protected final String[] path;

	public OidBase(String namespace, String[] path) {
		if (path.length == 0)
			throw new IllegalArgumentException();
		if (Arrays.stream(path).anyMatch(Objects::isNull))
			throw new IllegalArgumentException();

		this.namespace = namespace;
		this.path = path;
	}

	public OidBase(String namespace, String path) {
		this(namespace, path.replaceAll("^/+", "").split("/"));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OidBase) {
			var other = ((OidBase) obj);
			return namespace == other.namespace && Arrays.equals(path, other.path);
		}
		return false;
	}

	@Override
	public <T> T getData(OidData<T> dataType) {
		return (T) data.get(dataType);
	}

	@Override
	public int hashCode() {
		return (namespace + path).hashCode();
	}

	@Override
	public String namespace() {
		return namespace;
	}

	@Override
	public <T, O extends Oid> O setData(OidData<T> dataType, T item) {
		data.put(dataType, item);
		return (O) this;
	}

	@Override
	public String toString() {
		return ((this instanceof AbsoluteOid) ? "/" : "") + Arrays.stream(path).collect(Collectors.joining("/"));
	}

	@Override
	public String[] path() {
		return path;
	}

	protected <E extends Oid> E child(BiFunction<String, String[], E> cons, String component) {
		String[] n = Arrays.copyOf(path, path.length + 1);
		n[n.length - 1] = component;
		return cons.apply(namespace, n);
	}

	protected <E extends Oid> E head(BiFunction<String, String[], E> cons, int length) {
		if (path.length < length || length <= 0)
			throw new IllegalArgumentException("Target length out of range");

		return cons.apply(namespace, Arrays.copyOf(path, length));
	}

	protected <E extends Oid> E parent(BiFunction<String, String[], E> cons) {
		if (size() == 1)
			return null;

		return (E) head(size() - 1);
	}

	protected <E extends RelativeOid> E relativize(BiFunction<String, String[], E> cons, Oid oid) {
		if (oid == null)
			return cons.apply(namespace, path.clone());

		if (!isChildOf(oid))
			throw new IllegalArgumentException("Target: " + this + " must be a child of: " + oid);

		return cons.apply(namespace, Arrays.copyOfRange(path, oid.size(), path.length));
	}

	protected <E extends Oid> E resolve(BiFunction<String, String[], E> cons, String... components) {
		if (isConcrete())
			throw new IllegalStateException("Cannot resolve a concrete OID");

		String[] p = path.clone();

		int i = 0;
		for (var component : components) {
			for (; i < p.length; i++) {
				if (p[i].isEmpty()) {
					p[i] = component;
					break;
				}
			}
		}

		var derived = (OidBase) cons.apply(namespace, components);
		derived.data = data;
		return (E) derived;
	}

	protected <E extends Oid> E tail(BiFunction<String, String[], E> cons, int offset) {
		if (path.length < offset || offset < 1)
			throw new IllegalStateException("Invalid tail offset: " + offset);

		var derived = (OidBase) cons.apply(namespace, Arrays.copyOfRange(path, offset, path.length));
		derived.data = data;
		return (E) derived;
	}
}
