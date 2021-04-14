//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.state.st;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.sandpolis.core.instance.state.oid.Oid;

/**
 * {@link STDocument} is a composite entity that may contain attributes and
 * sub-documents.
 *
 * @since 5.1.1
 */
public interface STDocument extends STObject {

	/**
	 * Indicates that an {@link STDocument} has been added to the document.
	 */
	public static final record DocumentAddedEvent(STDocument document, STDocument newDocument) {
	}

	/**
	 * Indicates that an {@link STDocument} has been removed from the document.
	 */
	public static final record DocumentRemovedEvent(STDocument document, STDocument oldDocument) {
	}

	/**
	 * Retrieve or create an attribute at the given OID. Any intermediate documents
	 * will be created if necessary.
	 * 
	 * @param oid An OID which must be a descendant of this document's OID
	 * @return A new or old attribute
	 */
	public default STAttribute attribute(Oid oid) {
		return attribute(oid.path());
	}

	/**
	 * Retrieve or create a child attribute with the given ID.
	 * 
	 * @param id The child ID
	 * @return A new or old attribute
	 */
	public STAttribute attribute(String id);

	/**
	 * Retrieve or create an attribute at the given OID. Any intermediate documents
	 * will be created if necessary.
	 * 
	 * @param path An OID path which must be a descendant of this document's OID
	 * @return A new or old attribute
	 */
	public default STAttribute attribute(String[] path) {

		if (path.length == 0) {
			throw new IllegalArgumentException("Empty OID path");
		}
		if (!oid().isAncestorOf(path)) {
			throw new IllegalArgumentException("/" + Arrays.stream(path).collect(Collectors.joining("/"))
					+ " is not a descendant of: " + oid().toString());
		}
		if ((path.length - oid().path().length) == 1) {
			return attribute(path[path.length - 1]);
		}

		STDocument document = this;
		for (int i = oid().path().length - 1; i < path.length - 1; i++) {
			document = document(path[i]);
		}

		return document.attribute(path[path.length - 1]);
	}

	/**
	 * @return The number of sub-attributes belonging to this document
	 */
	public int attributeCount();

	/**
	 * @return A collection of all sub-attributes
	 */
	public Collection<STAttribute> attributes();

	public default void copyFrom(STDocument other) {
		other.forEachDocument(document -> {
			this.document(document.oid().last()).copyFrom(document);
		});
		other.forEachAttribute(attribute -> {
			this.attribute(attribute.oid().last()).set(attribute.get());
		});
	}

	public default STDocument document(Oid oid) {
		return document(oid.path());
	}

	public STDocument document(String id);

	public default STDocument document(String[] path) {
		if (path.length == 0) {
			throw new IllegalArgumentException();
		}

		STDocument document = this;
		for (int i = 0; i < path.length - 1; i++) {
			document = document(path[i]);
		}

		return document.document(path[path.length - 1]);
	}

	/**
	 * @return The number of sub-documents belonging to this document
	 */
	public int documentCount();

	/**
	 * @return An immutable collection of all {@link STDocument} members
	 */
	public Collection<STDocument> documents();

	/**
	 * Perform the given action on all {@link STAttribute} members.
	 *
	 * @param consumer The action
	 */
	public default void forEachAttribute(Consumer<STAttribute> consumer) {
		attributes().forEach(consumer);
	}

	/**
	 * Perform the given action on all {@link STDocument} members.
	 *
	 * @param consumer The action
	 */
	public default void forEachDocument(Consumer<STDocument> consumer) {
		documents().forEach(consumer);
	}

	/**
	 * Remove the given {@link STAttribute} member.
	 *
	 * @param attribute The attribute to remove
	 */
	public void remove(STAttribute attribute);

	/**
	 * Remove the given {@link STDocument} member.
	 *
	 * @param document The document to remove
	 */
	public void remove(STDocument document);

	/**
	 * Remove the given {@link STDocument} or {@link STAttribute} member by id.
	 *
	 * @param id The item to remove
	 */
	public void remove(String id);

	public void set(String id, STAttribute attribute);

	public void set(String id, STDocument document);
}
