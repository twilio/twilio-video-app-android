package com.twilio.conversations;


public interface LocalAudioMediaStatsRecord extends MediaTrackStatsRecord {

    long getBytesSent();

    long getPacketsSent();

    int getAudioInputLevel();

    int getJitterReceived();

    int getJitter();

    int getRoundTripTime();

}
