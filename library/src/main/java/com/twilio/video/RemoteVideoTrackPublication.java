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
import android.support.annotation.Nullable;

/**
 * A remote video track publication represents a {@link RemoteVideoTrack} that has been shared
 * to a {@link Room}.
 */
public class RemoteVideoTrackPublication implements VideoTrackPublication {
    private final String sid;
    private final String name;
    private RemoteVideoTrack remoteVideoTrack;
    private boolean subscribed;
    private boolean enabled;

    RemoteVideoTrackPublication(boolean subscribed,
                                boolean enabled,
                                @NonNull String sid,
                                @NonNull String name) {
        this.enabled = enabled;
        this.subscribed = subscribed;
        this.sid = sid;
        this.name = name;
    }

    /**
     * Returns the remote video track's server identifier. This value uniquely identifies the remote
     * video track within the scope of a {@link Room}.
     */
    @Override
    public String getTrackSid() {
        return sid;
    }

    /**
     * Returns the base video track object of the published remote video track. {@code null} is
     * returned if the track is not subscribed.
     */
    @Override
    public synchronized @Nullable VideoTrack getVideoTrack() {
        return remoteVideoTrack;
    }

    /**
     * Returns the name of the published video track. An empty string is returned if no track name
     * was specified.
     */
    @Override
    public String getTrackName() {
        return name;
    }

    /**
     * Returns true of the published video track is enabled or false otherwise.
     */
    @Override
    public synchronized boolean isTrackEnabled() {
        return enabled;
    }

    /**
     * Check if the remote video track is subscribed to by the {@link LocalParticipant}.
     */
    public synchronized boolean isTrackSubscribed() {
        return subscribed;
    }

    /**
     * Returns the published remote video track. {@code null} is returned if the track is not
     * subscribed to.
     */
    public synchronized @Nullable RemoteVideoTrack getRemoteVideoTrack() {
        return remoteVideoTrack;
    }

    /*
     * Set by remote participant listener proxy.
     */
    synchronized void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    /*
     * Set by remote participant listener proxy.
     */
    synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (remoteVideoTrack != null) {
            remoteVideoTrack.setEnabled(enabled);
        }
    }

    /*
     * Called from JNI layer when a track has been subscribed to.
     */
    @SuppressWarnings("unused")
    synchronized void setRemoteVideoTrack(RemoteVideoTrack remoteVideoTrack) {
        this.remoteVideoTrack = remoteVideoTrack;
    }
}
