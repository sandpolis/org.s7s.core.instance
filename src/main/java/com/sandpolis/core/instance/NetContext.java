//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance;

import com.sandpolis.core.instance.Entrypoint;
import com.sandpolis.core.instance.EnvironmentVariable;
import com.sandpolis.core.instance.RuntimeVariable;
import com.sandpolis.core.instance.SystemProperty;
import com.sandpolis.core.instance.state.oid.Oid;

public final class NetContext {

	/**
	 * The default message timeout in milliseconds.
	 */
	public static final RuntimeVariable<Integer> MESSAGE_TIMEOUT = RuntimeVariable.of(cfg -> {
		cfg.type = Integer.class;
		cfg.primary = Oid.of("com.sandpolis.core.instance:/profile/" + Entrypoint.data().uuid() + "/message_timeout");
		cfg.secondary = SystemProperty.of("s7s.net.message_timeout");
		cfg.tertiary = EnvironmentVariable.of("S7S_MESSAGE_TIMEOUT");
		cfg.defaultValue = () -> 1000;
	});

	/**
	 * The maximum number of outgoing connection attempts.
	 */
	public static final RuntimeVariable<Integer> OUTGOING_CONCURRENCY = RuntimeVariable.of(cfg -> {
		cfg.type = Integer.class;
		cfg.secondary = SystemProperty.of("s7s.net.connection.max_outgoing");
		cfg.defaultValue = () -> 4;
		cfg.validator = value -> {
			return value > 0;
		};
	});

	/**
	 * Whether TLS will be used for network connections.
	 */
	public static final RuntimeVariable<Boolean> TLS_ENABLED = RuntimeVariable.of(cfg -> {
		cfg.type = Boolean.class;
		cfg.secondary = SystemProperty.of("s7s.net.connection.tls");
	});

	/**
	 * Whether decoded network traffic will be logged.
	 */
	public static final RuntimeVariable<Boolean> LOG_TRAFFIC_DECODED = RuntimeVariable.of(cfg -> {
		cfg.type = Boolean.class;
		cfg.secondary = SystemProperty.of("s7s.net.logging.decoded");
	});

	/**
	 * The traffic statistics interval.
	 */
	public static final RuntimeVariable<Integer> TRAFFIC_INTERVAL = RuntimeVariable.of(cfg -> {
		cfg.type = Integer.class;
		cfg.secondary = SystemProperty.of("s7s.net.connection.stat_interval");
	});

	/**
	 * Whether raw network traffic will be logged.
	 */
	public static final RuntimeVariable<Boolean> LOG_TRAFFIC_RAW = RuntimeVariable.of(cfg -> {
		cfg.type = Boolean.class;
		cfg.secondary = SystemProperty.of("s7s.net.logging.raw");
	});

	private NetContext() {
	}
}
