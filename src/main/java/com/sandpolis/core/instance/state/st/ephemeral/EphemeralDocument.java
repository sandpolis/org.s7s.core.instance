//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.state.st.ephemeral;

import java.util.HashMap;

import com.sandpolis.core.instance.State.ProtoDocument;
import com.sandpolis.core.instance.state.oid.GlobalOid;
import com.sandpolis.core.instance.state.oid.Oid;
import com.sandpolis.core.instance.state.st.AbstractSTDocument;
import com.sandpolis.core.instance.state.st.STDocument;

/**
 * {@link EphemeralDocument} is a memory-only implementation of
 * {@link STDocument}.
 *
 * @since 5.1.1
 */
public class EphemeralDocument extends AbstractSTDocument implements STDocument {

	public EphemeralDocument(STDocument parent, Oid<STDocument> oid) {
		super(parent, oid);

		documents = new HashMap<>();
		attributes = new HashMap<>();
	}

	public EphemeralDocument(STDocument parent, Oid<STDocument> oid, ProtoDocument document) {
		this(parent, oid);
		merge(document);
	}

	public EphemeralDocument() {
		this(null, new GlobalOid<>(null));
	}

	public EphemeralDocument(ProtoDocument document) {
		this();
		merge(document);
	}
}
