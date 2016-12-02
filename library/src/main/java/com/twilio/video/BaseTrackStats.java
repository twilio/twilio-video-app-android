package com.twilio.video;

public abstract class BaseTrackStats {
    /**
     * Track identifier
     */
    public final String trackId;

    /**
     * Total number of RTP packets lost for this SSRC since
     * the beginning of the reception.
     */
    public final int packetsLost;

    /**
     * Name of codec used for this track
     */
    public final String codecName;

    /**
     * The SSRC identifier of the source
     */
    public final String ssrc;

    /**
     * Unix timestamp in milliseconds
     */
    public final double unixTimestamp;

    public BaseTrackStats(String trackId, int packetsLost,
                          String codecName, String ssrc, double unixTimestamp) {
        this.trackId = trackId;
        this.packetsLost = packetsLost;
        this.codecName = codecName;
        this.ssrc = ssrc;
        this.unixTimestamp = unixTimestamp;
    }
}
