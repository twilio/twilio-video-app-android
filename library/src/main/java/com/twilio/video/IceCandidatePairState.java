/*
 * Copyright (C) 2018 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

/**
 * ICE candidate pair state as defined in <a href="https://tools.ietf.org/html/rfc5245">RFC
 * 5245</a>.
 */
public enum IceCandidatePairState {
    /** Succeeded: A check for this pair was already done and produced a successful result. */
    STATE_SUCCEEDED,

    /**
     * Frozen: A check for this pair hasn't been performed, and it can't yet be performed until some
     * other check succeeds, allowing this pair to unfreeze and move into the Waiting state.
     */
    STATE_FROZEN,

    /**
     * Waiting: A check has not been performed for this pair, and can be performed as soon as it is
     * the highest-priority Waiting pair on the check list.
     */
    STATE_WAITING,

    /** In-Progress: A check has been sent for this pair, but the transaction is in progress. */
    STATE_IN_PROGRESS,

    /**
     * Failed: A check for this pair was already done and failed, either never producing any
     * response or producing an unrecoverable failure response.
     */
    STATE_FAILED
}
