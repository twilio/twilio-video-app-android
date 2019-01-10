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

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.Manifest;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import com.twilio.video.base.BaseCameraCapturerTest;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.util.FakeVideoCapturer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class VideoCapturerTest extends BaseVideoTest {
    private LocalVideoTrack localVideoTrack;
    private I420Frame i420Frame;
    private long nativeVideoSinkHandle;

    @Rule
    public GrantPermissionRule cameraPermissionsRule =
            GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Before
    public void setup() throws InterruptedException {
        super.setup();
    }

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
    public void shouldCaptureAndRenderFrame(
            VideoPixelFormat videoPixelFormat, VideoFrame.RotationAngle rotationAngle)
            throws InterruptedException {
        List<VideoFormat> videoFormats =
                Collections.singletonList(
                        new VideoFormat(
                                VideoDimensions.VGA_VIDEO_DIMENSIONS, 30, videoPixelFormat));
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer(videoFormats, rotationAngle);
        localVideoTrack =
                LocalVideoTrack.create(
                        InstrumentationRegistry.getContext(), true, fakeVideoCapturer);
        final CountDownLatch frameRendered = new CountDownLatch(1);
        final AtomicReference<I420Frame> frameReference = new AtomicReference<>();
        boolean rotationApplied = rotationAngle != VideoFrame.RotationAngle.ROTATION_0;
        boolean dimensionsSwapped =
                rotationAngle == VideoFrame.RotationAngle.ROTATION_90
                        || rotationAngle == VideoFrame.RotationAngle.ROTATION_270;
        int expectedWidth =
                dimensionsSwapped
                        ? VideoDimensions.VGA_VIDEO_DIMENSIONS.height
                        : VideoDimensions.VGA_VIDEO_DIMENSIONS.width;
        int expectedHeight =
                dimensionsSwapped
                        ? VideoDimensions.VGA_VIDEO_DIMENSIONS.width
                        : VideoDimensions.VGA_VIDEO_DIMENSIONS.height;
        VideoFrame.RotationAngle expectedRotation =
                rotationApplied ? VideoFrame.RotationAngle.ROTATION_0 : rotationAngle;

        // Add renderer and wait for frame
        this.nativeVideoSinkHandle =
                localVideoTrack.addRendererWithWants(
                        frame -> {
                            frameReference.set(frame);
                            frameRendered.countDown();
                        },
                        rotationApplied);
        assertTrue(frameRendered.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        this.i420Frame = frameReference.get();

        // Validate frame
        assertEquals(expectedWidth, i420Frame.width);
        assertEquals(expectedHeight, i420Frame.height);
        assertEquals(expectedRotation.getValue(), i420Frame.rotationDegree);
    }

    /*
     * After WebRTC 67 upgrade, CameraCapturer, Camera2Capturer, and ScreenCapturer execute
     * a different code path for signaling captured frames. This test validates the same behavior
     * as shouldCaptureAndRenderFrame using CameraCapturer (GSDK-1632). These two tests should
     * converge once GSDK-1629 is planned and implemented.
     */
    @Test
    @Parameters({"false", "true"})
    public void shouldCaptureAndRenderFrame2(final boolean rotationApplied)
            throws InterruptedException {
        Context context = InstrumentationRegistry.getContext();
        CameraCapturer.CameraSource cameraSource =
                BaseCameraCapturerTest.getSupportedCameraSource();
        assumeTrue("No camera source available", cameraSource != null);
        final AtomicReference<VideoFrame.RotationAngle> rotationAngle =
                new AtomicReference<>(VideoFrame.RotationAngle.ROTATION_0);
        final CountDownLatch frameRendered = new CountDownLatch(1);
        final CountDownLatch rotationSet = new CountDownLatch(1);
        final AtomicReference<I420Frame> frameReference = new AtomicReference<>();
        final AtomicReference<VideoFormat> videoFormatReference = new AtomicReference<>();

        class CameraCapturerProxy extends CameraCapturer {
            private CameraCapturerProxy(
                    @NonNull Context context, @NonNull CameraSource cameraSource) {
                super(context, cameraSource);
            }

            @Override
            public void startCapture(
                    @NonNull VideoFormat captureFormat,
                    @NonNull VideoCapturer.Listener videoCapturerListener) {
                videoFormatReference.set(captureFormat);
                super.startCapture(
                        captureFormat,
                        new VideoCapturer.Listener() {
                            @Override
                            public void onCapturerStarted(boolean success) {
                                videoCapturerListener.onCapturerStarted(success);
                            }

                            @Override
                            public void onFrameCaptured(VideoFrame videoFrame) {
                                rotationAngle.set(videoFrame.orientation);
                                rotationSet.countDown();
                                videoCapturerListener.onFrameCaptured(videoFrame);
                            }
                        });
            }
        }
        CameraCapturerProxy cameraCapturerProxy = new CameraCapturerProxy(context, cameraSource);
        localVideoTrack = LocalVideoTrack.create(context, true, cameraCapturerProxy);

        // Wait for rotation
        assertTrue(rotationSet.await(10000, TimeUnit.MILLISECONDS));

        // Establish expected dimensions after rotation set by capturer
        boolean dimensionsSwapped =
                rotationAngle.get() == VideoFrame.RotationAngle.ROTATION_90
                        || rotationAngle.get() == VideoFrame.RotationAngle.ROTATION_270;
        VideoFormat videoFormat = videoFormatReference.get();
        int expectedWidth =
                dimensionsSwapped && rotationApplied
                        ? videoFormat.dimensions.height
                        : videoFormat.dimensions.width;
        int expectedHeight =
                dimensionsSwapped && rotationApplied
                        ? videoFormat.dimensions.width
                        : videoFormat.dimensions.height;
        VideoFrame.RotationAngle expectedRotation =
                rotationApplied ? VideoFrame.RotationAngle.ROTATION_0 : rotationAngle.get();

        // Add renderer and wait for frame
        this.nativeVideoSinkHandle =
                localVideoTrack.addRendererWithWants(
                        frame -> {
                            frameReference.set(frame);
                            frameRendered.countDown();
                        },
                        rotationApplied);
        assertTrue(frameRendered.await(10000, TimeUnit.MILLISECONDS));
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
        return new Object[] {
            new Object[] {VideoPixelFormat.NV21, VideoFrame.RotationAngle.ROTATION_0},
            new Object[] {VideoPixelFormat.NV21, VideoFrame.RotationAngle.ROTATION_90},
            new Object[] {VideoPixelFormat.NV21, VideoFrame.RotationAngle.ROTATION_180},
            new Object[] {VideoPixelFormat.NV21, VideoFrame.RotationAngle.ROTATION_270},
            new Object[] {VideoPixelFormat.RGBA_8888, VideoFrame.RotationAngle.ROTATION_0},
            new Object[] {VideoPixelFormat.RGBA_8888, VideoFrame.RotationAngle.ROTATION_90},
            new Object[] {VideoPixelFormat.RGBA_8888, VideoFrame.RotationAngle.ROTATION_180},
            new Object[] {VideoPixelFormat.RGBA_8888, VideoFrame.RotationAngle.ROTATION_270}
        };
    }
}
