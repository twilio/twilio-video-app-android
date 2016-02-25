package com.twilio.conversations;


public interface LocalVideoStatsReport extends TrackStatsReport {

    long getBytesSent();

    long getPacketsSent();

    VideoDimensions getCaptureDimensions();

    VideoDimensions getSentDimensions();

    int getFrameRate();

    int getRoundTripTime();
}
