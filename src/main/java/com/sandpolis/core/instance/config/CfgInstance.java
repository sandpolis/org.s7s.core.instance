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

import com.sandpolis.core.foundation.config.ConfigProperty;
import com.sandpolis.core.foundation.config.SysEnvConfigProperty;

public final class CfgInstance {

	/**
	 * Whether process mutexes will be checked and enforced.
	 */
	public static final ConfigProperty<Boolean> MUTEX = new SysEnvConfigProperty<>(Boolean.class, "s7s.mutex");

	/**
	 * The configuration directory.
	 */
	public static final ConfigProperty<String> PATH_CFG = new SysEnvConfigProperty<>(String.class, "s7s.path.config");

	/**
	 * The database directory.
	 */
	public static final ConfigProperty<String> PATH_DATA = new SysEnvConfigProperty<>(String.class, "s7s.path.data");

	/**
	 * The library directory.
	 */
	public static final ConfigProperty<String> PATH_LIB = new SysEnvConfigProperty<>(String.class, "s7s.path.lib");

	/**
	 * The log output directory.
	 */
	public static final ConfigProperty<String> PATH_LOG = new SysEnvConfigProperty<>(String.class, "s7s.path.log");

	/**
	 * The plugin directory.
	 */
	public static final ConfigProperty<String> PATH_PLUGIN = new SysEnvConfigProperty<>(String.class,
			"s7s.path.plugin");

	/**
	 * The temporary directory.
	 */
	public static final ConfigProperty<String> PATH_TMP = new SysEnvConfigProperty<>(String.class, "s7s.path.tmp");

	/**
	 * Whether plugins can be loaded.
	 */
	public static final ConfigProperty<Boolean> PLUGIN_ENABLED = new DefaultConfigProperty<>(Boolean.class,
			"s7s.plugins.enabled");

	/**
	 * Whether the startup summary will be logged.
	 */
	public static final ConfigProperty<Boolean> STARTUP_SUMMARY = new SysEnvConfigProperty<>(Boolean.class,
			"s7s.startup.logging.summary");

	/**
	 * The temporary directory.
	 */
	public static final ConfigProperty<String[]> LOG_LEVELS = new DefaultConfigProperty<>(String[].class,
			"s7s.log.levels", new String[] { "io.netty=WARN", "java.util.prefs=OFF", "com.sandpolis=INFO" });

	private CfgInstance() {
	}
}
