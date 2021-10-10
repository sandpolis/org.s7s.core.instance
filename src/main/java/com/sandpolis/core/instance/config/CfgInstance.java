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

public final class CfgInstance {

	/**
	 * Whether process mutexes will be checked and enforced.
	 */
	public static final ConfigProperty<Boolean> MUTEX = ConfigProperty.evaluate(Boolean.class, "s7s.mutex");

	/**
	 * The configuration directory.
	 */
	public static final ConfigProperty<String> PATH_CFG = ConfigProperty.evaluate(String.class, "s7s.path.config");

	/**
	 * The database directory.
	 */
	public static final ConfigProperty<String> PATH_DATA = ConfigProperty.evaluate(String.class, "s7s.path.data");

	/**
	 * The library directory.
	 */
	public static final ConfigProperty<String> PATH_LIB = ConfigProperty.evaluate(String.class, "s7s.path.lib");

	/**
	 * The log output directory.
	 */
	public static final ConfigProperty<String> PATH_LOG = ConfigProperty.evaluate(String.class, "s7s.path.log");

	/**
	 * The plugin directory.
	 */
	public static final ConfigProperty<String> PATH_PLUGIN = ConfigProperty.evaluate(String.class, "s7s.path.plugin");

	/**
	 * The temporary directory.
	 */
	public static final ConfigProperty<String> PATH_TMP = ConfigProperty.evaluate(String.class, "s7s.path.tmp");

	/**
	 * Whether plugins can be loaded.
	 */
	public static final ConfigProperty<Boolean> PLUGIN_ENABLED = ConfigProperty.evaluate(Boolean.class,
			"s7s.plugins.enabled");

	/**
	 * Whether the startup summary will be logged.
	 */
	public static final ConfigProperty<Boolean> STARTUP_SUMMARY = ConfigProperty.evaluate(Boolean.class,
			"s7s.startup.logging.summary");

	/**
	 * The temporary directory.
	 */
	public static final ConfigProperty<String[]> LOG_LEVELS = ConfigProperty.evaluate(String[].class, "s7s.log.levels",
			new String[] { "io.netty=WARN", "java.util.prefs=OFF", "com.sandpolis=INFO" });

	/**
	 * Whether development features should be enabled.
	 */
	public static final ConfigProperty<Boolean> DEVELOPMENT_MODE = ConfigProperty.evaluate(Boolean.class,
			"s7s.development_mode");

	private CfgInstance() {
	}
}
