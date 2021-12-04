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
import com.sandpolis.core.instance.Metatypes.InstanceFlavor;
import com.sandpolis.core.instance.Metatypes.InstanceType;
import com.sandpolis.core.instance.state.InstanceOids.ConnectionOid;
import com.sandpolis.core.instance.Message.MSG;
import com.sandpolis.core.instance.channel.ChannelConstant;
import com.sandpolis.core.instance.sid.AbstractCvidHandler.CvidHandshakeCompletionEvent;
import com.sandpolis.core.instance.msg.MsgCvid.RQ_Cvid;
import com.sandpolis.core.instance.msg.MsgCvid.RS_Cvid;
import com.sandpolis.core.instance.session.SessionRequestHandler;
import com.sandpolis.core.instance.util.CvidUtil;
import com.sandpolis.core.instance.util.MsgUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;

@Disabled
class CvidRequestHandlerTest {

	private static final SessionRequestHandler HANDLER = new SessionRequestHandler();

	private EmbeddedChannel client;
	private CvidHandshakeCompletionEvent event;

	@BeforeEach
	void setup() {
		client = new EmbeddedChannel();
		event = null;

		client.pipeline().addFirst(new ChannelInboundHandlerAdapter() {
			@Override
			public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
				assertTrue(evt instanceof CvidHandshakeCompletionEvent);
				event = (CvidHandshakeCompletionEvent) evt;
			}
		});
		client.pipeline().addFirst(HANDLER);
	}

	@Test
	@DisplayName("Initiate a SID handshake")
	void testInitiate() throws InvalidProtocolBufferException {
		final var testUuid = randomUUID().toString();
		HANDLER.handshake(client, InstanceType.CLIENT, InstanceFlavor.GENERIC, testUuid);

		RQ_Cvid rq = MsgUtil.unpack(client.readOutbound(), RQ_Cvid.class);

		assertTrue(rq != null);
		assertEquals(InstanceType.CLIENT, rq.getInstance());
		assertEquals(InstanceFlavor.GENERIC, rq.getInstanceFlavor());
		assertEquals(testUuid, rq.getUuid());
	}

	@Test
	@DisplayName("Receive an invalid response")
	void testReceiveIncorrect() {
		client.writeInbound(MsgUtil.pack(MSG.newBuilder(), RS_Cvid.newBuilder().build()));

		await().atMost(1000, TimeUnit.MILLISECONDS).until(() -> event != null);
		assertFalse(event.success);
		assertNull(client.pipeline().get(SessionRequestHandler.class), "Handler autoremove failed");
	}

	@Test
	@DisplayName("Receive a valid response")
	void testReceiveCorrect() {
		final var testUuid = randomUUID().toString();
		final var testCvid = 123456;
		client.writeInbound(MsgUtil.pack(MSG.newBuilder(),
				RS_Cvid.newBuilder().setSid(CvidUtil.sid(InstanceType.CLIENT, InstanceFlavor.GENERIC))
						.setServerCvid(testCvid).setServerUuid(testUuid).build()));

		await().atMost(1000, TimeUnit.MILLISECONDS).until(() -> event != null);
		assertTrue(event.success);

		assertEquals(testCvid, client.attr(ChannelConstant.SOCK).get().get(ConnectionOid.REMOTE_CVID).asInt());
		assertEquals(testUuid, client.attr(ChannelConstant.SOCK).get().get(ConnectionOid.REMOTE_UUID).asInt());
		assertNull(client.pipeline().get(SessionRequestHandler.class), "Handler autoremove failed");
	}

}
