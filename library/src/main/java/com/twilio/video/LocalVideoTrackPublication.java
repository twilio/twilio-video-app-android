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
 * A local video track publication represents a {@link LocalVideoTrack} that has been shared to a
 * {@link Room}.
 */
public class LocalVideoTrackPublication implements VideoTrackPublication {
    private final String sid;
    private final LocalVideoTrack localVideoTrack;

    LocalVideoTrackPublication(@NonNull String sid, @NonNull LocalVideoTrack localVideoTrack) {
        Preconditions.checkNotNull(sid, "SID must not be null");
        Preconditions.checkArgument(!sid.isEmpty(), "SID must not be empty");
        Preconditions.checkNotNull(localVideoTrack, "Local video track must not be null");
        this.sid = sid;
        this.localVideoTrack = localVideoTrack;
    }

    /**
     * Returns the local video track's server identifier. This value uniquely identifies the local
     * video track within the scope of a {@link Room}.
     */
    @Override
    public String getTrackSid() {
        return sid;
    }

    /**
     * Returns the name of the local video track. An empty string is returned if no name was
     * specified.
     */
    @Override
    public String getTrackName() {
        return localVideoTrack.getName();
    }

    /** Check if local video track is enabled. */
    @Override
    public boolean isTrackEnabled() {
        return localVideoTrack.isEnabled();
    }

    /** Returns the base video track object of the published local video track. */
    @Override
    public VideoTrack getVideoTrack() {
        return localVideoTrack;
    }

    /** Returns the published local video track. */
    public LocalVideoTrack getLocalVideoTrack() {
        return localVideoTrack;
    }
}
