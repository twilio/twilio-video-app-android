package com.twilio.conversations;


public interface RemoteAudioTrackStatsRecord extends MediaTrackStatsRecord {

    long getBytesReceived();

    long getPacketsReceived();

    int getAudioOutputLevel();

    int getJitterBuffer();

    int getJitterReceived();

}
