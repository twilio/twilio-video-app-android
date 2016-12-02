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
    public final VideoDimensions sentDimensions;

    /**
     * Sent frame rate
     */
    public final int sentFrameRate;

    public LocalVideoTrackStats(String trackId,
                                int packetsLost,
                                String codecName,
                                String ssrc,
                                double unixTimestamp,
                                long bytesSent,
                                int packetsSent,
                                long roundTripTime,
                                VideoDimensions captureDimensions,
                                VideoDimensions sentDimensions,
                                int capturedFrameRate,
                                int sentFrameRate) {
        super(trackId, packetsLost, codecName, ssrc, unixTimestamp,
                bytesSent, packetsSent, roundTripTime);
        this.captureDimensions = captureDimensions;
        this.sentDimensions = sentDimensions;
        this.sentFrameRate = sentFrameRate;
        this.capturedFrameRate = capturedFrameRate;
    }
}
