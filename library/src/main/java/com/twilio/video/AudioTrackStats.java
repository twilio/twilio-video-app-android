package com.twilio.video;

public class AudioTrackStats extends TrackStats {
    public final int audioOutputLevel;
    public final int jitterReceived;

    public AudioTrackStats(String trackId,
                           int packetsLost,
                           String codecName,
                           String ssrc,
                           double unixTimestamp,
                           long bytesReceived,
                           int packetsReceived,
                           int jitterBuffer,
                           int audioOutputLevel,
                           int jitterReceived) {
        super(trackId, packetsLost, codecName, ssrc,
                unixTimestamp, bytesReceived, packetsReceived, jitterBuffer);
        this.audioOutputLevel = audioOutputLevel;
        this.jitterReceived = jitterReceived;
    }
}
