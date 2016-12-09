package com.twilio.video;

public class VideoTrackStats extends TrackStats {
    /**
     * Received frame dimensions
     */
    public final VideoDimensions dimensions;

    /**
     * Received frame rate
     */
    public final int frameRate;

    VideoTrackStats(String trackId,
                    int packetsLost,
                    String codec,
                    String ssrc,
                    double timestamp,
                    long bytesReceived,
                    int packetsReceived,
                    VideoDimensions dimensions,
                    int frameRate) {
        super(trackId, packetsLost, codec, ssrc,
                timestamp, bytesReceived, packetsReceived);
        this.dimensions = dimensions;
        this.frameRate = frameRate;
    }
}
