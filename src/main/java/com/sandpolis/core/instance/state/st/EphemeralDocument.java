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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.sandpolis.core.instance.State.ProtoSTObjectUpdate;
import com.sandpolis.core.instance.state.oid.Oid;

public class EphemeralDocument extends AbstractSTObject implements STDocument {

	protected final Map<String, STAttribute> attributes;

	protected final Map<String, STDocument> documents;

	public EphemeralDocument(STDocument parent, String id) {
		super(parent, id);
		this.attributes = new HashMap<>();
		this.documents = new HashMap<>();
	}

	@Override
	public int attributeCount() {
		return attributes.size();
	}

	@Override
	public Collection<STAttribute> attributes() {
		return Collections.unmodifiableCollection(attributes.values());
	}

	@Override
	public STDocument document(String id) {
		synchronized (documents) {
			STDocument document = documents.get(id);
			if (document == null) {
				document = new EphemeralDocument(this, id);
				documents.put(id, document);
				fireDocumentAddedEvent(this, document);
			}
			return document;
		}
	}

	@Override
	public STAttribute attribute(String id) {
		synchronized (attributes) {
			STAttribute attribute = attributes.get(id);
			if (attribute == null) {
				attribute = new EphemeralAttribute(this, id);
				attributes.put(id, attribute);
			}
			return attribute;
		}
	}

	@Override
	public int documentCount() {
		return documents.size();
	}

	@Override
	public Collection<STDocument> documents() {
		return Collections.unmodifiableCollection(documents.values());
	}

	@Override
	public void forEachAttribute(Consumer<STAttribute> consumer) {
		attributes.values().forEach(consumer);
	}

	@Override
	public void forEachDocument(Consumer<STDocument> consumer) {
		documents.values().forEach(consumer);
	}

	@Override
	public void merge(ProtoSTObjectUpdate snapshot) {

		// Handle removals
		snapshot.getRemovedList().stream().map(Oid::of).forEach(removal -> {
			if (oid().path().length - removal.path().length == 1) {
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
			attribute(Oid.of(path).path()).merge(ProtoSTObjectUpdate.newBuilder().putChanged(path, change).build());
		});
	}

	@Override
	public void remove(STAttribute attribute) {
		synchronized (attributes) {
			if (attributes.values().remove(attribute)) {
//				fireAttributeRemovedEvent(this, attribute);
			}
		}
	}

	@Override
	public void remove(STDocument document) {
		synchronized (documents) {
			if (documents.values().remove(document)) {
				fireDocumentRemovedEvent(this, document);
			}
		}
	}

	@Override
	public void remove(String id) {
		synchronized (documents) {
			if (documents.remove(id) != null) {
				return;
			}
		}
		synchronized (attributes) {
			if (attributes.remove(id) != null) {
				return;
			}
		}
	}

	@Override
	public ProtoSTObjectUpdate snapshot(Oid... oids) {

		var snapshot = ProtoSTObjectUpdate.newBuilder();

		if (oids.length == 0) {
			synchronized (documents) {
				documents.values().stream().map(STDocument::snapshot).forEach(snapshot::mergeFrom);
			}
			synchronized (attributes) {
				attributes.values().stream().map(STAttribute::snapshot).forEach(snapshot::mergeFrom);
			}
		} else {
			for (var head : Arrays.stream(oids).map(Oid::first).distinct().toArray()) {
				var children = Arrays.stream(oids).filter(oid -> oid.first() != head).toArray(Oid[]::new);

				if (documents.containsKey(head))
					snapshot.mergeFrom(documents.get(head).snapshot(children));
				if (attributes.containsKey(head))
					snapshot.mergeFrom(attributes.get(head).snapshot());
			}
		}

		return snapshot.build();
	}

	@Override
	public void set(String id, STAttribute attribute) {
		var previous = attributes.put(id, attribute);
	}

	@Override
	public void set(String id, STDocument document) {
		var previous = documents.put(id, document);
	}
}
