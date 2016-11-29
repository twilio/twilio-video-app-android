package com.twilio.video;

public class VideoTrackStats extends TrackStats {
    public final VideoDimensions receivedDimensions;
    public final int receivedFrameRate;

    public VideoTrackStats(String trackId,
                           int packetsLost,
                           String codecName,
                           String ssrc,
                           double unixTimestamp,
                           long bytesReceived,
                           int packetsReceived,
                           int jitterBuffer,
                           VideoDimensions receivedDimensions,
                           int receivedFrameRate) {
        super(trackId, packetsLost, codecName, ssrc,
                unixTimestamp, bytesReceived, packetsReceived, jitterBuffer);
        this.receivedDimensions = receivedDimensions;
        this.receivedFrameRate = receivedFrameRate;
    }
}
