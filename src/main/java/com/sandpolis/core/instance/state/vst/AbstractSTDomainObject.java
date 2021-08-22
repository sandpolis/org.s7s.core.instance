//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.state.vst;

import com.sandpolis.core.instance.state.oid.Oid;
import com.sandpolis.core.instance.state.st.STAttribute;
import com.sandpolis.core.instance.state.st.STDocument;

public abstract class AbstractSTDomainObject implements STDomainObject {

	protected STDocument document;

	public AbstractSTDomainObject(STDocument document) {
		this.document = document;
	}

	public Oid oid() {
		return document.oid();
	}

	public String getId() {
		return oid().last();
	}

	public STAttribute get(Oid oid) {
		return document.attribute(oid);
	}

	public void set(Oid oid, Object value) {
		get(oid).set(value);
	}
}
