//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.exelet;

import static com.sandpolis.core.instance.exelet.ExeletStore.ExeletStore;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sandpolis.core.instance.state.InstanceOids.ConnectionOid;
import com.sandpolis.core.instance.Message.MSG;
import com.sandpolis.core.instance.connection.Connection;
import com.sandpolis.core.instance.session.AbstractSessionHandler.SessionHandshakeCompletionEvent;
import com.sandpolis.core.instance.util.S7SSessionID;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * A handler that distributes incoming messages to the appropriate
 * {@link Exelet} handler.
 *
 * <p>
 * This handler maintains a separate dispatch vector for each loaded plugin.
 */
public final class ExeletHandler extends SimpleChannelInboundHandler<MSG> {

	private static final Logger log = LoggerFactory.getLogger(ExeletHandler.class);

	final Connection sock;

	Map<Integer, ExeletMethod> handlers;

	public ExeletHandler(Connection sock) {
		this.sock = sock;
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof SessionHandshakeCompletionEvent event) {

			if (event.success) {
				switch (S7SSessionID.of(event.remote).instanceType()) {
				case AGENT:
					handlers = ExeletStore.agent;
					break;
				case SERVER:
					handlers = ExeletStore.server;
					break;
				case CLIENT:
					handlers = ExeletStore.client;
					break;
				default:
					throw new RuntimeException("Cannot create ExeletHandler with remote instance: "
							+ sock.get(ConnectionOid.REMOTE_INSTANCE).asString());
				}
			}
		}

		ctx.fireUserEventTriggered(evt);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MSG msg) throws Exception {

		var handler = handlers.get(msg.getPayloadType());
		if (handler != null) {
			log.debug("Handling message with exelet: {}", handler.name);
			handler.accept(new ExeletContext(sock, msg));
		} else {
			// There's no valid handler
			ctx.fireChannelRead(msg);
		}
	}
}
