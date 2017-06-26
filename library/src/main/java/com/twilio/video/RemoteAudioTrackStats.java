package com.twilio.video;

public class RemoteAudioTrackStats extends TrackStats {
    /**
     * Audio output level
     */
    public final int audioLevel;

    /**
     * Packet jitter measured in milliseconds
     */
    public final int jitter;

    RemoteAudioTrackStats(String trackId,
                          int packetsLost,
                          String codec,
                          String ssrc,
                          double timestamp,
                          long bytesReceived,
                          int packetsReceived,
                          int audioLevel,
                          int jitter) {
        super(trackId, packetsLost, codec, ssrc,
                timestamp, bytesReceived, packetsReceived);
        this.audioLevel = audioLevel;
        this.jitter = jitter;
    }
}
