package com.twilio.video;

public abstract class LocalTrackStats extends BaseTrackStats {

    public LocalTrackStats(String trackId, int packetsLost, String direction,
                           String codecName, String ssrc, long unixTimestamp,
                           long bytesSent, long packetsSent, int roundTripTime) {
        super(trackId, packetsLost, direction, codecName, ssrc, unixTimestamp);
        this.bytesSent = bytesSent;
        this.packetsSent = packetsSent;
        this.roundTripTime = roundTripTime;
    }

    /**
     * Total number of bytes sent for this SSRC
     */
    public final long bytesSent;
    /**
     * Total number of RTP packets sent for this SSRC
     */
    public final long packetsSent;
    /**
     * Estimated round trip time for this SSRC based on the RTCP timestamps.
     * Measured in milliseconds.
     */
    public final int roundTripTime;
}
