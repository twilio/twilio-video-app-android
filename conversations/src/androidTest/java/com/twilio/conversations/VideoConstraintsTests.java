package com.twilio.conversations;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VideoConstraintsTests {

    @Test
    public void createVideoConstraints() {
        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .setMaxVideoDimensions(VideoConstraints.HD_720P_VIDEO_DIMENSIONS)
                .setMinFPS(VideoConstraints.BATTERY_SAVER_10_FPS)
                .setMaxFPS(VideoConstraints.CINEMATIC_24_FPS)
                .build();

        VideoDimensions minVideoDimensions = videoConstraints.getMinVideoDimensions();
        VideoDimensions maxVideoDimensions = videoConstraints.getMaxVideoDimensions();
        int minFPS = videoConstraints.getMinFPS();
        int maxFPS = videoConstraints.getMaxFPS();
    }

}
