package com.twilio.conversations;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VideoConstraintsTests {

    @Test
    public void createVideoConstraints() {
        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .maxVideoDimensions(VideoConstraints.HD_720P_VIDEO_DIMENSIONS)
                .minFps(VideoConstraints.BATTERY_SAVER_10_FPS)
                .maxFps(VideoConstraints.CINEMATIC_24_FPS)
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
                .minFps(VideoConstraints.BATTERY_EFFICIENT_15_FPS)
                .maxFps(VideoConstraints.BATTERY_EFFICIENT_20_FPS)
                .build();

        LocalVideoTrack localVideoTrack = LocalVideoTrackFactory.createLocalVideoTrack(
                cameraCapturer,
                videoConstraints);

    }

}
