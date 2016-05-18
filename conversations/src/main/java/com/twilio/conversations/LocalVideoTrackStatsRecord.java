package com.twilio.conversations;


import com.twilio.conversations.core.CoreTrackStatsReport;

public class LocalVideoTrackStatsRecord extends MediaTrackStatsRecord {
    private final long bytesSent;
    private final long packetsSent;
    private final VideoDimensions captureDimensions;
    private final VideoDimensions sentDimensions;
    private final int frameRate;
    private final int roundTripTime;

    public LocalVideoTrackStatsRecord(CoreTrackStatsReport report) {
        super(report);
        this.bytesSent = report.getLongValue(CoreTrackStatsReport.KeyEnum.BYTES_SENT);
        this.packetsSent = report.getLongValue(CoreTrackStatsReport.KeyEnum.PACKETS_SENT);
        // coerce the width and height to ensure they are non-negative values
        int width = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_WIDTH_INPUT) < 0 ?
                0 : report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_WIDTH_INPUT);
        int height = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_HEIGHT_INPUT) < 0 ?
                0 : report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_HEIGHT_INPUT);
        captureDimensions = new VideoDimensions(width, height);
        // coerce the width and height to ensure they are non-negative values
        width = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_WIDTH_SENT) < 0 ?
                0 : report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_WIDTH_SENT);
        height = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_HEIGHT_SENT) < 0
                ? 0 : report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_HEIGHT_SENT);
        sentDimensions = new VideoDimensions(width, height);
        frameRate = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_RATE_SENT);
        roundTripTime = report.getIntValue(CoreTrackStatsReport.KeyEnum.RTT);
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public long getPacketsSent() {
        return packetsSent;
    }

    public VideoDimensions getCaptureDimensions() {
        return captureDimensions;
    }

    public VideoDimensions getSentDimensions() {
        return sentDimensions;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public int getRoundTripTime() {
        return roundTripTime;
    }
}
