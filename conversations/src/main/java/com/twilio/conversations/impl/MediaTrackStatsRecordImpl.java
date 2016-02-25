package com.twilio.conversations.impl;

import com.twilio.conversations.MediaTrackStatsRecord;
import com.twilio.conversations.impl.core.CoreTrackStatsReport;

public class MediaTrackStatsRecordImpl implements MediaTrackStatsRecord {

    private final String trackId;
    private final int packetsLost;
    private final String direction;
    private final String codecName;
    private final String ssrc;
    private final String participantSid;
    private final long unixTimestamp;

    public MediaTrackStatsRecordImpl(CoreTrackStatsReport report) {
        trackId = report.trackId;
        packetsLost = report.getIntValue(CoreTrackStatsReport.KeyEnum.PACKETS_LOST);
        direction = report.direction;
        codecName = report.codecName;
        ssrc = report.ssrc;
        participantSid = report.participantSid;
        unixTimestamp = report.timestamp;
    }

    @Override
    public String getTrackId() {
        return trackId;
    }

    @Override
    public int getPacketsLost() {
        return packetsLost;
    }

    @Override
    public String getDirection() {
        return direction;
    }

    @Override
    public String getCodecName() {
        return codecName;
    }

    @Override
    public String getSsrc() {
        return ssrc;
    }

    @Override
    public String getParticipantSid() {
        return participantSid;
    }

    @Override
    public long getUnixTimestamp() {
        return unixTimestamp;
    }
}
