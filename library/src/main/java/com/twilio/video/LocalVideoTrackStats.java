package com.twilio.video;

public class LocalVideoTrackStats extends LocalTrackStats {
    /**
     * Captured frame dimensions
     */
    public final VideoDimensions captureDimensions;

    /**
     * Captured frame rate
     */
    public final int capturedFrameRate;

    /**
     * Sent frame dimensions
     */
    public final VideoDimensions dimensions;

    /**
     * Sent frame rate
     */
    public final int frameRate;

    LocalVideoTrackStats(String trackId,
                         int packetsLost,
                         String codec,
                         String ssrc,
                         double timestamp,
                         long bytesSent,
                         int packetsSent,
                         long roundTripTime,
                         VideoDimensions captureDimensions,
                         VideoDimensions dimensions,
                         int capturedFrameRate,
                         int frameRate) {
        super(trackId, packetsLost, codec, ssrc, timestamp,
                bytesSent, packetsSent, roundTripTime);
        this.captureDimensions = captureDimensions;
        this.dimensions = dimensions;
        this.frameRate = frameRate;
        this.capturedFrameRate = capturedFrameRate;
    }
}
