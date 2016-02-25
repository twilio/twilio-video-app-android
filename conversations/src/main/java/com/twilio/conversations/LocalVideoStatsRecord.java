package com.twilio.conversations;


public interface LocalVideoStatsRecord extends TrackStatsRecord {

    long getBytesSent();

    long getPacketsSent();

    VideoDimensions getCaptureDimensions();

    VideoDimensions getSentDimensions();

    int getFrameRate();

    int getRoundTripTime();
}
