package com.twilio.conversations;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VideoConstraintsTests {

    @Test
    public void createCustomDimensions() {
        int myMinResolutionWidth = 100;
        int myMinResolutionHeight = 200;

        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .minVideoDimensions(new VideoDimensions(myMinResolutionWidth, myMinResolutionHeight))
                .build();
    }

    @Test
    public void createVideoConstraints() {
        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .minVideoDimensions(VideoConstraints.CIF_VIDEO_DIMENSIONS)
                .maxVideoDimensions(VideoConstraints.HD_720P_VIDEO_DIMENSIONS)
                .minFps(VideoConstraints.FRAME_RATE_10)
                .maxFps(VideoConstraints.FRAME_RATE_24)
                .build();

        VideoDimensions minVideoDimensions = videoConstraints.getMinVideoDimensions();
        VideoDimensions maxVideoDimensions = videoConstraints.getMaxVideoDimensions();
        int minFPS = videoConstraints.getMinFps();
        int maxFPS = videoConstraints.getMaxFps();
    }

    @Test
    public void createLocalVideoTrackWithVideoConstraints() {
        Context context = null;
        CameraCapturer cameraCapturer = null;

        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .minFps(VideoConstraints.FRAME_RATE_15)
                .maxFps(VideoConstraints.FRAME_RATE_20)
                .build();

    }

}
