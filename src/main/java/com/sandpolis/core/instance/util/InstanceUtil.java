//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.util;

import static com.sandpolis.core.instance.Metatypes.InstanceFlavor.CLIENT_ASCETIC;
import static com.sandpolis.core.instance.Metatypes.InstanceFlavor.CLIENT_LIFEGEM;
import static com.sandpolis.core.instance.Metatypes.InstanceFlavor.CLIENT_LOCKSTONE;
import static com.sandpolis.core.instance.Metatypes.InstanceFlavor.CLIENT_SOAPSTONE;
import static com.sandpolis.core.instance.Metatypes.InstanceFlavor.SERVER_VANILLA;
import static com.sandpolis.core.instance.Metatypes.InstanceFlavor.AGENT_KILO;

import java.util.function.BiConsumer;

import com.sandpolis.core.instance.Metatypes.InstanceType;
import com.sandpolis.core.instance.Metatypes.InstanceFlavor;

public class InstanceUtil {

	public static InstanceFlavor[] getFlavors(InstanceType instance) {
		switch (instance) {
		case AGENT:
			return new InstanceFlavor[] { AGENT_KILO };
		case SERVER:
			return new InstanceFlavor[] { SERVER_VANILLA };
		case CLIENT:
			return new InstanceFlavor[] { CLIENT_ASCETIC, CLIENT_LIFEGEM, CLIENT_SOAPSTONE, CLIENT_LOCKSTONE };
		default:
			return null;
		}
	}

	public static void iterate(BiConsumer<InstanceType, InstanceFlavor> consumer) {
		for (var instance : InstanceType.values()) {
			if (instance != InstanceType.UNRECOGNIZED) {
				for (var flavor : getFlavors(instance)) {
					consumer.accept(instance, flavor);
				}
			}
		}
	}
}
