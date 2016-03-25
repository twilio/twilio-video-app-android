package com.twilio.conversations;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

@RunWith(AndroidJUnit4.class)
public class VideoConstraintsTests {

    @Test
    public void createCustomMinAndMaxDimensions() {
        int dummyMinWidth = 100;
        int dummyMinHeight = 200;
        int dummyMaxWidth = 100;
        int dummyMaxHeight = 200;

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

    @Test(expected = IllegalStateException.class)
    public void useInvalidMinVideoDimensions() {
        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .minVideoDimensions(null)
                .build();

    }

    @Test(expected = IllegalStateException.class)
    public void useInvalidMaxVideoDimensions() {
        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .maxVideoDimensions(null)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void useNegativeMinFps() {
        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .minFps(-100)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void useNegativeMaxFps() {
        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .maxFps(-100)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void createLocalVideoTrackWithVideoConstraints() {
        CameraCapturer cameraCapturer = null;

        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .build();

        LocalVideoTrack localVideoTrack = LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer, videoConstraints);
    }

}
