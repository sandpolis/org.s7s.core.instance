//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
open module com.sandpolis.core.instance {
	exports com.sandpolis.core.instance.channel.client;
	exports com.sandpolis.core.instance.channel.peer;
	exports com.sandpolis.core.instance.channel;
	exports com.sandpolis.core.instance.cmdlet;
	exports com.sandpolis.core.instance.connection;
	exports com.sandpolis.core.instance.exelet;
	exports com.sandpolis.core.instance.handler;
	exports com.sandpolis.core.instance.init;
	exports com.sandpolis.core.instance.logging;
	exports com.sandpolis.core.instance.message;
	exports com.sandpolis.core.instance.network;
	exports com.sandpolis.core.instance.plugin;
	exports com.sandpolis.core.instance.pref;
	exports com.sandpolis.core.instance.profile;
	exports com.sandpolis.core.instance.session;
	exports com.sandpolis.core.instance.state.oid;
	exports com.sandpolis.core.instance.state.st.entangled;
	exports com.sandpolis.core.instance.state.st;
	exports com.sandpolis.core.instance.state.vst;
	exports com.sandpolis.core.instance.state;
	exports com.sandpolis.core.instance.store;
	exports com.sandpolis.core.instance.stream;
	exports com.sandpolis.core.instance.thread;
	exports com.sandpolis.core.instance.util;
	exports com.sandpolis.core.instance;

	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires com.fasterxml.jackson.databind;
	requires com.google.common;
	requires com.google.protobuf;
	requires com.sandpolis.core.foundation;
	requires io.netty.buffer;
	requires io.netty.codec.dns;
	requires io.netty.codec;
	requires io.netty.common;
	requires io.netty.handler;
	requires io.netty.resolver.dns;
	requires io.netty.transport;
	requires java.prefs;
	requires org.slf4j;

	uses com.sandpolis.core.instance.plugin.SandpolisPlugin;

	provides ch.qos.logback.classic.spi.Configurator
			with com.sandpolis.core.instance.logging.DefaultLogbackConfigurator;
}
