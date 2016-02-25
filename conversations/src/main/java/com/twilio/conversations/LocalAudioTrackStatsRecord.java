package com.twilio.conversations;


public interface LocalAudioTrackStatsRecord extends MediaTrackStatsRecord {

    long getBytesSent();

    long getPacketsSent();

    int getAudioInputLevel();

    int getJitterReceived();

    int getJitter();

    int getRoundTripTime();

}
