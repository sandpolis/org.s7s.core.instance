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

import static com.sandpolis.core.instance.Metatypes.InstanceFlavor.ASCETIC;
import static com.sandpolis.core.instance.Metatypes.InstanceFlavor.LIFEGEM;
import static com.sandpolis.core.instance.Metatypes.InstanceFlavor.LOCKSTONE;
import static com.sandpolis.core.instance.Metatypes.InstanceFlavor.SOAPSTONE;
import static com.sandpolis.core.instance.Metatypes.InstanceFlavor.VANILLA;

import java.util.function.BiConsumer;

import com.sandpolis.core.instance.Metatypes.InstanceType;
import com.sandpolis.core.instance.Metatypes.InstanceFlavor;

public class InstanceUtil {

	public static InstanceFlavor[] getFlavors(InstanceType instance) {
		switch (instance) {
		case AGENT:
			return new InstanceFlavor[] { VANILLA };
		case SERVER:
			return new InstanceFlavor[] { VANILLA };
		case CLIENT:
			return new InstanceFlavor[] { ASCETIC, LIFEGEM, SOAPSTONE, LOCKSTONE };
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
