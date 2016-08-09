package com.twilio.video;

public class RemoteAudioTrackStatsRecord extends MediaTrackStatsRecord {
    private final long bytesReceived;
    private final long packetsReceived;
    private final int audioOutputLevel;
    private final int jitterBuffer;
    private final int jitterReceived;

    public RemoteAudioTrackStatsRecord(CoreTrackStatsReport report) {
        super(report);
        bytesReceived = report.getLongValue(CoreTrackStatsReport.KeyEnum.BYTES_RECEIVED);
        packetsReceived = report.getLongValue(CoreTrackStatsReport.KeyEnum.PACKETS_RECEIVED);
        audioOutputLevel = report.getIntValue(CoreTrackStatsReport.KeyEnum.AUDIO_OUTPUT_LEVEL);
        jitterBuffer = report.getIntValue(CoreTrackStatsReport.KeyEnum.JITTER_BUFFER_MS);
        jitterReceived = report.getIntValue(CoreTrackStatsReport.KeyEnum.JITTER_RECEIVED);
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public long getPacketsReceived() {
        return packetsReceived;
    }

    public int getAudioOutputLevel() {
        return audioOutputLevel;
    }

    public int getJitterBuffer() {
        return jitterBuffer;
    }

    public int getJitterReceived() {
        return jitterReceived;
    }
}
