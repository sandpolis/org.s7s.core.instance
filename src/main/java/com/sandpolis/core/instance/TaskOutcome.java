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

import java.util.Objects;

import com.sandpolis.core.foundation.Result.Outcome;
import com.sandpolis.core.foundation.util.ProtoUtil;

/**
 * Represents the outcome of an {@link InitTask}.
 */
public class TaskOutcome {

	private Outcome outcome;
	private boolean skipped;

	private Outcome.Builder temporary;

	public TaskOutcome(String name) {
		temporary = ProtoUtil.begin(Objects.requireNonNull(name));
	}

	/**
	 * Mark the task as complete.
	 *
	 * @param result The task result
	 * @return A completed {@link TaskOutcome}
	 */
	public TaskOutcome complete(boolean result) {
		if (outcome != null)
			throw new IllegalStateException();

		outcome = ProtoUtil.complete(temporary.setResult(result));
		return this;
	}

	/**
	 * Mark the task as complete and merge the given outcome.
	 *
	 * @param _outcome The outcome to merge
	 * @return A completed {@link TaskOutcome}
	 */
	public TaskOutcome complete(Outcome _outcome) {
		if (outcome != null)
			throw new IllegalStateException();

		outcome = temporary.clearTime().mergeFrom(_outcome).build();
		return this;
	}

	/**
	 * Mark the task as failed.
	 *
	 * @return A completed {@link TaskOutcome}
	 */
	public TaskOutcome failure() {
		if (outcome != null)
			throw new IllegalStateException();

		outcome = ProtoUtil.failure(temporary);
		return this;
	}

	/**
	 * Mark the task as failed with an exception.
	 *
	 * @param t The relevant exception
	 * @return A completed {@link TaskOutcome}
	 */
	public TaskOutcome failure(Exception t) {
		if (outcome != null)
			throw new IllegalStateException();

		outcome = ProtoUtil.failure(temporary, t);
		return this;
	}

	/**
	 * Mark the task as failed with a comment.
	 *
	 * @param comment The relevant comment
	 * @return A completed {@link TaskOutcome}
	 */
	public TaskOutcome failure(String comment) {
		if (outcome != null)
			throw new IllegalStateException();

		outcome = ProtoUtil.failure(temporary, comment);
		return this;
	}

	/**
	 * @return Whether the task succeeded
	 */
	public boolean isSuccess() {
		return outcome.getResult();
	}

	/**
	 * @return The error comment
	 */
	public String getComment() {
		return outcome.getComment();
	}

	/**
	 * @return The exception stacktrace
	 */
	public String getException() {
		return outcome.getException();
	}

	/**
	 * @return The task duration in milliseconds
	 */
	public long getDuration() {
		return outcome.getTime();
	}

	/**
	 * @return The task's name.
	 */
	public String getName() {
		return outcome.getAction();
	}

	/**
	 * @return Whether the task was skipped
	 */
	public boolean isSkipped() {
		return skipped;
	}

	/**
	 * Mark the task as skipped.
	 *
	 * @return A completed {@link TaskOutcome}
	 */
	public TaskOutcome skipped() {
		if (outcome != null)
			throw new IllegalStateException();

		skipped = true;
		outcome = ProtoUtil.complete(temporary);
		return this;
	}

	/**
	 * Mark the task as succeeded.
	 *
	 * @return A completed {@link TaskOutcome}
	 */
	public TaskOutcome success() {
		if (outcome != null)
			throw new IllegalStateException();

		outcome = ProtoUtil.success(temporary);
		return this;
	}
}
