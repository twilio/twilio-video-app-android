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
 * A local data track publication represents a {@link LocalDataTrack} that has been shared to a
 * {@link Room}.
 */
public class LocalDataTrackPublication implements DataTrackPublication {
    private final String sid;
    private final LocalDataTrack localDataTrack;

    LocalDataTrackPublication(@NonNull String sid, @NonNull LocalDataTrack localDataTrack) {
        Preconditions.checkNotNull(sid, "SID must not be null");
        Preconditions.checkArgument(!sid.isEmpty(), "SID must not be empty");
        Preconditions.checkNotNull(localDataTrack, "Local data track must not be null");
        this.sid = sid;
        this.localDataTrack = localDataTrack;
    }

    /**
     * Returns the local data track's server identifier. This value uniquely identifies the local
     * data track within the scope of a {@link Room}.
     */
    @Override
    public String getTrackSid() {
        return sid;
    }

    /**
     * Returns the name of the local data track. An empty string is returned if no name was
     * specified.
     */
    @Override
    public String getTrackName() {
        return localDataTrack.getName();
    }

    /** Check if local data track is enabled. */
    @Override
    public boolean isTrackEnabled() {
        return localDataTrack.isEnabled();
    }

    /** Returns the base data track object of the published local data track. */
    @Override
    public DataTrack getDataTrack() {
        return localDataTrack;
    }

    /** Returns the published local data track. */
    public LocalDataTrack getLocalDataTrack() {
        return localDataTrack;
    }
}
