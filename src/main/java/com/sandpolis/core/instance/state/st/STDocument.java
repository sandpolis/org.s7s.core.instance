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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.sandpolis.core.instance.State.ProtoSTObjectUpdate;
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
	 * @param id The ID of the child to retrieve or create
	 * @return A child attribute with the given ID
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
		for (int i = oid().path().length; i < path.length - 1; i++) {
			document = document.document(path[i]);
		}

		return document.attribute(path[path.length - 1]);
	}

	/**
	 * @return The number of sub-attributes belonging to this document
	 */
	public int attributeCount();

	/*
	 * public default void copyFrom(STDocument other) {
	 * other.forEachDocument(document -> {
	 * this.document(document.oid().last()).copyFrom(document); });
	 * other.forEachAttribute(attribute -> {
	 * this.attribute(attribute.oid().last()).set(attribute.get()); }); }
	 */

	public default STDocument document(Oid oid) {
		return document(oid.path());
	}

	public STDocument document(String id);

	public STDocument getDocument(String id);

	public STAttribute getAttribute(String id);

	public default STDocument document(String[] path) {
		if (path.length == 0) {
			throw new IllegalArgumentException();
		}

		STDocument document = this;
		for (int i = oid().path().length; i < path.length; i++) {
			document = document.document(path[i]);
		}

		return document;
	}

	/**
	 * @return The number of sub-documents belonging to this document
	 */
	public int documentCount();

	/**
	 * Perform the given action on all {@link STAttribute} members.
	 *
	 * @param consumer The action
	 */
	public void forEachAttribute(Consumer<STAttribute> consumer);

	/**
	 * Perform the given action on all {@link STDocument} members.
	 *
	 * @param consumer The action
	 */
	public void forEachDocument(Consumer<STDocument> consumer);

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

	@Override
	public default void merge(ProtoSTObjectUpdate snapshot) {

		var oid = oid();

		// Handle removals
		snapshot.getRemovedList().stream().map(Oid::of).forEach(removal -> {
			if (oid.path().length - removal.path().length == 1) {
				if (removal.quantifier() != null) {
					remove(removal.last());
				} else {
					// TODO
				}
			} else {
				// TODO
			}
		});

		snapshot.getChangedMap().forEach((path, change) -> {

			var change_oid = Oid.of(path);

			if (oid.isAncestorOf(change_oid)) {

				// TODO!
			}

		});
	}

	@Override
	public default ProtoSTObjectUpdate snapshot(Oid... oids) {

		var snapshot = ProtoSTObjectUpdate.newBuilder();

		if (oids.length == 0) {
			forEachDocument(document -> {
				snapshot.mergeFrom(document.snapshot());
			});
			forEachAttribute(attribute -> {
				snapshot.mergeFrom(attribute.snapshot());
			});
		} else {
			for (var head : Arrays.stream(oids).map(Oid::first).distinct().toArray(String[]::new)) {
				var children = Arrays.stream(oids).filter(oid -> oid.first() != head).toArray(Oid[]::new);

				var document = getDocument(head);
				if (document != null) {
					snapshot.mergeFrom(document.snapshot(children));
				}

				var attribute = getAttribute(head);
				if (attribute != null) {
					snapshot.mergeFrom(attribute.snapshot());
				}
			}
		}

		return snapshot.build();
	}
}
