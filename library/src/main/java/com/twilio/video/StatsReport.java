package com.twilio.video;


import java.util.List;

public class StatsReport {

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

    private final String peerConnectionId;
    private List<LocalAudioTrackStats> localAudioTracksStats;
    private List<LocalVideoTrackStats> localVideoTracksStats;
    private List<AudioTrackStats> audioTracksStats;
    private List<VideoTrackStats> videoTracksStats;
}
