package com.twilio.video;

public abstract class TrackStats extends BaseTrackStats {
    public final long bytesReceived;
    public final int packetsReceived;
    public final int jitterBuffer;

    public TrackStats(String trackId, int packetsLost,
                      String codecName, String ssrc, double unixTimestamp,
                      long bytesReceived, int packetsReceived, int jitterBuffer) {
        super(trackId, packetsLost, codecName, ssrc, unixTimestamp);
        this.bytesReceived = bytesReceived;
        this.packetsReceived = packetsReceived;
        this.jitterBuffer = jitterBuffer;
    }
}
