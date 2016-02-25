package com.twilio.conversations;


public interface RemoteVideoStatsRecord extends TrackStatsRecord {

    long getBytesReceived();

    long getPacketsReceived();

    VideoDimensions getDimensions();

    int getFrameRate();

    int getJitterBuffer();


}
