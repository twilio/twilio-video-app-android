package com.twilio.video;

import org.junit.Test;

import static junit.framework.Assert.*;

public class VideoConstraintsUnitTests {

    @Test(expected = IllegalStateException.class)
    public void negativeWidthVideoDimensions() {
        new VideoDimensions(-1, 5);
    }

    @Test(expected = IllegalStateException.class)
    public void negativeHeightVideoDimensions() {
        new VideoDimensions(1, -1);
    }

    @Test(expected = IllegalStateException.class)
    public void negativeWidthAndHeightVideoDimensions() {
        new VideoDimensions(-1, -1);
    }

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
                .minVideoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS)
                .maxVideoDimensions(VideoDimensions.HD_720P_VIDEO_DIMENSIONS)
                .minFps(VideoConstraints.FPS_10)
                .maxFps(VideoConstraints.FPS_24)
                .aspectRatio(VideoConstraints.ASPECT_RATIO_4_3)
                .build();

        assertEquals(VideoDimensions.CIF_VIDEO_DIMENSIONS, videoConstraints.getMinVideoDimensions());
        assertEquals(VideoDimensions.HD_720P_VIDEO_DIMENSIONS, videoConstraints.getMaxVideoDimensions());
        assertEquals(VideoConstraints.FPS_10, videoConstraints.getMinFps());
        assertEquals(VideoConstraints.FPS_24, videoConstraints.getMaxFps());
        assertEquals(VideoConstraints.ASPECT_RATIO_4_3, videoConstraints.getAspectRatio());
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
    public void localVideoTrackWithNegativeMinAndMaxFps() {
        new VideoConstraints.Builder()
                .minFps(-1)
                .maxFps(-1)
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
    public void useNegativeAspectRatio() {
        new VideoConstraints.Builder()
                .aspectRatio(new AspectRatio(-1, -2))
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void useInvalidDimensionsRange() {
        new VideoConstraints.Builder()
                .minVideoDimensions(VideoDimensions.HD_1080P_VIDEO_DIMENSIONS)
                .maxVideoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS)
                .build();
    }
}
