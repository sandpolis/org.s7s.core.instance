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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record EnvironmentVariable(String name, Optional<String> value) {

	private static final Logger log = LoggerFactory.getLogger(EnvironmentVariable.class);

	public EnvironmentVariable(String name, Optional<String> value) {
		this.name = name;
		this.value = value;

		if (value.isPresent()) {
			log.trace("Loaded environment variable: {} -> \"{}\"", name, value.get());
		}
	}

	public static EnvironmentVariable of(String name) {
		return new EnvironmentVariable(name, Optional.ofNullable(System.getenv().get(name)));
	}
}
