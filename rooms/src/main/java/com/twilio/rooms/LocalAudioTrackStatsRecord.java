package com.twilio.rooms;


public class LocalAudioTrackStatsRecord extends MediaTrackStatsRecord {
    private final long bytesSent;
    private final long packetsSent;
    private final int audioInputLevel;
    private final int jitterReceived;
    private final int jitter;
    private final int roundTripTime;

    public LocalAudioTrackStatsRecord(CoreTrackStatsReport report) {
        super(report);
        this.bytesSent = report.getLongValue(CoreTrackStatsReport.KeyEnum.BYTES_SENT);
        this.packetsSent = report.getLongValue(CoreTrackStatsReport.KeyEnum.PACKETS_SENT);
        audioInputLevel = report.getIntValue(CoreTrackStatsReport.KeyEnum.AUDIO_INPUT_LEVEL);
        jitterReceived = report.getIntValue(CoreTrackStatsReport.KeyEnum.JITTER_RECEIVED);
        jitter = report.getIntValue(CoreTrackStatsReport.KeyEnum.JITTER);
        roundTripTime = report.getIntValue(CoreTrackStatsReport.KeyEnum.RTT);

    }

    public long getBytesSent() {
        return bytesSent;
    }

    public long getPacketsSent() {
        return packetsSent;
    }

    public int getAudioInputLevel() {
        return audioInputLevel;
    }

    public int getJitterReceived() {
        return jitterReceived;
    }

    public int getJitter() {
        return jitter;
    }

    public int getRoundTripTime() {
        return roundTripTime;
    }
}
