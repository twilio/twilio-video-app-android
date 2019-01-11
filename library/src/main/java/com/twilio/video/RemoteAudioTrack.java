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

/** A remote audio track represents a remote audio source. */
public class RemoteAudioTrack extends AudioTrack {
    private static final Logger logger = Logger.getLogger(RemoteAudioTrack.class);

    private long nativeRemoteAudioTrackHandle;
    private final String sid;
    private boolean playbackEnabled;
    private boolean isReleased = false;

    RemoteAudioTrack(
            long nativeRemoteAudioTrackHandle,
            @NonNull String sid,
            @NonNull String name,
            boolean isEnabled) {
        super(nativeRemoteAudioTrackHandle, isEnabled, name);
        this.nativeRemoteAudioTrackHandle = nativeRemoteAudioTrackHandle;
        this.sid = sid;
        this.playbackEnabled = true;
    }

    /**
     * Returns the remote audio track's server identifier. This value uniquely identifies the remote
     * audio track within the scope of a {@link Room}.
     */
    @NonNull
    public String getSid() {
        return sid;
    }

    /**
     * Enables playback of remote audio track. When playback is disabled the audio is muted.
     *
     * @param enable the desired playback state of the remote audio track.
     */
    public synchronized void enablePlayback(boolean enable) {
        this.playbackEnabled = enable;
        if (!isReleased) {
            nativeEnablePlayback(nativeRemoteAudioTrackHandle, enable);
        } else {
            logger.w(
                    "Cannot enable playback of remote audio track that is no longer "
                            + "subscribed to");
        }
    }

    /**
     * Check if playback on the remote audio track is enabled.
     *
     * <p>When the value is false, the remote audio track is muted. When the value is true the
     * remote audio track is playing.
     *
     * @return true if remote audio is enabled.
     */
    public synchronized boolean isPlaybackEnabled() {
        return playbackEnabled;
    }

    @Override
    synchronized void release() {
        if (!isReleased) {
            nativeRelease(nativeRemoteAudioTrackHandle);
            isReleased = true;
        }
    }

    private native void nativeEnablePlayback(long nativeAudioTrackHandle, boolean enable);

    private native void nativeRelease(long nativeAudioTrackHandle);
}
