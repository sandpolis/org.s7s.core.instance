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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hashing;
import com.sandpolis.core.foundation.Result.Outcome;
import com.sandpolis.core.foundation.util.SystemUtil;
import com.sandpolis.core.instance.Metatypes.InstanceFlavor;
import com.sandpolis.core.instance.Metatypes.InstanceType;
import com.sandpolis.core.instance.config.CfgInstance;

/**
 * Main classes can inherit this class to have the instance setup automatically.
 */
public abstract class Entrypoint {

	private static final Logger log = LoggerFactory.getLogger(Entrypoint.class);

	public static record EntrypointInfo(

			/**
			 * The instance's main class.
			 */
			Class<?> main,

			/**
			 * The instance's type.
			 */
			InstanceType instance,

			/**
			 * The instance's subtype.
			 */
			InstanceFlavor flavor,

			/**
			 * The instance's UUID.
			 */
			String uuid,

			/**
			 * Build information.
			 */
			Properties so_build) {

	}

	private static EntrypointInfo metadata;

	public static EntrypointInfo data() {
		if (metadata == null) {
			throw new IllegalStateException();
		}
		return metadata;
	}

	/**
	 * A list of tasks that initialize the instance.
	 */
	private List<InitTask> tasks = new ArrayList<>();

	/**
	 * A list of tasks that are executed on instance shutdown.
	 */
	private List<ShutdownTask> shutdown = new ArrayList<>();

	/**
	 * A list of tasks that are executed periodically.
	 */
	private IdleLoop idle;

	private boolean started = false;

	private Properties readBuildInfo(Class<?> main) {
		try (var in = main.getResourceAsStream("/build.properties")) {
			if (in == null) {
				throw new RuntimeException("build.properties not found");
			}

			var so_build = new Properties();
			so_build.load(in);
			return so_build;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private UUID readUuid(InstanceType instance, InstanceFlavor flavor) {
		int seed = instance.getNumber() << 24;
		seed |= flavor.getNumber() << 16;
		seed |= SystemUtil.OS_TYPE.getNumber();

		var uuid_hash = Hashing.murmur3_128(seed).newHasher();

		try {
			for (var i : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if (!i.isLoopback() && !i.isVirtual()) {
					uuid_hash.putBytes(i.getHardwareAddress());
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		var uuid_buffer = ByteBuffer.wrap(uuid_hash.hash().asBytes());

		return new UUID(uuid_buffer.getLong(), uuid_buffer.getLong());
	}

	protected Entrypoint(Class<?> main, InstanceType instance, InstanceFlavor flavor) {

		Entrypoint.metadata = new EntrypointInfo(main, instance, flavor, readUuid(instance, flavor).toString(),
				readBuildInfo(main));
	}

	public void register(InitTask task) {
		if (started)
			throw new IllegalStateException("Cannot register task");

		tasks.add(task);
	}

	public void start(String instanceName, String[] args) {
		if (started)
			throw new IllegalStateException("Start cannot be called more than once");

		started = true;

		final long timestamp = System.currentTimeMillis();

		// Setup exception handler
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			log.error("An unexpected exception has occurred", throwable);
		});

		// Setup shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			shutdown.forEach(task -> {
				try {
					task.run();
				} catch (Exception e) {
					log.error("Failed to execute shutdown task", e);
				}
			});
		}));

		boolean initFailed = false;
		List<TaskOutcome> outcomes = new ArrayList<>();

		// Execute registered initialization tasks
		for (var task : tasks) {

			TaskOutcome outcome = new TaskOutcome(task.description());
			outcomes.add(outcome);

			// Skip if the entire run failed or the task is not enabled
			if (initFailed || !task.enabled()) {
				outcome.skipped();
				continue;
			}

			// Execute the task
			try {
				if (outcome != task.run(outcome)) {
					throw new RuntimeException();
				}
			} catch (Exception e) {
				outcome.failure(e);
			}

			// Exit if the task failed and fatal is enabled
			if (!outcome.isSkipped() && !outcome.isSuccess() && task.fatal()) {
				initFailed = true;
			}
		}

		if (initFailed) {
			logSummary(outcomes);
			System.exit(1);
		}

		// Print task summary if required
		else if (CfgInstance.STARTUP_SUMMARY.value().orElse(true)) {
			logSummary(outcomes);
		}

		// Launch idle loop
		if (idle != null)
			idle.start();

		log.info("Initialization completed in {} ms", System.currentTimeMillis() - timestamp);
	}

	/**
	 * Build a summary of the task execution and write to log.
	 */
	private void logSummary(List<TaskOutcome> outcomes) {
		if (outcomes.isEmpty()) {
			return;
		}

		// Create a format string according to the width of the longest task description
		String descFormat = String.format("%%%ds:",
				Math.min(outcomes.stream().map(TaskOutcome::getName).mapToInt(String::length).max().getAsInt(), 70));

		for (var outcome : outcomes) {

			// Format description and result
			String line = String.format(descFormat + " %4s", outcome.getName(),
					outcome.isSkipped() ? "SKIP" : outcome.isSuccess() ? "OK" : "FAIL");

			// Format duration
			if (outcome.isSkipped() || !outcome.isSuccess())
				line += " ( ---- ms)";
			else if (outcome.getDuration() > 9999)
				line += String.format(" (%5.1f  s)", outcome.getDuration() / 1000.0);
			else
				line += String.format(" (%5d ms)", outcome.getDuration());

			// Write to log
			if (outcome.isSkipped() || outcome.isSuccess()) {
				log.info(line);
			} else {
				log.error(line);
			}
		}

		// Log any failure messages/exceptions
		for (var outcome : outcomes) {
			if (!outcome.isSkipped()) {

				if (!outcome.isSuccess()) {
					if (!outcome.getException().isEmpty())
						log.error("An exception occurred in task \"{}\":\n{}", outcome.getName(),
								outcome.getException());
					else if (!outcome.getComment().isEmpty())
						log.error("An error occurred in task \"{}\": {}", outcome.getName(), outcome.getComment());
					else
						log.error("An unknown error occurred in task \"{}\"", outcome.getName());
				}
			}
		}
	}
}
