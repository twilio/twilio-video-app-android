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
import android.support.annotation.Nullable;

/**
 * A remote audio track publication represents a {@link RemoteAudioTrack} that has been shared to a
 * {@link Room}.
 */
public class RemoteAudioTrackPublication implements AudioTrackPublication {
    private final String sid;
    private final String name;
    private RemoteAudioTrack remoteAudioTrack;
    private boolean subscribed;
    private boolean enabled;

    RemoteAudioTrackPublication(
            boolean subscribed, boolean enabled, @NonNull String sid, @NonNull String name) {
        this.subscribed = subscribed;
        this.sid = sid;
        this.name = name;
        this.enabled = enabled;
    }

    /**
     * Returns the remote audio track's server identifier. This value uniquely identifies the remote
     * audio track within the scope of a {@link Room}.
     */
    @NonNull
    @Override
    public String getTrackSid() {
        return sid;
    }

    /**
     * Returns the base audio track object of the published remote audio track. {@code null} is
     * returned if the track is not subscribed to.
     */
    @Override
    public synchronized @Nullable AudioTrack getAudioTrack() {
        return remoteAudioTrack;
    }

    /**
     * Returns the name of the published audio track. An empty string is returned if no track name
     * was specified.
     */
    @NonNull
    @Override
    public String getTrackName() {
        return name;
    }

    /** Check if remote audio track is enabled. */
    @Override
    public synchronized boolean isTrackEnabled() {
        return enabled;
    }

    /** Check if the remote audio track is subscribed to by the {@link LocalParticipant}. */
    public synchronized boolean isTrackSubscribed() {
        return subscribed;
    }

    /**
     * Returns the published remote audio track. {@code null} is returned if the track is not
     * subscribed to.
     */
    @Nullable
    public synchronized RemoteAudioTrack getRemoteAudioTrack() {
        return remoteAudioTrack;
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
        if (remoteAudioTrack != null) {
            remoteAudioTrack.setEnabled(enabled);
        }
    }

    /*
     * Called from JNI layer when a track has been subscribed to.
     */
    @SuppressWarnings("unused")
    synchronized void setRemoteAudioTrack(RemoteAudioTrack remoteAudioTrack) {
        this.remoteAudioTrack = remoteAudioTrack;
    }
}
