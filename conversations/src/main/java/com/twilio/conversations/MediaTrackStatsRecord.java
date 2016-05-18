package com.twilio.conversations;

public class MediaTrackStatsRecord {
    private final String trackId;
    private final int packetsLost;
    private final String direction;
    private final String codecName;
    private final String ssrc;
    private final String participantSid;
    private final long unixTimestamp;

    public MediaTrackStatsRecord(CoreTrackStatsReport report) {
        trackId = report.trackId;
        packetsLost = report.getIntValue(CoreTrackStatsReport.KeyEnum.PACKETS_LOST);
        direction = report.direction;
        codecName = report.codecName;
        ssrc = report.ssrc;
        participantSid = report.participantSid;
        unixTimestamp = report.timestamp;
    }

    public String getTrackId() {
        return trackId;
    }

    public int getPacketsLost() {
        return packetsLost;
    }

    public String getDirection() {
        return direction;
    }

    public String getCodecName() {
        return codecName;
    }

    public String getSsrc() {
        return ssrc;
    }

    public String getParticipantSid() {
        return participantSid;
    }

    public long getUnixTimestamp() {
        return unixTimestamp;
    }
}
