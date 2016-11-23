package com.twilio.video;

public abstract class BaseTrackStats {

    public BaseTrackStats(String trackId, int packetsLost, String direction,
                          String codecName, String ssrc, long unixTimestamp) {
        this.trackId = trackId;
        this.packetsLost = packetsLost;
        this.direction = direction;
        this.codecName = codecName;
        this.ssrc = ssrc;
        this.unixTimestamp = unixTimestamp;
    }

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
     * Track direction sending/receiving
     */
    public final String direction;
    /**
     * Name of codec used for this track
     */
    public final String codecName;
    /**
     * The SSRC identifier of the source
     */
    public final String ssrc;
    public final long unixTimestamp;
}
