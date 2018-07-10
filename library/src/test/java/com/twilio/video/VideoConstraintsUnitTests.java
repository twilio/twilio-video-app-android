/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import static com.twilio.video.AspectRatio.ASPECT_RATIO_4_3;
import static junit.framework.Assert.*;

import org.junit.Test;

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

        VideoConstraints videoConstraints =
                new VideoConstraints.Builder()
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
        VideoConstraints videoConstraints =
                new VideoConstraints.Builder()
                        .minVideoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS)
                        .maxVideoDimensions(VideoDimensions.HD_720P_VIDEO_DIMENSIONS)
                        .minFps(VideoConstraints.FPS_10)
                        .maxFps(VideoConstraints.FPS_24)
                        .aspectRatio(ASPECT_RATIO_4_3)
                        .build();

        assertEquals(
                VideoDimensions.CIF_VIDEO_DIMENSIONS, videoConstraints.getMinVideoDimensions());
        assertEquals(
                VideoDimensions.HD_720P_VIDEO_DIMENSIONS, videoConstraints.getMaxVideoDimensions());
        assertEquals(VideoConstraints.FPS_10, videoConstraints.getMinFps());
        assertEquals(VideoConstraints.FPS_24, videoConstraints.getMaxFps());
        assertEquals(ASPECT_RATIO_4_3, videoConstraints.getAspectRatio());
    }

    @Test(expected = NullPointerException.class)
    public void useInvalidMinVideoDimensions() {
        new VideoConstraints.Builder().minVideoDimensions(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void useInvalidMaxVideoDimensions() {
        new VideoConstraints.Builder().maxVideoDimensions(null).build();
    }

    @Test(expected = IllegalStateException.class)
    public void useNegativeMinFps() {
        new VideoConstraints.Builder().minFps(-100).build();
    }

    @Test(expected = IllegalStateException.class)
    public void useNegativeMaxFps() {
        new VideoConstraints.Builder().maxFps(-100).build();
    }

    @Test(expected = IllegalStateException.class)
    public void localVideoTrackWithNegativeMinAndMaxFps() {
        new VideoConstraints.Builder().minFps(-1).maxFps(-1).build();
    }

    @Test(expected = IllegalStateException.class)
    public void useInvalidFpsRange() {
        new VideoConstraints.Builder().minFps(20).maxFps(10).build();
    }

    @Test(expected = IllegalStateException.class)
    public void useNegativeAspectRatio() {
        new VideoConstraints.Builder().aspectRatio(new AspectRatio(-1, -2)).build();
    }

    @Test(expected = IllegalStateException.class)
    public void useInvalidDimensionsRange() {
        new VideoConstraints.Builder()
                .minVideoDimensions(VideoDimensions.HD_1080P_VIDEO_DIMENSIONS)
                .maxVideoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS)
                .build();
    }
}
