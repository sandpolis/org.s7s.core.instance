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
	exports com.sandpolis.core.instance.config;
	exports com.sandpolis.core.instance.logging;
	exports com.sandpolis.core.instance.msg;
	exports com.sandpolis.core.instance.plugin;
	exports com.sandpolis.core.instance.pref;
	exports com.sandpolis.core.instance.profile;
	exports com.sandpolis.core.instance.state.oid;
	exports com.sandpolis.core.instance.state.st;
	exports com.sandpolis.core.instance.state.vst;
	exports com.sandpolis.core.instance.state;
	exports com.sandpolis.core.instance.store.event;
	exports com.sandpolis.core.instance.store;
	exports com.sandpolis.core.instance.thread;
	exports com.sandpolis.core.instance.util;
	exports com.sandpolis.core.instance;

	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires com.google.common;
	requires com.google.protobuf;
	requires com.sandpolis.core.foundation;
	requires java.prefs;
	requires org.slf4j;

	uses com.sandpolis.core.instance.plugin.SandpolisPlugin;

	provides ch.qos.logback.classic.spi.Configurator
			with com.sandpolis.core.instance.logging.DefaultLogbackConfigurator;
}
