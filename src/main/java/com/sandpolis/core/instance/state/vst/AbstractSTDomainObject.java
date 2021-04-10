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

	public Oid<STDocument> oid() {
		return document.oid();
	}

//	public abstract String getId();
//
//	public abstract STAttribute<String> id();

	public <T> STAttribute<T> attribute(Oid<STAttribute<T>> oid) {
		return document.attribute(oid);
	}

	public <T> T get(Oid<STAttribute<T>> oid) {
		return attribute(oid).get();
	}

	public <T> void set(Oid<STAttribute<T>> oid, T value) {
		attribute(oid).set(value);
	}
}
