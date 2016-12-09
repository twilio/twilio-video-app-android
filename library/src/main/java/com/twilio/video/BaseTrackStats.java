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
    public final String codec;

    /**
     * The SSRC identifier of the source
     */
    public final String ssrc;

    /**
     * Unix timestamp in milliseconds
     */
    public final double timestamp;

    protected BaseTrackStats(String trackId, int packetsLost,
                             String codec, String ssrc, double timestamp) {
        this.trackId = trackId;
        this.packetsLost = packetsLost;
        this.codec = codec;
        this.ssrc = ssrc;
        this.timestamp = timestamp;
    }
}
