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

import com.sandpolis.core.instance.Environment;
import com.sandpolis.core.instance.InitTask;
import com.sandpolis.core.instance.TaskOutcome;

public class InstanceLoadEnvironment extends InitTask {

	@Override
	public TaskOutcome run(TaskOutcome outcome) throws Exception {
		Environment.LIB.requireReadable();
		Environment.DATA.requireWritable();
		Environment.CFG.requireWritable();
		Environment.LOG.requireWritable();
		Environment.PLUGIN.requireWritable();
		Environment.TMP.requireWritable();
		return outcome.success();
	}

	@Override
	public String description() {
		return "Load runtime environment";
	}

	@Override
	public boolean fatal() {
		return true;
	}

}
