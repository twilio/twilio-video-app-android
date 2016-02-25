package com.twilio.conversations;

public interface TrackStatsReport {

    String getTrackId();

    int getPacketsLost();

    String getDirection();

    String getCodecName();

    String getSsrc();

    String getParticipantSid();

    long getUnixTimestamp();
}
