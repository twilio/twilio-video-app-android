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

/**
 * A remote audio track represents a remote audio source.
 */
public class RemoteAudioTrack extends AudioTrack {
    private final String sid;
    private boolean subscribed;
    private org.webrtc.AudioTrack webRtcAudioTrack;
    private boolean playbackEnabled;

    RemoteAudioTrack(String sid,
                     String trackId,
                     boolean isEnabled,
                     boolean subscribed) {
        super(trackId, isEnabled);
        this.sid = sid;
        this.subscribed = subscribed;
        this.playbackEnabled = true;
    }

    /**
     * Returns a string that uniquely identifies the remote audio track within the scope
     * of a {@link Room}.
     *
     * @return sid
     */
    public String getSid() {
        return sid;
    }

    /**
     * Check if remote audio track is subscribed to by {@link LocalParticipant}.
     */
    public synchronized boolean isSubscribed() {
        return subscribed;
    }

    /**
     * Enables playback of remote audio track. When playback is disabled the audio is muted.
     *
     * @param enable the desired playback state of the remote audio track.
     */
    public synchronized void enablePlayback(boolean enable) {
        this.playbackEnabled = enable;

        if (webRtcAudioTrack != null) {
            webRtcAudioTrack.setEnabled(playbackEnabled);
        }
    }

    /**
     * Check if playback on the remote audio track is enabled.
     *
     * When the value is false, the remote audio track is muted. When the value is true the remote
     * audio track is playing.
     *
     * @return true if remote audio is enabled.
     */
    public synchronized boolean isPlaybackEnabled() {
        return playbackEnabled;
    }

    /*
     * Called from JNI layer after a remote audio track has been subscribed to
     */
    @SuppressWarnings("unused")
    synchronized void setWebRtcTrack(org.webrtc.AudioTrack webRtcAudioTrack) {
        Preconditions.checkState(this.webRtcAudioTrack == null, "Did not invalidate WebRTC track " +
                "before initializing new track");
        this.webRtcAudioTrack = webRtcAudioTrack;
        this.webRtcAudioTrack.setEnabled(playbackEnabled);
    }

    synchronized void invalidateWebRtcTrack() {
        this.webRtcAudioTrack = null;
    }

    /*
     * State updated by remote participant listener proxy.
     */
    synchronized void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }
}
