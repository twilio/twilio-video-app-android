package com.twilio.video;

public class LocalVideoTrackStats extends LocalTrackStats {
    /**
     * FrameWidthInput x FrameHeightInput
     */
    public final VideoDimensions captureDimensions;
    /**
     * FrameWidthSent x FrameHeightSent
     */
    public final VideoDimensions sentDimensions;
    /**
     * Frame rate sent
     */
    public final int sentFrameRate;

    /**
     * Captured frame rate
     */
    public final int capturedFrameRate;

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
