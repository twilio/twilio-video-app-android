package com.twilio.video;

public abstract class RemoteTrackStats extends BaseTrackStats {
    /**
     * Total number of bytes received
     */
    public final long bytesReceived;

    /**
     * Total number of packets received
     */
    public final int packetsReceived;


    protected RemoteTrackStats(String trackId,
                               int packetsLost,
                               String codec,
                               String ssrc,
                               double timestamp,
                               long bytesReceived,
                               int packetsReceived) {
        super(trackId, packetsLost, codec, ssrc, timestamp);
        this.bytesReceived = bytesReceived;
        this.packetsReceived = packetsReceived;
    }
}
