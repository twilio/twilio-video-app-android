package com.twilio.conversations.impl;

import com.twilio.conversations.LocalAudioStatsRecord;
import com.twilio.conversations.impl.core.CoreTrackStatsReport;


public class LocalAudioStatsRecordImpl extends TrackStatsRecordImpl
        implements LocalAudioStatsRecord {

    private final long bytesSent;
    private final long packetsSent;
    private final int audioInputLevel;
    private final int jitterReceived;
    private final int jitter;
    private final int roundTripTime;

    public LocalAudioStatsRecordImpl(CoreTrackStatsReport report) {
        super(report);
        this.bytesSent = report.getLongValue(CoreTrackStatsReport.KeyEnum.BYTES_SENT);
        this.packetsSent = report.getLongValue(CoreTrackStatsReport.KeyEnum.PACKETS_SENT);
        audioInputLevel = report.getIntValue(CoreTrackStatsReport.KeyEnum.AUDIO_INPUT_LEVEL);
        jitterReceived = report.getIntValue(CoreTrackStatsReport.KeyEnum.JITTER_RECEIVED);
        jitter = report.getIntValue(CoreTrackStatsReport.KeyEnum.JITTER);
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
    public int getAudioInputLevel() {
        return audioInputLevel;
    }

    @Override
    public int getJitterReceived() {
        return jitterReceived;
    }

    @Override
    public int getJitter() {
        return jitter;
    }

    @Override
    public int getRoundTripTime() {
        return roundTripTime;
    }
}
