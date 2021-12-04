//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.stream;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

public interface StreamEndpoint {

	/**
	 * @return The endpoint's stream ID
	 */
	public int getStreamID();

	public void close();

	public interface StreamPublisher<E> extends Publisher<E>, StreamEndpoint {

		/**
		 * @return A String that can be used to determine whether two
		 *         {@link StreamPublisher}s are producing the same content.
		 */
		public default String getStreamKey() {
			return null;
		}
	}

	public interface StreamSubscriber<E> extends Subscriber<E>, StreamEndpoint {

	}
}
