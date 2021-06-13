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

public abstract class InitTask {

	public abstract TaskOutcome run(TaskOutcome outcome) throws Exception;

	public abstract String description();

	public boolean fatal() {
		return false;
	}

	public boolean enabled() {
		return true;
	}
}