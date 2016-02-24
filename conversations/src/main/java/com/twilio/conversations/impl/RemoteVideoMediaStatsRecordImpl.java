package com.twilio.conversations.impl;

import com.twilio.conversations.RemoteVideoMediaStatsRecord;
import com.twilio.conversations.VideoDimensions;
import com.twilio.conversations.impl.core.TrackStatsReport;

public class RemoteVideoMediaStatsRecordImpl extends MediaTrackStatsRecordImpl
        implements RemoteVideoMediaStatsRecord {

    private final long bytesReceived;
    private final long packetsReceived;
    private final VideoDimensions dimensions;
    private final int frameRate;
    private final int jitterBuffer;

    public RemoteVideoMediaStatsRecordImpl(TrackStatsReport report) {
        super(report);
        bytesReceived = report.getLongValue(TrackStatsReport.KeyEnum.BYTES_RECEIVED);
        packetsReceived = report.getLongValue(TrackStatsReport.KeyEnum.PACKETS_RECEIVED);
        int width = report.getIntValue(TrackStatsReport.KeyEnum.FRAME_WIDTH_RECEIVED);
        int height = report.getIntValue(TrackStatsReport.KeyEnum.FRAME_HEIGHT_RECEIVED);
        dimensions = new VideoDimensions(width, height);
        frameRate = report.getIntValue(TrackStatsReport.KeyEnum.FRAME_RATE_RECEIVED);
        jitterBuffer = report.getIntValue(TrackStatsReport.KeyEnum.JITTER_BUFFER_MS);
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
