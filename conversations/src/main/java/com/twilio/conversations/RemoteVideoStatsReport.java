package com.twilio.conversations;


public interface RemoteVideoStatsReport extends TrackStatsReport {

    long getBytesReceived();

    long getPacketsReceived();

    VideoDimensions getDimensions();

    int getFrameRate();

    int getJitterBuffer();


}
