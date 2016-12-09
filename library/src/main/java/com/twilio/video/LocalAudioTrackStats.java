package com.twilio.video;

public class LocalAudioTrackStats extends LocalTrackStats {
    /**
     *  Audio input level
     */
    public final int audioLevel;

    /**
     * Packet jitter measured in milliseconds
     */
    public final int jitter;

    LocalAudioTrackStats(String trackId,
                         int packetsLost,
                         String codec,
                         String ssrc,
                         double timestamp,
                         long bytesSent,
                         int packetsSent,
                         long roundTripTime,
                         int audioLevel,
                         int jitter) {
        super(trackId, packetsLost, codec, ssrc,
                timestamp, bytesSent, packetsSent, roundTripTime);
        this.audioLevel = audioLevel;
        this.jitter = jitter;
    }
}
