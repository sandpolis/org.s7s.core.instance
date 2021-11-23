package com.sandpolis.core.instance.config;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandpolis.core.instance.Entrypoint;
import com.sandpolis.core.instance.config.InstanceConfig.LoggingConfig;
import com.sandpolis.core.instance.config.InstanceConfig.PluginConfig;

public record InstanceConfig(boolean development, LoggingConfig logging, PluginConfig plugin) {

	private static final Logger log = LoggerFactory.getLogger(InstanceConfig.class);

	public record LoggingConfig(String[] levels) {
	}

	public record PluginConfig(boolean enabled) {
	}

	public static final InstanceConfig INSTANCE = load();

	private static InstanceConfig load() {
		try (var in = Entrypoint.data().main().getResourceAsStream("/config/com.sandpolis.core.agent.json")) {
			if (in != null) {
				return new ObjectMapper().readValue(in, InstanceConfig.class);
			} else {
				log.debug("Instance config not found: /config/com.sandpolis.core.agent.json");
			}
		} catch (IOException e) {
			log.error("Failed to read instance config: /config/com.sandpolis.core.agent.json", e);
		}

		// Defaults
		return new InstanceConfig(false,
				new LoggingConfig(new String[] { "io.netty=WARN", "java.util.prefs=OFF", "com.sandpolis=INFO" }),
				new PluginConfig(true));
	}

	public static final ConfigProperty<String[]> LOG_LEVELS = ConfigProperty.evaluate(String[].class, "s7s.log.levels",
			INSTANCE.logging().levels());

	/**
	 * Whether plugins can be loaded.
	 */
	public static final ConfigProperty<Boolean> PLUGIN_ENABLED = ConfigProperty.evaluate(Boolean.class,
			"s7s.plugins.enabled", INSTANCE.plugin().enabled());

	/**
	 * Whether development features should be enabled.
	 */
	public static final ConfigProperty<Boolean> DEVELOPMENT_MODE = ConfigProperty.evaluate(Boolean.class,
			"s7s.development", INSTANCE.development());
}
