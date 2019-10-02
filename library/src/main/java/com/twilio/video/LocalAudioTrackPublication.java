/*
 * Copyright (C) 2017 Twilio, Inc.
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
 * A local audio track publication represents a {@link LocalAudioTrack} that has been shared to a
 * {@link Room}.
 */
public class LocalAudioTrackPublication implements AudioTrackPublication {
    private final String sid;
    private final LocalAudioTrack localAudioTrack;

    LocalAudioTrackPublication(@NonNull String sid, @NonNull LocalAudioTrack localAudioTrack) {
        Preconditions.checkNotNull(sid, "SID must not be null");
        Preconditions.checkNotNull(localAudioTrack, "Local audio track must not be null");
        Preconditions.checkArgument(!sid.isEmpty(), "SID must not be empty");
        this.sid = sid;
        this.localAudioTrack = localAudioTrack;
    }

    /**
     * Returns the local audio track's server identifier. This value uniquely identifies the local
     * audio track within the scope of a {@link Room}.
     */
    @NonNull
    @Override
    public String getTrackSid() {
        return sid;
    }

    /**
     * Returns the name of the local audio track. An empty string is returned if not name was
     * specified.
     */
    @NonNull
    @Override
    public String getTrackName() {
        return localAudioTrack.getName();
    }

    /** Check if local audio track is enabled. */
    @Override
    public boolean isTrackEnabled() {
        return localAudioTrack.isEnabled();
    }

    /**
     * Unlike {@link AudioTrackPublication} or {@link RemoteAudioTrackPublication} this {@link
     * AudioTrack} is guaranteed to be non null.
     *
     * @return Returns the base audio track object of the published local audio track.
     */
    @NonNull
    @Override
    public AudioTrack getAudioTrack() {
        return localAudioTrack;
    }

    /** Returns the published local audio track. */
    @NonNull
    public LocalAudioTrack getLocalAudioTrack() {
        return localAudioTrack;
    }
}
