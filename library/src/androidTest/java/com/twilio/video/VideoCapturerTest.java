/*
 * Copyright (C) 2018 Twilio, Inc.
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

import android.support.test.InstrumentationRegistry;

import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(JUnitParamsRunner.class)
public class VideoCapturerTest {
    private LocalVideoTrack localVideoTrack;
    private I420Frame i420Frame;
    private long nativeVideoSinkHandle;

    @After
    public void tearDown() {
        if (i420Frame != null) {
            i420Frame.release();
        }
        if (localVideoTrack != null) {
            localVideoTrack.removeRendererWithWants(nativeVideoSinkHandle);
            localVideoTrack.release();
        }
    }

    @Test
    @Parameters
    public void shouldCaptureAndRenderFrame(VideoPixelFormat videoPixelFormat,
                                            VideoFrame.RotationAngle rotationAngle)
            throws InterruptedException {
        List<VideoFormat> videoFormats = Collections
                .singletonList(new VideoFormat(VideoDimensions.VGA_VIDEO_DIMENSIONS,
                        30, videoPixelFormat));
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer(videoFormats, rotationAngle);
        localVideoTrack = LocalVideoTrack.create(InstrumentationRegistry.getContext(),
                true, fakeVideoCapturer);
        final CountDownLatch frameRendered = new CountDownLatch(1);
        final AtomicReference<I420Frame> frameReference = new AtomicReference<>();
        boolean rotationApplied = rotationAngle != VideoFrame.RotationAngle.ROTATION_0;
        boolean dimensionsSwapped = rotationAngle == VideoFrame.RotationAngle.ROTATION_90 ||
                rotationAngle == VideoFrame.RotationAngle.ROTATION_270;
        int expectedWidth = dimensionsSwapped ?
                VideoDimensions.VGA_VIDEO_DIMENSIONS.height :
                VideoDimensions.VGA_VIDEO_DIMENSIONS.width;
        int expectedHeight = dimensionsSwapped ?
                VideoDimensions.VGA_VIDEO_DIMENSIONS.width :
                VideoDimensions.VGA_VIDEO_DIMENSIONS.height;
        VideoFrame.RotationAngle expectedRotation = rotationApplied ?
                VideoFrame.RotationAngle.ROTATION_0 :
                rotationAngle;

        // Add renderer and wait for frame
        this.nativeVideoSinkHandle = localVideoTrack.addRendererWithWants(new VideoRenderer() {
            @Override
            public void renderFrame(I420Frame frame) {
                frameReference.set(frame);
                frameRendered.countDown();
            }
        }, rotationApplied);
        assertTrue(frameRendered.await(10, TimeUnit.SECONDS));
        this.i420Frame = frameReference.get();

        // Validate frame
        assertEquals(expectedWidth, i420Frame.width);
        assertEquals(expectedHeight, i420Frame.height);
        assertEquals(expectedRotation.getValue(), i420Frame.rotationDegree);
    }

    /*
     * Used to supply parameters to test
     */
    @SuppressWarnings("unused")
    private Object[] parametersForShouldCaptureAndRenderFrame() {
        return new Object[]{
                new Object[]{VideoPixelFormat.NV21, VideoFrame.RotationAngle.ROTATION_0},
                new Object[]{VideoPixelFormat.NV21, VideoFrame.RotationAngle.ROTATION_90},
                new Object[]{VideoPixelFormat.NV21, VideoFrame.RotationAngle.ROTATION_180},
                new Object[]{VideoPixelFormat.NV21, VideoFrame.RotationAngle.ROTATION_270},
                new Object[]{VideoPixelFormat.RGBA_8888, VideoFrame.RotationAngle.ROTATION_0},
                new Object[]{VideoPixelFormat.RGBA_8888, VideoFrame.RotationAngle.ROTATION_90},
                new Object[]{VideoPixelFormat.RGBA_8888, VideoFrame.RotationAngle.ROTATION_180},
                new Object[]{VideoPixelFormat.RGBA_8888, VideoFrame.RotationAngle.ROTATION_270}
        };
    }
}
