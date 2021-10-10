//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.init;

import static com.sandpolis.core.instance.plugin.PluginStore.PluginStore;

import com.sandpolis.core.instance.InitTask;
import com.sandpolis.core.instance.config.CfgInstance;

public class InstanceLoadPlugins extends InitTask {

	@Override
	public boolean enabled() {
		return CfgInstance.PLUGIN_ENABLED.value().orElse(true);
	}

	@Override
	public TaskOutcome run(TaskOutcome.Factory outcome) throws Exception {
		PluginStore.scanPluginDirectory();
		PluginStore.loadPlugins();

		return outcome.succeeded();
	}

	@Override
	public String description() {
		return "Load plugins";
	}

}
