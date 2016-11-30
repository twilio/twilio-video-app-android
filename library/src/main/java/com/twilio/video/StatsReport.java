package com.twilio.video;


import java.util.ArrayList;
import java.util.List;

public class StatsReport {
    private final String peerConnectionId;
    private List<LocalAudioTrackStats> localAudioTracksStats = new ArrayList<>();
    private List<LocalVideoTrackStats> localVideoTracksStats = new ArrayList<>();
    private List<AudioTrackStats> audioTracksStats = new ArrayList<>();
    private List<VideoTrackStats> videoTracksStats = new ArrayList<>();

    public StatsReport(String peerConnectionId) {
        this.peerConnectionId = peerConnectionId;
    }

    public String getPeerConnectionId() {
        return peerConnectionId;
    }

    public List<LocalAudioTrackStats> getLocalAudioTrackStats() {
        return localAudioTracksStats;
    }

    public List<LocalVideoTrackStats> getLocalVideoTrackStats() {
        return localVideoTracksStats;
    }

    public List<AudioTrackStats> getAudioTrackStats() {
        return audioTracksStats;
    }

    public List<VideoTrackStats> getVideoTrackStats() {
        return videoTracksStats;
    }

    void addLocalAudioTrackStats(LocalAudioTrackStats localAudioTrackStats) {
        localAudioTracksStats.add(localAudioTrackStats);
    }

    void addLocalVideoTrackStats(LocalVideoTrackStats localVideoTrackStats) {
        localVideoTracksStats.add(localVideoTrackStats);
    }

    void addAudioTrackStats(AudioTrackStats audioTrackStats) {
        audioTracksStats.add(audioTrackStats);
    }

    void addVideoTrackStats(VideoTrackStats videoTrackStats) {
        videoTracksStats.add(videoTrackStats);
    }
}
