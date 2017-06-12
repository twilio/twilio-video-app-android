package com.twilio.video;

import com.twilio.video.base.BaseCamera2CapturerTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class Camera2IdParameterizedCapturerTest extends BaseCamera2CapturerTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"0"},
                {"1"}});
    }

    private final String cameraId;

    public Camera2IdParameterizedCapturerTest(String cameraId) {
        this.cameraId = cameraId;
    }

    @Before
    public void setUp() {
        super.setup();
    }

    @After
    public void teardown() {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void shouldCaptureFramesWhenVideoTrackCreated() throws InterruptedException {
        final CountDownLatch firstFrameReceived = new CountDownLatch(1);
        camera2Capturer = new Camera2Capturer(cameraCapturerActivity, cameraId,
                new Camera2Capturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {
                        firstFrameReceived.countDown();
                    }

                    @Override
                    public void onCameraSwitched(String cameraId) {

                    }

                    @Override
                    public void onError(Camera2Capturer.Exception exception) {

                    }
                });
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, camera2Capturer);

        // Validate we got our first frame
        assertTrue(firstFrameReceived.await(CAMERA2_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void canBeRenderedToView() throws InterruptedException {
        VideoView localVideo = (VideoView) cameraCapturerActivity.findViewById(com.twilio.video.test.R.id.local_video);
        final CountDownLatch renderedFirstFrame = new CountDownLatch(1);
        VideoRenderer.Listener rendererListener = new VideoRenderer.Listener() {
            @Override
            public void onFirstFrame() {
                renderedFirstFrame.countDown();
            }

            @Override
            public void onFrameDimensionsChanged(int width, int height, int rotation) {

            }
        };
        localVideo.setListener(rendererListener);
        camera2Capturer = new Camera2Capturer(cameraCapturerActivity, cameraId,
                new Camera2Capturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {

                    }

                    @Override
                    public void onCameraSwitched(String cameraId) {

                    }

                    @Override
                    public void onError(Camera2Capturer.Exception exception) {

                    }
                });
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, camera2Capturer);
        localVideoTrack.addRenderer(localVideo);
        assertTrue(renderedFirstFrame.await(CAMERA2_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
        localVideoTrack.removeRenderer(localVideo);
    }

    @Test
    public void canBeReused() throws InterruptedException {
        int reuseCount = 2;

        // Reuse the same capturer while we iterate
        final AtomicReference<CountDownLatch> firstFrameReceived = new AtomicReference<>();
        camera2Capturer = new Camera2Capturer(cameraCapturerActivity, cameraId,
                new Camera2Capturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {
                        firstFrameReceived.get().countDown();
                    }

                    @Override
                    public void onCameraSwitched(String cameraId) {

                    }

                    @Override
                    public void onError(Camera2Capturer.Exception exception) {

                    }
                });
        for (int i = 0 ; i < reuseCount ; i++) {
            firstFrameReceived.set(new CountDownLatch(1));
            LocalVideoTrack localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity,
                    true, camera2Capturer);

            // Validate we got our first frame
            assertTrue(firstFrameReceived.get().await(CAMERA2_CAPTURER_DELAY_MS,
                    TimeUnit.MILLISECONDS));

            // Release video track
            localVideoTrack.release();
        }
    }
}
