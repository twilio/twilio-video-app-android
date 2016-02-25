package com.twilio.conversations.impl;

import com.twilio.conversations.LocalVideoStatsRecord;
import com.twilio.conversations.VideoDimensions;
import com.twilio.conversations.impl.core.CoreTrackStatsReport;


public class LocalVideoStatsRecordImpl extends TrackStatsRecordImpl
        implements LocalVideoStatsRecord {

    private final long bytesSent;
    private final long packetsSent;
    private final VideoDimensions captureDimensions;
    private final VideoDimensions sentDimensions;
    private final int frameRate;
    private final int roundTripTime;

    public LocalVideoStatsRecordImpl(CoreTrackStatsReport report) {
        super(report);
        this.bytesSent = report.getLongValue(CoreTrackStatsReport.KeyEnum.BYTES_SENT);
        this.packetsSent = report.getLongValue(CoreTrackStatsReport.KeyEnum.PACKETS_SENT);
        int width = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_WIDTH_INPUT);
        int height = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_HEIGHT_INPUT);
        captureDimensions = new VideoDimensions(width, height);
        width = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_WIDTH_SENT);
        height = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_HEIGHT_SENT);
        sentDimensions = new VideoDimensions(width, height);
        frameRate = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_RATE_SENT);
        roundTripTime = report.getIntValue(CoreTrackStatsReport.KeyEnum.RTT);

    }
    @Override
    public long getBytesSent() {
        return bytesSent;
    }

    @Override
    public long getPacketsSent() {
        return packetsSent;
    }

    @Override
    public VideoDimensions getCaptureDimensions() {
        return captureDimensions;
    }

    @Override
    public VideoDimensions getSentDimensions() {
        return sentDimensions;
    }

    @Override
    public int getFrameRate() {
        return frameRate;
    }

    @Override
    public int getRoundTripTime() {
        return roundTripTime;
    }
}
