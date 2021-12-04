//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.sid;

import static java.util.UUID.randomUUID;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.InvalidProtocolBufferException;
import com.sandpolis.core.instance.Metatypes.InstanceType;
import com.sandpolis.core.instance.state.InstanceOids.ConnectionOid;
import com.sandpolis.core.instance.Message.MSG;
import com.sandpolis.core.instance.channel.ChannelConstant;
import com.sandpolis.core.instance.sid.AbstractCvidHandler.CvidHandshakeCompletionEvent;
import com.sandpolis.core.instance.msg.MsgCvid.RQ_Cvid;
import com.sandpolis.core.instance.msg.MsgCvid.RS_Cvid;
import com.sandpolis.core.instance.session.SessionResponseHandler;
import com.sandpolis.core.instance.util.CvidUtil;
import com.sandpolis.core.instance.util.MsgUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;

@Disabled
class CvidResponseHandlerTest {

	private static final SessionResponseHandler HANDLER = new SessionResponseHandler();

	private EmbeddedChannel server;
	private CvidHandshakeCompletionEvent event;

	@BeforeEach
	void setup() {
		server = new EmbeddedChannel();
		event = null;

		server.pipeline().addFirst(new ChannelInboundHandlerAdapter() {
			@Override
			public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
				assertTrue(evt instanceof CvidHandshakeCompletionEvent);
				event = (CvidHandshakeCompletionEvent) evt;
			}
		});
		server.pipeline().addFirst(HANDLER);
	}

	@Test
	@DisplayName("Receive an invalid response")
	void testReceiveIncorrect() {
		final var testUuid = randomUUID().toString();

		server.writeInbound(MsgUtil.pack(MSG.newBuilder(),
				RQ_Cvid.newBuilder().setInstance(InstanceType.SERVER).setUuid(testUuid).build()));

		await().atMost(1000, TimeUnit.MILLISECONDS).until(() -> event != null);
		assertFalse(event.success);
		assertNull(server.pipeline().get(SessionResponseHandler.class), "Handler autoremove failed");
	}

	@Test
	@DisplayName("Receive a valid response")
	void testReceiveCorrect() throws InvalidProtocolBufferException {
		final var testUuid = randomUUID().toString();

		server.writeInbound(MsgUtil.pack(MSG.newBuilder(),
				RQ_Cvid.newBuilder().setInstance(InstanceType.CLIENT).setUuid(testUuid).build()));

		await().atMost(1000, TimeUnit.MILLISECONDS).until(() -> event != null);
		assertTrue(event.success);

		assertEquals(InstanceType.CLIENT, CvidUtil
				.extractInstance(server.attr(ChannelConstant.SOCK).get().get(ConnectionOid.REMOTE_CVID).asInt()));
		assertEquals(testUuid, server.attr(ChannelConstant.SOCK).get().get(ConnectionOid.REMOTE_UUID).asString());
		assertNull(server.pipeline().get(SessionResponseHandler.class), "Handler autoremove failed");

		MSG msg = server.readOutbound();
		RS_Cvid rs = MsgUtil.unpack(server.readOutbound(), RS_Cvid.class);

		assertEquals(InstanceType.CLIENT, CvidUtil.extractInstance(rs.getCvid()));
		assertFalse(rs.getServerUuid().isEmpty());

	}

}
