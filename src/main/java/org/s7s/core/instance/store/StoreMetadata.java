//============================================================================//
//                                                                            //
//            Copyright © 2015 - 2022 Sandpolis Software Foundation           //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPLv2. //
//                                                                            //
//============================================================================//
package org.s7s.core.instance.store;

/**
 * {@link StoreMetadata} contains information about the store itself.
 */
public interface StoreMetadata {

	/**
	 * Get the number of times the store has been initialized.
	 *
	 * @return The initialization count
	 */
	public int getInitCount();
}
