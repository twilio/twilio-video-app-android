package com.twilio.conversations;


public interface RemoteAudioMediaStatsRecord extends MediaTrackStatsRecord {

    long getBytesReceived();

    long getPacketsReceived();

    int getAudioOutputLevel();

    int getJitterBuffer();

    int getJitterReceived();

}
