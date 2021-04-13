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

import com.sandpolis.core.instance.State.ProtoSTObjectUpdate;
import com.sandpolis.core.instance.state.oid.Oid;

/**
 * A {@link STObject} is a member of the real state tree (ST).
 *
 * <p>
 * The ST is a tree data-structure containing general collections, documents,
 * and attributes.
 *
 * @since 5.0.0
 */
public interface STObject {

	/**
	 * Register a new listener on the object. The listener will be notified of all
	 * collection, document, and attribute events from the object's descendants.
	 *
	 * @param listener The listener to register
	 * @return The listener for convenience
	 */
	public void addListener(Object listener);

	/**
	 * Incorporate the given snapshot into the object. If the snapshot is not
	 * partial, the object's state becomes identical to the snapshot. If the
	 * snapshot is partial, it has an additive effect.
	 *
	 * @param snapshot A state object snapshot
	 */
	public void merge(ProtoSTObjectUpdate snapshot);

	/**
	 * Get the object's OID.
	 *
	 * @return The associated OID or {@code null} if the object is a root node
	 */
	public Oid oid();

	/**
	 * Get this object's parent.
	 *
	 * @return The parent {@link STDocument} or {@code null}
	 */
	public STDocument parent();

	/**
	 * Deregister a previously registered listener. Any currently queued events will
	 * still be delivered.
	 *
	 * @param listener The listener to deregister
	 */
	public void removeListener(Object listener);

	/**
	 * Extract the object's state into a new snapshot. If whitelist OIDs are
	 * specified, the snapshot will be "partial" and therefore only contain
	 * descendants that are also children of at least one of the OIDs specified in
	 * the whitelist.
	 *
	 * @param oids Whitelisted OIDs
	 * @return A new protocol buffer representing the object
	 */
	public ProtoSTObjectUpdate snapshot(Oid... oids);
}
