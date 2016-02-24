package com.twilio.conversations.impl;

import com.twilio.conversations.RemoteAudioMediaStatsRecord;
import com.twilio.conversations.impl.core.TrackStatsReport;

public class RemoteAudioMediaStatsRecordImpl extends MediaTrackStatsRecordImpl
        implements RemoteAudioMediaStatsRecord {

    private final long bytesReceived;
    private final long packetsReceived;
    private final int audioOutputLevel;
    private final int jitterBuffer;
    private final int jitterReceived;

    public RemoteAudioMediaStatsRecordImpl(TrackStatsReport report) {
        super(report);
        bytesReceived = report.getLongValue(TrackStatsReport.KeyEnum.BYTES_RECEIVED);
        packetsReceived = report.getLongValue(TrackStatsReport.KeyEnum.PACKETS_RECEIVED);
        audioOutputLevel = report.getIntValue(TrackStatsReport.KeyEnum.AUDIO_OUTPUT_LEVEL);
        jitterBuffer = report.getIntValue(TrackStatsReport.KeyEnum.JITTER_BUFFER_MS);
        jitterReceived = report.getIntValue(TrackStatsReport.KeyEnum.JITTER_RECEIVED);
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
    public int getAudioOutputLevel() {
        return audioOutputLevel;
    }

    @Override
    public int getJitterBuffer() {
        return jitterBuffer;
    }

    @Override
    public int getJitterReceived() {
        return jitterReceived;
    }
}
