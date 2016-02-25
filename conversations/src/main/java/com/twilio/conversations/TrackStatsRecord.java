package com.twilio.conversations;

public interface TrackStatsRecord {

    String getTrackId();

    int getPacketsLost();

    String getDirection();

    String getCodecName();

    String getSsrc();

    String getParticipantSid();

    long getUnixTimestamp();
}
