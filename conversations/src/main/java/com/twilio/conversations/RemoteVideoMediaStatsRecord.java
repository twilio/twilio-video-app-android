package com.twilio.conversations;


public interface RemoteVideoMediaStatsRecord extends MediaTrackStatsRecord {

    long getBytesReceived();

    long getPacketsReceived();

    VideoDimensions getDimensions();

    int getFrameRate();

    int getJitterBuffer();


}
