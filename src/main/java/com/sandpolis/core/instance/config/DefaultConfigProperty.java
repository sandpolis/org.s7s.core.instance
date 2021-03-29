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
import java.nio.file.Files;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sandpolis.core.foundation.config.SysEnvConfigProperty;
import com.sandpolis.core.instance.Environment;

public class DefaultConfigProperty<T> extends SysEnvConfigProperty<T> {

	private static final Logger log = LoggerFactory.getLogger(DefaultConfigProperty.class);

	public DefaultConfigProperty(Class<T> type, String property) {
		super(type, property);
	}

	public DefaultConfigProperty(Class<T> type, String property, T defaultValue) {
		super(type, property, defaultValue);
	}

	@Override
	protected boolean evaluate() {
		if (!super.evaluate()) {

			// Check configuration file
			Properties configuration = new Properties();
			try (var in = Files.newInputStream(Environment.CFG.path().resolve("instance.properties"))) {
				configuration.load(in);
			} catch (IOException e) {
				return false;
			}

			String value = configuration.getProperty(property());
			if (value != null) {
				log.trace("Found property in config file: {}", property());
				setValue(value);
				return true;
			}
			return false;
		}
		return true;
	}

}
