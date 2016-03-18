package com.twilio.conversations;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VideoConstraintsTests {

    @Test
    public void createVideoConstraints() {
        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .setMaxVideoDimensions(VideoConstraints.HD_VIDEO_WIDTH, VideoConstraints.HD_VIDEO_HEIGHT)
                .setMaxVideoDimensions(VideoConstraints.HD_VIDEO_DIMENSIONS)
                .setMinFPS(VideoConstraints.MIN_VIDEO_FPS)
                .setMaxFPS(VideoConstraints.MAX_VIDEO_FPS)
                .build();

        videoConstraints.getMinVideoWidth();
        videoConstraints.getMinVideoHeight();
        videoConstraints.getMaxVideoWidth();
        videoConstraints.getMaxVideoHeight();
        videoConstraints.getMinFPS();
        videoConstraints.getMaxFPS();

    }
}
