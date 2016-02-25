package com.twilio.conversations;


public interface LocalAudioStatsReport extends TrackStatsReport {

    long getBytesSent();

    long getPacketsSent();

    int getAudioInputLevel();

    int getJitterReceived();

    int getJitter();

    int getRoundTripTime();

}
