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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandpolis.core.instance.Entrypoint;

public record BuildConfig(String platform, long timestamp, VersionConfig versions, List<String> dependencies) {

	private static Optional<BuildConfig> instance;

	public static Optional<BuildConfig> get() {
		if (instance == null) {
			try (var in = Entrypoint.data().main().getResourceAsStream("/config/build.json")) {
				if (in != null) {
					instance = Optional.of(new ObjectMapper().readValue(in, BuildConfig.class));
				} else {
					instance = Optional.empty();
				}
			} catch (IOException e) {
				instance = Optional.empty();
			}
		}
		return instance;
	}

	public record VersionConfig(

			/**
			 * The instance version.
			 */
			String instance,

			/**
			 * The Gradle version used in the build.
			 */
			String gradle,

			/**
			 * The Java version used in the build.
			 */
			String java) {
	}
}
