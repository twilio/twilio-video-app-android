package com.twilio.conversations;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

public class VideoConstraintsUnitTests {

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

    @Test(expected = NullPointerException.class)
    public void useInvalidMinVideoDimensions() {
        new VideoConstraints.Builder()
                .minVideoDimensions(null)
                .build();

    }

    @Test(expected = NullPointerException.class)
    public void useInvalidMaxVideoDimensions() {
        new VideoConstraints.Builder()
                .maxVideoDimensions(null)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void useNegativeMinFps() {
        new VideoConstraints.Builder()
                .minFps(-100)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void useNegativeMaxFps() {
        new VideoConstraints.Builder()
                .maxFps(-100)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void useInvalidFpsRange() {
        new VideoConstraints.Builder()
                .minFps(20)
                .maxFps(10)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void useInvalidDimensionsRange() {
        new VideoConstraints.Builder()
                .minVideoDimensions(VideoConstraints.HD_1080P_VIDEO_DIMENSIONS)
                .maxVideoDimensions(VideoConstraints.CIF_VIDEO_DIMENSIONS)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void localVideoTrackWithNullCameraCapturer() {
        CameraCapturer cameraCapturer = null;

        LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer);
    }

    @Test(expected = NullPointerException.class)
    public void localVideoTrackWithNullCameraCapturer2() {
        CameraCapturer cameraCapturer = null;

        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .build();

        LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer, videoConstraints);
    }

}
