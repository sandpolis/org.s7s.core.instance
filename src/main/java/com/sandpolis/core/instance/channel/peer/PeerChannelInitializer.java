//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.channel.peer;

import static com.sandpolis.core.instance.thread.ThreadStore.ThreadStore;
import static com.sandpolis.core.instance.channel.HandlerKey.EXELET;
import static com.sandpolis.core.instance.channel.HandlerKey.FRAME_DECODER;
import static com.sandpolis.core.instance.channel.HandlerKey.FRAME_ENCODER;
import static com.sandpolis.core.instance.channel.HandlerKey.LOG_DECODED;
import static com.sandpolis.core.instance.channel.HandlerKey.LOG_RAW;
import static com.sandpolis.core.instance.channel.HandlerKey.MANAGEMENT;
import static com.sandpolis.core.instance.channel.HandlerKey.PROTO_DECODER;
import static com.sandpolis.core.instance.channel.HandlerKey.PROTO_ENCODER;
import static com.sandpolis.core.instance.channel.HandlerKey.RESPONSE;
import static com.sandpolis.core.instance.channel.HandlerKey.TRAFFIC;
import static com.sandpolis.core.instance.connection.ConnectionStore.ConnectionStore;

import java.util.function.Consumer;

import com.sandpolis.core.instance.NetContext;
import com.sandpolis.core.instance.Message.MSG;
import com.sandpolis.core.instance.channel.ChannelConstant;
import com.sandpolis.core.instance.channel.ChannelStruct;
import com.sandpolis.core.instance.channel.HandlerKey;
import com.sandpolis.core.instance.connection.Connection;
import com.sandpolis.core.instance.exelet.ExeletHandler;
import com.sandpolis.core.instance.handler.ManagementHandler;
import com.sandpolis.core.instance.handler.ResponseHandler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;

/**
 * {@link PeerChannelInitializer} configures a {@link Channel} for peer-to-peer
 * connections. Typically this initializer will be used with datagram channels.
 * The {@link Channel} will automatically perform a NAT traversal prior
 * completion of the handshake if required.
 *
 * @since 5.0.0
 */
public class PeerChannelInitializer extends ChannelInitializer<Channel> {

	public static final HandlerKey<HolePunchHandler> HOLEPUNCH = new HandlerKey<>("HolePunchHandler");
	public static final HandlerKey<PeerEncryptionEncoder> ENCRYPTION_ENCODER = new HandlerKey<>("EncryptionEncoder");
	public static final HandlerKey<PeerEncryptionDecoder> ENCRYPTION_DECODER = new HandlerKey<>("EncryptionDecoder");

	private static final ManagementHandler HANDLER_MANAGEMENT = new ManagementHandler();
	private static final ProtobufDecoder HANDLER_PROTO_DECODER = new ProtobufDecoder(MSG.getDefaultInstance());
	private static final ProtobufEncoder HANDLER_PROTO_ENCODER = new ProtobufEncoder();
	private static final ProtobufVarint32LengthFieldPrepender HANDLER_PROTO_FRAME_ENCODER = new ProtobufVarint32LengthFieldPrepender();

	public PeerChannelInitializer(Consumer<ChannelStruct> configurator) {
		var config = new ChannelStruct(configurator);

	}

	@Override
	protected void initChannel(Channel ch) throws Exception {
		var connection = ConnectionStore.create(ch);
		ch.attr(ChannelConstant.HANDSHAKE_FUTURE).set(ch.eventLoop().newPromise());

		ChannelPipeline p = ch.pipeline();

		if (ch instanceof DatagramChannel)
			p.addLast(HOLEPUNCH.next(p), new HolePunchHandler());

		p.addLast(TRAFFIC.next(p), new ChannelTrafficShapingHandler(NetContext.TRAFFIC_INTERVAL.get()));

		p.addLast(ENCRYPTION_ENCODER.next(p), new PeerEncryptionEncoder());
		p.addLast(ENCRYPTION_DECODER.next(p), new PeerEncryptionDecoder());

		if (NetContext.LOG_TRAFFIC_RAW.get())
			p.addLast(LOG_RAW.next(p), new LoggingHandler(Connection.class));

		p.addLast(FRAME_DECODER.next(p), new ProtobufVarint32FrameDecoder());
		p.addLast(PROTO_DECODER.next(p), HANDLER_PROTO_DECODER);
		p.addLast(FRAME_ENCODER.next(p), HANDLER_PROTO_FRAME_ENCODER);
		p.addLast(PROTO_ENCODER.next(p), HANDLER_PROTO_ENCODER);

		if (NetContext.LOG_TRAFFIC_DECODED.get())
			p.addLast(LOG_DECODED.next(p), new LoggingHandler(Connection.class));

		p.addLast(ThreadStore.get("net.exelet"), RESPONSE.next(p), new ResponseHandler());

		p.addLast(ThreadStore.get("net.exelet"), EXELET.next(p), new ExeletHandler(connection));

		p.addLast(MANAGEMENT.next(p), HANDLER_MANAGEMENT);
	}
}
