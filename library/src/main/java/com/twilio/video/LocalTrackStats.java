package com.twilio.video;

public abstract class LocalTrackStats extends BaseTrackStats {
    /**
     * Total number of bytes sent for this SSRC
     */
    public final long bytesSent;

    /**
     * Total number of RTP packets sent for this SSRC
     */
    public final int packetsSent;

    /**
     * Estimated round trip time for this SSRC based on the RTCP timestamps.
     * Measured in milliseconds.
     */
    public final long roundTripTime;

    public LocalTrackStats(String trackId, int packetsLost,
                           String codecName, String ssrc, double unixTimestamp,
                           long bytesSent, int packetsSent, long roundTripTime) {
        super(trackId, packetsLost, codecName, ssrc, unixTimestamp);
        this.bytesSent = bytesSent;
        this.packetsSent = packetsSent;
        this.roundTripTime = roundTripTime;
    }
}
