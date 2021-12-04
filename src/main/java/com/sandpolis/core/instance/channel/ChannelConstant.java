//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.channel;

import com.sandpolis.core.instance.Metatypes.InstanceType;
import com.sandpolis.core.instance.connection.Connection;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Promise;

/**
 * {@link AttributeKey} constants that are useful to {@link Connection} and
 * other classes that use {@link Channel}s.
 *
 * @author cilki
 * @since 5.0.0
 */
public final class ChannelConstant {

	public static final AttributeKey<Boolean> AUTH_STATE = AttributeKey.valueOf("state.auth");

	/**
	 * The remote host's SSL certificate status.
	 */
	public static final AttributeKey<Boolean> CERTIFICATE_STATE = AttributeKey.valueOf("state.certificate");

	/**
	 * A future that is notified when the handshake completes.
	 */
	public static final AttributeKey<Promise<Void>> HANDSHAKE_FUTURE = AttributeKey.valueOf("handshake_future");

	/**
	 * The {@link Connection} associated with the {@link Channel}.
	 */
	public static final AttributeKey<Connection> SOCK = AttributeKey.valueOf("sock");

	private ChannelConstant() {
	}
}
