package com.twilio.conversations.impl;

import com.twilio.conversations.LocalAudioMediaStatsRecord;
import com.twilio.conversations.impl.core.TrackStatsReport;


public class LocalAudioMediaStatsRecordImpl extends MediaTrackStatsRecordImpl
        implements LocalAudioMediaStatsRecord {

    private final long bytesSent;
    private final long packetsSent;
    private final int audioInputLevel;
    private final int jitterReceived;
    private final int jitter;
    private final int roundTripTime;

    public LocalAudioMediaStatsRecordImpl(TrackStatsReport report) {
        super(report);
        this.bytesSent = report.getLongValue(TrackStatsReport.KeyEnum.BYTES_SENT);
        this.packetsSent = report.getLongValue(TrackStatsReport.KeyEnum.PACKETS_SENT);
        audioInputLevel = report.getIntValue(TrackStatsReport.KeyEnum.AUDIO_INPUT_LEVEL);
        jitterReceived = report.getIntValue(TrackStatsReport.KeyEnum.JITTER_RECEIVED);
        jitter = report.getIntValue(TrackStatsReport.KeyEnum.JITTER);
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
