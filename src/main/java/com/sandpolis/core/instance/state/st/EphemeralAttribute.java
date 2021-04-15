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

import static com.sandpolis.core.instance.State.ProtoAttributeValue.newBuilder;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.protobuf.UnsafeByteOperations;
import com.sandpolis.core.instance.State.ProtoAttributeValue;
import com.sandpolis.core.instance.State.ProtoAttributeValues;
import com.sandpolis.core.instance.State.ProtoSTObjectUpdate;
import com.sandpolis.core.instance.state.oid.Oid;

public class EphemeralAttribute extends AbstractSTObject implements STAttribute {

	public static record EphemeralAttributeValue(long timestamp, Object value) {
	}

	public static enum AttributeType {
		STRING(proto -> new EphemeralAttributeValue(proto.getTimestamp(), proto.getString()),
				value -> newBuilder().setTimestamp(value.timestamp()).setString((String) value.value()).build()),
		LONG(proto -> new EphemeralAttributeValue(proto.getTimestamp(), proto.getLong()),
				value -> newBuilder().setTimestamp(value.timestamp()).setLong((Long) value.value()).build()),
		INTEGER(proto -> new EphemeralAttributeValue(proto.getTimestamp(), proto.getInteger()),
				value -> newBuilder().setTimestamp(value.timestamp()).setInteger((Integer) value.value()).build()),
		BOOLEAN(proto -> new EphemeralAttributeValue(proto.getTimestamp(), proto.getBoolean()),
				value -> newBuilder().setTimestamp(value.timestamp()).setBoolean((boolean) value.value()).build()),
		BYTES(proto -> new EphemeralAttributeValue(proto.getTimestamp(), proto.getBytes().toByteArray()),
				value -> newBuilder().setTimestamp(value.timestamp())
						.setBytes(UnsafeByteOperations.unsafeWrap((byte[]) value.value())).build()),
		X509CERTIFICATE(proto -> new EphemeralAttributeValue(proto.getTimestamp(), proto.getBytes().toByteArray()),
				value -> newBuilder().setTimestamp(value.timestamp())
						.setBytes(UnsafeByteOperations.unsafeWrap((byte[]) value.value())).build()),
		BOOLEAN_ARRAY(proto -> new EphemeralAttributeValue(proto.getTimestamp(), proto.getBooleanArrayList()),
				value -> newBuilder().setTimestamp(value.timestamp()).build());

		public final Function<ProtoAttributeValue, EphemeralAttributeValue> unpack;

		public final Function<EphemeralAttributeValue, ProtoAttributeValue> pack;

		private AttributeType(Function<ProtoAttributeValue, EphemeralAttributeValue> unpack,
				Function<EphemeralAttributeValue, ProtoAttributeValue> pack) {
			this.unpack = unpack;
			this.pack = pack;
		}
	}

	protected AttributeType type;

	/**
	 * The current value of the attribute.
	 */
	protected EphemeralAttributeValue current;

	/**
	 * Historical values.
	 */
	protected List<EphemeralAttributeValue> history;

	/**
	 * A strategy that determines what happens to old values.
	 */
	protected RetentionPolicy retention;

	/**
	 * A quantifier for the retention policy.
	 */
	protected long retentionLimit;

	/**
	 * An optional supplier that overrides the current value.
	 */
	protected Supplier<?> source;

	public EphemeralAttribute(STDocument parent, String id) {
		super(parent, id);
	}

	@Override
	public synchronized Object get() {
		if (source != null)
			return source.get();
		if (current == null)
			return null;

		return current.value();
	}

	@Override
	public synchronized List<EphemeralAttributeValue> history() {
		if (history == null)
			return List.of();

		return Collections.unmodifiableList(history);
	}

	@Override
	public synchronized void merge(ProtoSTObjectUpdate snapshot) {

		if (snapshot.getChangedCount() != 1) {
			throw new IllegalArgumentException();
		}

		snapshot.getChangedMap().forEach((path, change) -> {
			var oid = Oid.of(path);

			if (oid.quantifier() == null) {
				// This OID refers to the current value
				var old = current;
				current = type.unpack.apply(change.getValue(0));
				fireAttributeValueChangedEvent(this, old, current);
			} else {
				// This OID refers to one or more historical values
				change.getValueList().stream().map(type.unpack::apply).forEach(history::add);
			}
		});
	}

	@Override
	public synchronized void set(Object value) {

		// Save the old value for inclusion in the event
		var old = current;

		if (value == null) {
			current = null;

			fireAttributeValueChangedEvent(this, old, null);
			return;
		}

		if (type == null) {
			// Determine type experimentally
			type = findType(value);
		} else {
			// Assert the type has not changed
			if (type != findType(value)) {
				throw new IllegalArgumentException();
			}
		}

		// If retention is not enabled, then overwrite the old value
		if (retention == null) {
			current = new EphemeralAttributeValue(System.currentTimeMillis(), value);
		}

		// Retention is enabled
		else {
			// Move current value into history
			history.add(current);

			// Set current value
			current = new EphemeralAttributeValue(System.currentTimeMillis(), value);

			// Take action on the old values if necessary
			checkRetention();
		}

		fireAttributeValueChangedEvent(this, old, current);
	}

	private AttributeType findType(Object value) {
		if (value instanceof String) {
			return AttributeType.STRING;
		}
		if (value instanceof Boolean) {
			return AttributeType.BOOLEAN;
		}
		if (value instanceof Long) {
			return AttributeType.LONG;
		}
		if (value instanceof Integer) {
			return AttributeType.INTEGER;
		}
		if (value instanceof X509Certificate) {
			return AttributeType.X509CERTIFICATE;
		}
		if (value instanceof boolean[]) {
			return AttributeType.BOOLEAN_ARRAY;
		}
		throw new IllegalArgumentException("Unknown attribute value type: " + value.getClass());
	}

	public synchronized void setRetention(RetentionPolicy retention) {
		this.retention = retention;
		checkRetention();
	}

	public synchronized void setRetention(RetentionPolicy retention, int limit) {
		this.retention = retention;
		this.retentionLimit = limit;
		checkRetention();
	}

	@Override
	public synchronized ProtoSTObjectUpdate snapshot(Oid... oid) {

		if (oid.length > 1) {
			throw new IllegalArgumentException();
		}

		if (!isPresent())
			// Empty attribute shortcut
			return ProtoSTObjectUpdate.getDefaultInstance();

		var snapshot = ProtoSTObjectUpdate.newBuilder();

		// Check the retention condition before serializing
		checkRetention();

		if (oid.length == 0) {
			// Request for current value only
			if (source != null) {
				snapshot.putChanged(oid().toString(), ProtoAttributeValues.newBuilder()
						.addValue(
								type.pack.apply(new EphemeralAttributeValue(System.currentTimeMillis(), source.get())))
						.build());
			} else {
				snapshot.putChanged(oid().toString(),
						ProtoAttributeValues.newBuilder().addValue(type.pack.apply(current)).build());
			}
		} else if (oid.length == 1) {
			// Request is for historical value
			// TODO
		}

		return snapshot.build();
	}

	@Override
	public synchronized void source(Supplier<?> source) {
		this.source = source;
	}

	/**
	 * Get the timestamp associated with the current value.
	 *
	 * @return The current timestamp
	 */
	public synchronized long timestamp() {
		if (source != null)
			return 0;
		if (current == null)
			return 0;

		return current.timestamp();
	}

	@Override
	public String toString() {
		if (current != null)
			return current.toString();
		return null;
	}

	/**
	 * Check the retention condition and remove all violating elements.
	 */
	private void checkRetention() {
		if (retention == null)
			return;

		if (history == null)
			history = new ArrayList<>();

		switch (retention) {
		case ITEM_LIMITED:
			while (history.size() > retentionLimit) {
				history.remove(0);
			}
			break;
		case TIME_LIMITED:
			while (history.size() > 0 && history.get(0).timestamp() > (current.timestamp() - retentionLimit)) {
				history.remove(0);
			}
			break;
		case UNLIMITED:
			// Do nothing
			break;
		}
	}
}
