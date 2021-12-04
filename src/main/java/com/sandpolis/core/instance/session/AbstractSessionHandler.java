//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.session;

import com.sandpolis.core.instance.Message.MSG;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
public abstract class AbstractSessionHandler extends SimpleChannelInboundHandler<MSG> {

	/**
	 * This event is emitted by both {@link SessionRequestHandler} and
	 * {@link SessionResponseHandler} on the completion of the SID handshake.
	 */
	public static final class SessionHandshakeCompletionEvent {

		/**
		 * Whether the handshake was successful.
		 */
		public final boolean success;

		/**
		 * The local SID if the handshake was successful.
		 */
		public final int local;

		/**
		 * The remote SID if the handshake was successful.
		 */
		public final int remote;

		public SessionHandshakeCompletionEvent(int local, int remote) {
			this.success = true;
			this.local = local;
			this.remote = remote;
		}

		public SessionHandshakeCompletionEvent() {
			this.success = false;
			this.local = 0;
			this.remote = 0;
		}
	}
}
