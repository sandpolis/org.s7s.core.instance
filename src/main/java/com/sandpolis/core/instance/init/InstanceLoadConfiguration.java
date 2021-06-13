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

import com.sandpolis.core.instance.InitTask;
import com.sandpolis.core.instance.TaskOutcome;
import com.sandpolis.core.instance.config.CfgInstance;

public class InstanceLoadConfiguration extends InitTask {

	@Override
	public TaskOutcome run(TaskOutcome outcome) throws Exception {

		CfgInstance.PATH_LIB.register();
		CfgInstance.PATH_LOG.register();
		CfgInstance.PATH_PLUGIN.register();
		CfgInstance.PATH_TMP.register();
		CfgInstance.PATH_DATA.register();
		CfgInstance.PATH_CFG.register();

		CfgInstance.PLUGIN_ENABLED.register(true);

//		CfgNet.MESSAGE_TIMEOUT.register(1000);

		return outcome.success();
	}

	@Override
	public String description() {
		return "Load configuration";
	}

}
