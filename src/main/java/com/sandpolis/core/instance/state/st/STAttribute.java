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

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.function.Supplier;

import com.sandpolis.core.instance.Metatypes;
import com.sandpolis.core.instance.Metatypes.InstanceType;
import com.sandpolis.core.instance.state.st.EphemeralAttribute.EphemeralAttributeValue;

/**
 * {@link STAttribute} is a generic container for data of a specific type and
 * meaning.
 *
 * @param <T> The type of the attribute's value
 * @since 6.2.0
 */
public interface STAttribute extends STObject {

	/**
	 * Indicates that an {@link STAttribute}'s value has changed.
	 */
	public static final record ChangeEvent(STAttribute attribute, EphemeralAttributeValue newValue,
			EphemeralAttributeValue oldValue) {
	}

	public enum RetentionPolicy {

		/**
		 * Indicates that a fixed number of changes to the attribute will be retained.
		 */
		ITEM_LIMITED,

		/**
		 * Indicates that changes to the attribute will be retained for a fixed period
		 * of time.
		 */
		TIME_LIMITED,

		/**
		 * Indicates that changes to the attribute will be retained forever.
		 */
		UNLIMITED;
	}

	/**
	 * Get the history of the attribute's value if enabled by the
	 * {@link RetentionPolicy}.
	 *
	 * @return An unmodifiable list
	 */
	public List<EphemeralAttributeValue> history();

	public Object get();

	/**
	 * Get whether the attribute has a current value.
	 *
	 * @return Whether the attribute's value is {@code null}
	 */
	public default boolean isPresent() {
		return get() != null;
	}

	/**
	 * Set the current value of the attribute.
	 *
	 * @param value The new value to replace the current value or {@code null}
	 */
	public void set(Object value);

	/**
	 * Specify a source for the attribute's value. Setting an attribute source
	 * "binds" the attribute and will cause {@link #set(Object)} calls to fail.
	 *
	 * @param source The source or {@code null} to remove the previous source
	 */
	public void source(Supplier<?> source);

	/**
	 * Get the timestamp associated with the attribute's current value.
	 *
	 * @return The current timestamp
	 */
	public long timestamp();

	public default String asString() {
		return "";
	}

	public default long asLong() {
		return 0;
	}

	public default int asInt() {
		return 0;
	}

	public default boolean asBoolean() {
		return false;
	}

	public default byte[] asBytes() {
		return null;
	}

	public default InstanceType asInstanceType() {
		return Metatypes.InstanceType.forNumber(asInt());
	}

	public default X509Certificate asX590Certificate() {
		return null;
	}
}
