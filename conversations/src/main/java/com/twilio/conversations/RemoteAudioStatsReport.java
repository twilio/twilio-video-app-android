package com.twilio.conversations;


public interface RemoteAudioStatsReport extends TrackStatsReport {

    long getBytesReceived();

    long getPacketsReceived();

    int getAudioOutputLevel();

    int getJitterBuffer();

    int getJitterReceived();

}
