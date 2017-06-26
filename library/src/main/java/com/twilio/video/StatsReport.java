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
    private List<RemoteAudioTrackStats> remoteAudioTrackStats = new ArrayList<>();
    private List<RemoteVideoTrackStats> remoteVideoTrackStats = new ArrayList<>();

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
    public List<RemoteAudioTrackStats> getRemoteAudioTrackStats() {
        return remoteAudioTrackStats;
    }

    /**
     * Returns stats for all remote video tracks in the peer connection.
     *
     * @return a list of remote video tracks stats
     */
    public List<RemoteVideoTrackStats> getRemoteVideoTrackStats() {
        return remoteVideoTrackStats;
    }

    void addLocalAudioTrackStats(LocalAudioTrackStats localAudioTrackStats) {
        this.localAudioTrackStats.add(localAudioTrackStats);
    }

    void addLocalVideoTrackStats(LocalVideoTrackStats localVideoTrackStats) {
        this.localVideoTrackStats.add(localVideoTrackStats);
    }

    void addAudioTrackStats(RemoteAudioTrackStats remoteAudioTrackStats) {
        this.remoteAudioTrackStats.add(remoteAudioTrackStats);
    }

    void addVideoTrackStats(RemoteVideoTrackStats remoteVideoTrackStats) {
        this.remoteVideoTrackStats.add(remoteVideoTrackStats);
    }
}
