package com.twilio.conversations;


public interface LocalVideoTrackStatsRecord extends MediaTrackStatsRecord {

    long getBytesSent();

    long getPacketsSent();

    VideoDimensions getCaptureDimensions();

    VideoDimensions getSentDimensions();

    int getFrameRate();

    int getRoundTripTime();
}
