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

import java.util.concurrent.SubmissionPublisher;
import java.util.random.RandomGenerator;

import com.google.protobuf.MessageLiteOrBuilder;
import com.sandpolis.core.foundation.S7SRandom;
import com.sandpolis.core.instance.stream.StreamEndpoint.StreamPublisher;

/**
 * @author cilki
 * @since 5.0.2
 */
public abstract class StreamSource<E extends MessageLiteOrBuilder> extends SubmissionPublisher<E>
		implements StreamPublisher<E> {

	private int id;

	public StreamSource() {
		id = S7SRandom.insecure.nextInt();
		id = RandomGenerator.getDefault().nextInt();
	}

	@Override
	public int getStreamID() {
		return id;
	}

	/**
	 * Begin the flow of events from the source.
	 */
	public abstract void start();

}
