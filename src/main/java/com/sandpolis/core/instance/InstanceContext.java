//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class InstanceContext {

	/**
	 * Whether process mutexes will be checked and enforced.
	 */
	public static final RuntimeVariable<Boolean> MUTEX = RuntimeVariable.of(cfg -> {
		cfg.type = Boolean.class;
		cfg.secondary = SystemProperty.of("s7s.mutex.enabled");
		cfg.tertiary = EnvironmentVariable.of("S7S_MUTEX_ENABLED");
		cfg.defaultValue = () -> true;
	});

	/**
	 * The database directory.
	 */
	public static final RuntimeVariable<Path> PATH_DATA = RuntimeVariable.of(cfg -> {
		cfg.type = Path.class;
		cfg.secondary = SystemProperty.of("s7s.path.data");
		cfg.tertiary = EnvironmentVariable.of("S7S_PATH_DATA");
		cfg.defaultValue = () -> Entrypoint.data().jar().resolveSibling("data");
	});

	/**
	 * The library directory.
	 */
	public static final RuntimeVariable<Path> PATH_LIB = RuntimeVariable.of(cfg -> {
		cfg.type = Path.class;
		cfg.secondary = SystemProperty.of("s7s.path.lib");
		cfg.tertiary = EnvironmentVariable.of("S7S_PATH_LIB");
		cfg.defaultValue = () -> Entrypoint.data().jar().resolveSibling("lib");
	});

	/**
	 * The log output directory.
	 */
	public static final RuntimeVariable<Path> PATH_LOG = RuntimeVariable.of(cfg -> {
		cfg.type = Path.class;
		cfg.secondary = SystemProperty.of("s7s.path.log");
		cfg.tertiary = EnvironmentVariable.of("S7S_PATH_LOG");
		cfg.defaultValue = () -> Entrypoint.data().jar().resolveSibling("log");
	});

	/**
	 * The plugin directory.
	 */
	public static final RuntimeVariable<Path> PATH_PLUGIN = RuntimeVariable.of(cfg -> {
		cfg.type = Path.class;
		cfg.secondary = SystemProperty.of("s7s.path.plugin");
		cfg.tertiary = EnvironmentVariable.of("S7S_PATH_PLUGIN");
		cfg.defaultValue = () -> Entrypoint.data().jar().resolveSibling("plugin");
	});

	/**
	 * The temporary directory.
	 */
	public static final RuntimeVariable<Path> PATH_TMP = RuntimeVariable.of(cfg -> {
		cfg.type = Path.class;
		cfg.secondary = SystemProperty.of("s7s.path.tmp");
		cfg.tertiary = EnvironmentVariable.of("S7S_PATH_TMP");
		cfg.defaultValue = () -> Paths.get(System.getProperty("java.io.tmpdir"));
	});

	public static final RuntimeVariable<String[]> LOG_LEVELS = RuntimeVariable.of(cfg -> {
		cfg.type = String[].class;
		cfg.secondary = SystemProperty.of("s7s.logging.levels");
		cfg.tertiary = EnvironmentVariable.of("S7S_LOG_LEVELS");
		cfg.defaultValue = () -> InstanceConfig.EMBEDDED.logging().levels();
	});

	/**
	 * Whether plugins can be loaded.
	 */
	public static final RuntimeVariable<Boolean> PLUGIN_ENABLED = RuntimeVariable.of(cfg -> {
		cfg.type = Boolean.class;
		cfg.secondary = SystemProperty.of("s7s.plugins.enabled");
		cfg.tertiary = EnvironmentVariable.of("S7S_PLUGINS_ENABLED");
		cfg.defaultValue = () -> InstanceConfig.EMBEDDED.plugin().enabled();
	});

	/**
	 * Whether development features should be enabled.
	 */
	public static final RuntimeVariable<Boolean> DEVELOPMENT_MODE = RuntimeVariable.of(cfg -> {
		cfg.type = Boolean.class;
		cfg.secondary = SystemProperty.of("s7s.development");
		cfg.tertiary = EnvironmentVariable.of("S7S_DEVELOPMENT");
		cfg.defaultValue = () -> InstanceConfig.EMBEDDED.development();
	});

}
