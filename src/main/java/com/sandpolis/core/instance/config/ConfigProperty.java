//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.config;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ConfigProperty} reads values from system properties, environment
 * variables, and the instance config JSON in that order.
 *
 * Note: environment variables are expected to be uppercase and separated with
 * underscores rather than dots. Therefore the equivalent environment variable
 * for "install.path" is "INSTALL_PATH".
 *
 * @param <T> The value type
 */
public record ConfigProperty<T> (String name, Optional<T> value) {

	private static final Logger log = LoggerFactory.getLogger(ConfigProperty.class);

	public static <T> ConfigProperty<T> evaluate(Class<T> type, String name) {
		return evaluate(type, name, (T) null);
	}

	public static <T> ConfigProperty<T> evaluate(Class<T> type, String name, T defaultValue) {
		return evaluate(type, name, (T) null, t -> true);
	}

	public static <T> ConfigProperty<T> evaluate(Class<T> type, String name, T defaultValue, Predicate<T> validator) {

		String value;

		// First priority: system properties
		value = System.getProperty(name);
		if (value != null) {
			log.trace("Found system property: {}", name);
			return new ConfigProperty<T>(name, parseValue(type, name, value, validator));
		}

		// Second priority: environment variables
		value = System.getenv().get(name.toUpperCase().replace('.', '_'));
		if (value != null) {
			log.trace("Found environment variable: {}", name.toUpperCase().replace('.', '_'));
			return new ConfigProperty<T>(name, parseValue(type, name, value, validator));
		}

		return new ConfigProperty<T>(name, Optional.ofNullable(defaultValue));
	}

	private static <T> Optional<T> parseValue(Class<T> type, String name, String value, Predicate<T> validator) {
		Objects.requireNonNull(value);

		T v;

		try {
			if (type == String.class) {
				v = (T) (String) value;
			}

			else if (type == String[].class) {
				v = (T) (String[]) value.split(",");
			}

			else if (type == Integer.class) {
				v = (T) (Integer) Integer.parseInt(value);
			}

			else if (type == Boolean.class) {
				v = (T) (Boolean) Boolean.parseBoolean(value);
			} else {
				log.error("Unknown type: {}", type);
				return Optional.empty();
			}
		} catch (Exception e) {
			log.error("Failed to parse property: {}", name);
			return Optional.empty();
		}

		// Validate the value last
		if (validator.test(v)) {
			return Optional.of(v);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Ensure that the property has a value. Otherwise exit.
	 */
	public void require() {
		if (value.isEmpty())
			throw new RuntimeException("Missing property: " + name);
	}
}
