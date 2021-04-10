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
import java.util.Iterator;

import com.sandpolis.core.instance.state.st.STDocument;
import com.sandpolis.core.instance.state.st.STObject;

/**
 * <p>
 * An {@link Oid} corresponds to one or more objects in a real or virtual state
 * tree. OIDs locate objects in the state tree with an immutable sequence of
 * strings called the "path".
 *
 * <p>
 * OIDs also contain mutable data which are accessible via {@link OidData}.
 *
 * <p>
 * When represented as a String, OID components are joined with "." which is
 * known as dotted-notation.
 *
 * <h3>Concrete/Generic</h3>
 * <p>
 * An OID is either "concrete", meaning that it corresponds to exactly one
 * virtual object, or "generic" which means the OID corresponds to multiple
 * objects of the same type.
 */
public interface Oid<E extends STObject<?>> extends Comparable<Oid>, Iterable<String> {
	
	public static Oid<?> of(String path) {
		return of("", path);
	}
	
	public static Oid<?> of(String namespace, String path) {
		return null;
	}

	/**
	 * Extend the OID path by adding the given component to the end. The namespace
	 * is preserved by this operation.
	 *
	 * @param component The new component
	 * @return A new OID
	 */
	public Oid child(String component);

	@Override
	public default int compareTo(Oid oid) {
		return Arrays.compare(path(), oid.path());
	}

	/**
	 * Get the first (leftmost) component of the OID path.
	 *
	 * @return The path's first component
	 */
	public default String first() {
		return path()[0];
	}

	/**
	 * Get auxiliary data attached to this OID.
	 *
	 * @param <T>      The type of the attached data
	 * @param dataType
	 * @return The attached data
	 */
	public <T> T getData(OidData<T> dataType);

	/**
	 * Get auxiliary data attached to this OID.
	 *
	 * @param <T>         The type of the attached data
	 * @param dataType
	 * @param defaultItem A default item
	 * @return The attached data
	 */
	public default <T> T getData(OidData<T> dataType, T defaultItem) {
		T item = getData(dataType);
		return item == null ? defaultItem : item;
	}

	/**
	 * Truncate the OID path by removing components from the end. The namespace is
	 * preserved by this operation.
	 *
	 * @param length The length of the new OID
	 * @return A new OID
	 */
	public Oid head(int length);

	/**
	 * Determine whether this OID is a descendant of the given OID.
	 *
	 * @param oid The ancestor OID
	 * @return Whether this OID is a descendant
	 */
	public default boolean isChildOf(Oid oid) {
		if (oid == null)
			return true;

		return Arrays.mismatch(path(), oid.path()) == Math.min(size(), oid.size());
	}

	/**
	 * Determine whether the OID corresponds to exactly one entity (concrete) or
	 * multiple entities (generic). The OID is generic if it contains at least one
	 * empty component.
	 *
	 * @return Whether the OID is concrete
	 */
	public default boolean isConcrete() {
		return !Arrays.stream(path()).anyMatch(String::isEmpty);
	}

	@Override
	public default Iterator<String> iterator() {
		return Arrays.stream(path()).iterator();
	}

	/**
	 * Get the last (rightmost) component of the OID path.
	 *
	 * @return The OID's last component
	 */
	public default String last() {
		return path()[size() - 1];
	}

	public default String[] relativize(Oid<?> oid) {
		return Arrays.copyOfRange(path(), oid.size(), size());
	}

	/**
	 * Return a new OID with its generic components replaced (from left to right)
	 * with the given components. The resulting OID will be concrete if the number
	 * of supplied components equals the number of generic components in the OID
	 * (and none of the supplied components are empty).
	 *
	 * @param components The new components
	 * @return A new OID
	 */
	public Oid<E> resolve(String... components);

	/**
	 * Get the number of components in the OID.
	 *
	 * @return The OID's length
	 */
	public default int size() {
		return path().length;
	}

	/**
	 * Get the OID's path.
	 *
	 * @return The path
	 */
	public String[] path();

	/**
	 * Get the OID's unique namespace.
	 *
	 * @return The namespace
	 */
	public String namespace();
}
