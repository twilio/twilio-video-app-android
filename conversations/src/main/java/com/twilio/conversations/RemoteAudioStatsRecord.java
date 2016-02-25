package com.twilio.conversations;


public interface RemoteAudioStatsRecord extends TrackStatsRecord {

    long getBytesReceived();

    long getPacketsReceived();

    int getAudioOutputLevel();

    int getJitterBuffer();

    int getJitterReceived();

}
