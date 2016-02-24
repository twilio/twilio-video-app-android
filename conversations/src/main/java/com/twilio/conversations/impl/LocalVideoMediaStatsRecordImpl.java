package com.twilio.conversations.impl;

import com.twilio.conversations.LocalVideoMediaStatsRecord;
import com.twilio.conversations.VideoDimensions;
import com.twilio.conversations.impl.core.TrackStatsReport;


public class LocalVideoMediaStatsRecordImpl extends MediaTrackStatsRecordImpl
        implements LocalVideoMediaStatsRecord {

    private final long bytesSent;
    private final long packetsSent;
    private final VideoDimensions captureDimensions;
    private final VideoDimensions sentDimensions;
    private final int frameRate;
    private final int roundTripTime;

    public LocalVideoMediaStatsRecordImpl(TrackStatsReport report) {
        super(report);
        this.bytesSent = report.getLongValue(TrackStatsReport.KeyEnum.BYTES_SENT);
        this.packetsSent = report.getLongValue(TrackStatsReport.KeyEnum.PACKETS_SENT);
        int width = report.getIntValue(TrackStatsReport.KeyEnum.FRAME_WIDTH_INPUT);
        int height = report.getIntValue(TrackStatsReport.KeyEnum.FRAME_HEIGHT_INPUT);
        captureDimensions = new VideoDimensions(width, height);
        width = report.getIntValue(TrackStatsReport.KeyEnum.FRAME_WIDTH_SENT);
        height = report.getIntValue(TrackStatsReport.KeyEnum.FRAME_HEIGHT_SENT);
        sentDimensions = new VideoDimensions(width, height);
        frameRate = report.getIntValue(TrackStatsReport.KeyEnum.FRAME_RATE_SENT);
        roundTripTime = report.getIntValue(TrackStatsReport.KeyEnum.RTT);

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
