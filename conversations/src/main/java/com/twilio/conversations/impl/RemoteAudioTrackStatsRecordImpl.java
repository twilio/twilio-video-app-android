package com.twilio.conversations.impl;

import com.twilio.conversations.RemoteAudioTrackStatsRecord;
import com.twilio.conversations.impl.core.CoreTrackStatsReport;

public class RemoteAudioTrackStatsRecordImpl extends MediaTrackStatsRecordImpl
        implements RemoteAudioTrackStatsRecord {

    private final long bytesReceived;
    private final long packetsReceived;
    private final int audioOutputLevel;
    private final int jitterBuffer;
    private final int jitterReceived;

    public RemoteAudioTrackStatsRecordImpl(CoreTrackStatsReport report) {
        super(report);
        bytesReceived = report.getLongValue(CoreTrackStatsReport.KeyEnum.BYTES_RECEIVED);
        packetsReceived = report.getLongValue(CoreTrackStatsReport.KeyEnum.PACKETS_RECEIVED);
        audioOutputLevel = report.getIntValue(CoreTrackStatsReport.KeyEnum.AUDIO_OUTPUT_LEVEL);
        jitterBuffer = report.getIntValue(CoreTrackStatsReport.KeyEnum.JITTER_BUFFER_MS);
        jitterReceived = report.getIntValue(CoreTrackStatsReport.KeyEnum.JITTER_RECEIVED);
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
