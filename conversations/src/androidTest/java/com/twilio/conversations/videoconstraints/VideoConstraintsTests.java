package com.twilio.conversations.videoconstraints;

import android.support.test.runner.AndroidJUnit4;

import com.twilio.conversations.VideoConstraints;
import com.twilio.conversations.VideoDimensions;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

@RunWith(AndroidJUnit4.class)
public class VideoConstraintsTests {

    @Test
    public void createCustomMinAndMaxDimensions() {
        int dummyMinWidth = 100;
        int dummyMinHeight = 200;
        int dummyMaxWidth = 300;
        int dummyMaxHeight = 400;

        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .minVideoDimensions(new VideoDimensions(dummyMinWidth, dummyMinHeight))
                .maxVideoDimensions(new VideoDimensions(dummyMaxWidth, dummyMaxHeight))
                .build();

        assertEquals(dummyMinWidth, videoConstraints.getMinVideoDimensions().width);
        assertEquals(dummyMinHeight, videoConstraints.getMinVideoDimensions().height);
        assertEquals(dummyMaxWidth, videoConstraints.getMaxVideoDimensions().width);
        assertEquals(dummyMaxHeight, videoConstraints.getMaxVideoDimensions().height);
    }

    @Test
    public void createVideoConstraints() {
        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .minVideoDimensions(VideoConstraints.CIF_VIDEO_DIMENSIONS)
                .maxVideoDimensions(VideoConstraints.HD_720P_VIDEO_DIMENSIONS)
                .minFps(VideoConstraints.FRAME_RATE_10)
                .maxFps(VideoConstraints.FRAME_RATE_24)
                .build();

        assertEquals(VideoConstraints.CIF_VIDEO_DIMENSIONS, videoConstraints.getMinVideoDimensions());
        assertEquals(VideoConstraints.HD_720P_VIDEO_DIMENSIONS, videoConstraints.getMaxVideoDimensions());
        assertEquals(VideoConstraints.FRAME_RATE_10, videoConstraints.getMinFps());
        assertEquals(VideoConstraints.FRAME_RATE_24, videoConstraints.getMaxFps());
    }

    @Test
    public void startConversationWithVideoConstraints() {

    }
}
