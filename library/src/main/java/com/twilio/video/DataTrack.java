/*
 * Copyright (C) 2017 Twilio, inc.
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

import android.support.annotation.NonNull;

/**
 * Base representation of {@link LocalDataTrack} and {@link RemoteDataTrack}.
 */
public abstract class DataTrack implements Track {
    private final boolean enabled;
    private final boolean ordered;
    private final boolean reliable;
    private final int maxPacketLifeTime;
    private final int maxRetransmits;
    private final String name;

    protected DataTrack(boolean enabled,
                        boolean ordered,
                        boolean reliable,
                        int maxPacketLifeTime,
                        int maxRetransmits,
                        @NonNull String name) {
        this.enabled = enabled;
        this.ordered = ordered;
        this.reliable = reliable;
        this.maxPacketLifeTime = maxPacketLifeTime;
        this.maxRetransmits = maxRetransmits;
        this.name = name;
    }

    /**
     * Check if this data track is enabled.
     *
     * @return true if the track is enabled.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the data track name. An empty string is returned if no track name was specified.
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns true if data track guarantees in-order delivery of messages.
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * Returns true if the data track guarantees reliable transmission of messages.
     */
    public boolean isReliable() {
        return reliable;
    }

    /**
     * Returns the maximum period of time in milliseconds in which retransmissions will be sent.
     * Returns {@code 0} if {@link DataTrackOptions#DEFAULT_MAX_PACKET_LIFE_TIME} was specified
     * when building the data track.
     */
    public int getMaxPacketLifeTime() {
        return maxPacketLifeTime;
    }

    /**
     * Returns the maximum number of times to transmit a message before giving up.
     * Returns {@code 0} if {@link DataTrackOptions#DEFAULT_MAX_RETRANSMITS} was specified
     * when building the data track.
     */
    public int getMaxRetransmits() {
        return maxRetransmits;
    }
}
