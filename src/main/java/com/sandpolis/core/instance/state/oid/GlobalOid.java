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

import com.sandpolis.core.instance.state.st.STDocument;
import com.sandpolis.core.instance.state.st.STObject;

public class GlobalOid<E extends STObject<?>> implements Oid<E> {

	private final Map<OidData<?>, Object> data;

	/**
	 * The OID unique namespace.
	 */
	protected final String namespace;

	/**
	 * The OID path.
	 */
	protected final String[] path;

	protected Map<String, GlobalOid<?>> cache = new HashMap<>();

	public GlobalOid(String namespace, String[] path, Map<OidData<?>, Object> data) {
		if (path.length == 0)
			throw new IllegalArgumentException("Non-root OID cannot have an empty path");

		if (Arrays.stream(path).anyMatch(Objects::isNull))
			throw new IllegalArgumentException();

		this.namespace = namespace;
		this.path = path;
		this.data = data;

		cache.put(namespace + " " + this.toString(), this);
	}

	public GlobalOid(String namespace, String path, Map<OidData<?>, Object> data) {
		this(namespace, path.replaceAll("^/+", "").split("/"), data);
	}

	public GlobalOid(String namespace) {
		this(namespace, new String[] {}, Map.of());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GlobalOid<?>other) {
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
		return namespace.hashCode() * path.hashCode();
	}

	@Override
	public String namespace() {
		return namespace;
	}

	@Override
	public String toString() {
		return "/" + Arrays.stream(path).collect(Collectors.joining("/"));
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

	@Override
	public Oid child(String component) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Oid head(int length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Oid<E> resolve(String... components) {
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

		return new GlobalOid<>(namespace, p, data);
	}

}
