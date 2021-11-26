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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

public record BuildConfig(String platform, long timestamp, VersionConfig versions, List<String> dependencies) {

	public static final BuildConfig EMBEDDED = load();

	public static BuildConfig load() {

		try (var in = Entrypoint.data().main().getResourceAsStream("/config/com.sandpolis.build.json")) {
			if (in != null) {
				return new ObjectMapper().readValue(in, BuildConfig.class);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return null;
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
