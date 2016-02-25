package com.twilio.conversations;


public interface LocalAudioStatsRecord extends TrackStatsRecord {

    long getBytesSent();

    long getPacketsSent();

    int getAudioInputLevel();

    int getJitterReceived();

    int getJitter();

    int getRoundTripTime();

}
