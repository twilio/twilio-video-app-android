package com.twilio.conversations;


public interface RemoteVideoTrackStatsRecord extends MediaTrackStatsRecord {

    long getBytesReceived();

    long getPacketsReceived();

    VideoDimensions getDimensions();

    int getFrameRate();

    int getJitterBuffer();


}
