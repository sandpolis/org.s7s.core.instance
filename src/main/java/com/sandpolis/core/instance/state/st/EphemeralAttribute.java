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

import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.protobuf.UnsafeByteOperations;
import com.sandpolis.core.foundation.Platform.OsType;
import com.sandpolis.core.foundation.util.CertUtil;
import com.sandpolis.core.instance.Metatypes.InstanceFlavor;
import com.sandpolis.core.instance.Metatypes.InstanceType;
import com.sandpolis.core.instance.State.ProtoAttributeValue;
import com.sandpolis.core.instance.State.ProtoAttributeValues;
import com.sandpolis.core.instance.State.ProtoSTObjectUpdate;
import com.sandpolis.core.instance.state.oid.Oid;

public class EphemeralAttribute extends AbstractSTObject implements STAttribute {

	public static enum AttributeType {
		BOOLEAN( //
				proto -> new EphemeralAttributeValue(proto.getTimestamp(), //
						proto.getBoolean()), //
				value -> newBuilder().setTimestamp(value.timestamp()) //
						.setBoolean((boolean) value.value()).build()), //
		BOOLEAN_ARRAY( //
				proto -> new EphemeralAttributeValue(proto.getTimestamp(), //
						proto.getBooleanArrayList()), //
				value -> newBuilder().setTimestamp(value.timestamp()).build()), // TODO
		INT_ARRAY( //
				proto -> new EphemeralAttributeValue(proto.getTimestamp(), //
						proto.getIntegerArrayList()), //
				value -> newBuilder().setTimestamp(value.timestamp()).build()), // TODO
		BYTES( //
				proto -> new EphemeralAttributeValue(proto.getTimestamp(), //
						proto.getBytes().toByteArray()), //
				value -> newBuilder().setTimestamp(value.timestamp())//
						.setBytes(UnsafeByteOperations.unsafeWrap((byte[]) value.value())).build()), //
		INSTANCE_FLAVOR( //
				proto -> new EphemeralAttributeValue(proto.getTimestamp(), //
						InstanceFlavor.forNumber(proto.getInteger())), //
				value -> newBuilder().setTimestamp(value.timestamp()) //
						.setInteger(((InstanceFlavor) value.value()).getNumber()).build()), //
		INSTANCE_TYPE( //
				proto -> new EphemeralAttributeValue(proto.getTimestamp(), //
						InstanceType.forNumber(proto.getInstanceType())), //
				value -> newBuilder().setTimestamp(value.timestamp()) //
						.setInstanceType(((InstanceType) value.value()).getNumber()).build()), //
		INTEGER( //
				proto -> new EphemeralAttributeValue(proto.getTimestamp(), //
						proto.getInteger()),
				value -> newBuilder().setTimestamp(value.timestamp()) //
						.setInteger((Integer) value.value()).build()), //
		LONG( //
				proto -> new EphemeralAttributeValue(proto.getTimestamp(), //
						proto.getLong()), //
				value -> newBuilder().setTimestamp(value.timestamp()) //
						.setLong((Long) value.value()).build()), //
		OS_TYPE( //
				proto -> new EphemeralAttributeValue(proto.getTimestamp(), //
						OsType.forNumber(proto.getOsType())), //
				value -> newBuilder().setTimestamp(value.timestamp()) //
						.setOsType(((OsType) value.value()).getNumber()).build()), //
		STRING( //
				proto -> new EphemeralAttributeValue(proto.getTimestamp(), //
						proto.getString()), //
				value -> newBuilder().setTimestamp(value.timestamp()) //
						.setString((String) value.value()).build()), //
		X509CERTIFICATE( //
				proto -> {
					try {
						return new EphemeralAttributeValue(proto.getTimestamp(), //
								CertUtil.parseCert(proto.getBytes().toByteArray()));
					} catch (CertificateException e) {
						return null;
					}
				}, //
				value -> {
					try {
						return newBuilder().setTimestamp(value.timestamp()) //
								.setBytes(
										UnsafeByteOperations.unsafeWrap(((X509Certificate) value.value()).getEncoded()))
								.build();
					} catch (CertificateEncodingException e) {
						return null;
					}
				});

		public final Function<EphemeralAttributeValue, ProtoAttributeValue> pack;

		public final Function<ProtoAttributeValue, EphemeralAttributeValue> unpack;

		private AttributeType(Function<ProtoAttributeValue, EphemeralAttributeValue> unpack,
				Function<EphemeralAttributeValue, ProtoAttributeValue> pack) {
			this.unpack = unpack;
			this.pack = pack;
		}
	}

	public static record EphemeralAttributeValue(long timestamp, Object value) {
	}

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

	protected AttributeType type;

	public EphemeralAttribute(STDocument parent, String id) {
		super(parent, id);
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

	private AttributeType findType(ProtoAttributeValue value) {
		switch (value.getSingularTypeCase()) {
		case BOOLEAN:
			return AttributeType.BOOLEAN;
		case BYTES:
			return AttributeType.BYTES;
		case INTEGER:
			return AttributeType.INTEGER;
		case LONG:
			return AttributeType.LONG;
		case STRING:
			return AttributeType.STRING;
		case INSTANCE_TYPE:
			return AttributeType.INSTANCE_TYPE;
		case OS_TYPE:
			return AttributeType.OS_TYPE;
		default:
			throw new IllegalArgumentException("Unknown attribute value type: " + value);
		}
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
		if (value instanceof int[]) {
			return AttributeType.INT_ARRAY;
		}
		if (value instanceof byte[]) {
			return AttributeType.BYTES;
		}
		if (value instanceof InstanceType) {
			return AttributeType.INSTANCE_TYPE;
		}
		if (value instanceof InstanceFlavor) {
			return AttributeType.INSTANCE_FLAVOR;
		}
		if (value instanceof OsType) {
			return AttributeType.OS_TYPE;
		}
		throw new IllegalArgumentException("Unknown attribute value type: " + value.getClass());
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

			// Set type if necessary
			if (type == null) {
				type = findType(change.getValue(0));
			}

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
				var value = source.get();
				if (type == null) {
					// Determine type experimentally
					type = findType(value);
				}

				snapshot.putChanged(oid().toString(),
						ProtoAttributeValues.newBuilder()
								.addValue(
										type.pack.apply(new EphemeralAttributeValue(System.currentTimeMillis(), value)))
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
	 * @return The timestamp associated with the current value
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
}
