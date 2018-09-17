package com.twilio.video;

/**
 * ICE candidate pair state as defined in <a href="https://tools.ietf.org/html/rfc5245">RFC
 * 5245</a>.
 */
public enum IceCandidatePairState {
    /** Succeeded: A check for this pair was already done and produced a successful result. */
    kStateSucceeded,

    /**
     * Frozen: A check for this pair hasn't been performed, and it can't yet be performed until some
     * other check succeeds, allowing this pair to unfreeze and move into the Waiting state.
     */
    kStateFrozen,

    /**
     * Waiting: A check has not been performed for this pair, and can be performed as soon as it is
     * the highest-priority Waiting pair on the check list.
     */
    kStateWaiting,

    /** In-Progress: A check has been sent for this pair, but the transaction is in progress. */
    kStateInProgress,

    /**
     * Failed: A check for this pair was already done and failed, either never producing any
     * response or producing an unrecoverable failure response.
     */
    kStateFailed
}
