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


import java.util.ArrayList;
import java.util.List;

/**
 * Stats report contains stats for all the media tracks that exist in peer connection.
 */
public class StatsReport {
    private final String peerConnectionId;
    private List<LocalAudioTrackStats> localAudioTrackStats = new ArrayList<>();
    private List<LocalVideoTrackStats> localVideoTrackStats = new ArrayList<>();
    private List<AudioTrackStats> audioTrackStats = new ArrayList<>();
    private List<VideoTrackStats> videoTrackStats = new ArrayList<>();

    StatsReport(String peerConnectionId) {
        this.peerConnectionId = peerConnectionId;
    }

    /**
     * Returns the id of peer connection related to this report.
     */
    public String getPeerConnectionId() {
        return peerConnectionId;
    }

    /**
     * Returns stats for all local audio tracks in the peer connection.
     *
     * @return a list of local audio tracks stats
     */
    public List<LocalAudioTrackStats> getLocalAudioTrackStats() {
        return localAudioTrackStats;
    }

    /**
     * Returns stats for all local video tracks in the peer connection.
     *
     * @return a list of local video tracks stats
     */
    public List<LocalVideoTrackStats> getLocalVideoTrackStats() {
        return localVideoTrackStats;
    }

    /**
     * Returns stats for all remote audio tracks in the peer connection.
     *
     * @return a list of remote audio tracks stats
     */
    public List<AudioTrackStats> getAudioTrackStats() {
        return audioTrackStats;
    }

    /**
     * Returns stats for all remote video tracks in the peer connection.
     *
     * @return a list of remote video tracks stats
     */
    public List<VideoTrackStats> getVideoTrackStats() {
        return videoTrackStats;
    }

    void addLocalAudioTrackStats(LocalAudioTrackStats localAudioTrackStats) {
        this.localAudioTrackStats.add(localAudioTrackStats);
    }

    void addLocalVideoTrackStats(LocalVideoTrackStats localVideoTrackStats) {
        this.localVideoTrackStats.add(localVideoTrackStats);
    }

    void addAudioTrackStats(AudioTrackStats audioTrackStats) {
        this.audioTrackStats.add(audioTrackStats);
    }

    void addVideoTrackStats(VideoTrackStats videoTrackStats) {
        this.videoTrackStats.add(videoTrackStats);
    }
}
