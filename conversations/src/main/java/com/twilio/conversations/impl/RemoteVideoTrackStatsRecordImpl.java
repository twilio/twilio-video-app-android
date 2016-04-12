package com.twilio.conversations.impl;

import com.twilio.conversations.RemoteVideoTrackStatsRecord;
import com.twilio.conversations.VideoDimensions;
import com.twilio.conversations.impl.core.CoreTrackStatsReport;

public class RemoteVideoTrackStatsRecordImpl extends MediaTrackStatsRecordImpl
        implements RemoteVideoTrackStatsRecord {

    private final long bytesReceived;
    private final long packetsReceived;
    private final VideoDimensions dimensions;
    private final int frameRate;
    private final int jitterBuffer;

    public RemoteVideoTrackStatsRecordImpl(CoreTrackStatsReport report) {
        super(report);
        bytesReceived = report.getLongValue(CoreTrackStatsReport.KeyEnum.BYTES_RECEIVED);
        packetsReceived = report.getLongValue(CoreTrackStatsReport.KeyEnum.PACKETS_RECEIVED);
        // coerce the width and height to ensure they are non-negative values
        int width = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_WIDTH_RECEIVED) < 0 ? 0 : report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_WIDTH_RECEIVED);
        int height = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_HEIGHT_RECEIVED) < 0 ? 0 : report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_HEIGHT_RECEIVED);
        dimensions = new VideoDimensions(width, height);
        frameRate = report.getIntValue(CoreTrackStatsReport.KeyEnum.FRAME_RATE_RECEIVED);
        jitterBuffer = report.getIntValue(CoreTrackStatsReport.KeyEnum.JITTER_BUFFER_MS);
    }

    @Override
    public long getBytesReceived() {
        return bytesReceived;
    }

    @Override
    public long getPacketsReceived() {
        return packetsReceived;
    }

    @Override
    public VideoDimensions getDimensions() {
        return dimensions;
    }

    @Override
    public int getFrameRate() {
        return frameRate;
    }

    @Override
    public int getJitterBuffer() {
        return jitterBuffer;
    }
}
