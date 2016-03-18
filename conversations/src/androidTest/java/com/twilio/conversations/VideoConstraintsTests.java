package com.twilio.conversations;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VideoConstraintsTests {

    @Test
    public void createVideoConstraints() {
        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .setMaxVideoDimensions(VideoConstraints.HD_VIDEO_DIMENSIONS)
                .setMinFPS(10)
                .setMaxFPS(VideoConstraints.MAX_VIDEO_FPS)
                .build();

        videoConstraints.getMinVideoDimensions();
        videoConstraints.getMaxVideoDimensions();
        videoConstraints.getMinFPS();
        videoConstraints.getMaxFPS();
    }

}
