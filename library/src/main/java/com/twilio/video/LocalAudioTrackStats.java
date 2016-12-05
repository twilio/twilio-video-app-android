package com.twilio.video;

public class LocalAudioTrackStats extends LocalTrackStats {
    /**
     *  Audio input level
     */
    public final int audioInputLevel;

    /**
     * Packet jitter measured in milliseconds
     */
    public final int jitterReceived;

    /**
     * Jitter buffer measured in milliseconds
     */
    public final int jitterBufferMs;

    LocalAudioTrackStats(String trackId,
                                int packetsLost,
                                String codecName,
                                String ssrc,
                                double unixTimestamp,
                                long bytesSent,
                                int packetsSent,
                                long roundTripTime,
                                int audioInputLevel,
                                int jitterReceived,
                                int jitterBufferMs) {
        super(trackId, packetsLost, codecName, ssrc,
                unixTimestamp, bytesSent, packetsSent, roundTripTime);
        this.audioInputLevel = audioInputLevel;
        this.jitterReceived = jitterReceived;
        this.jitterBufferMs = jitterBufferMs;
    }
}
