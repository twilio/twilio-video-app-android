package com.twilio.video;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.widget.FrameLayout;

import com.twilio.video.base.BaseCameraCapturerTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.twilio.video.test.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class CameraCapturerParameterizedTest extends BaseCameraCapturerTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA},
                {CameraCapturer.CameraSource.CAMERA_SOURCE_BACK_CAMERA}});
    }

    private final CameraCapturer.CameraSource cameraSource;

    public CameraCapturerParameterizedTest(CameraCapturer.CameraSource cameraSource) {
        this.cameraSource = cameraSource;
    }

    @Test
    public void shouldCaptureFramesWhenAddedToVideoTrack() throws InterruptedException {
        cameraCapturer = CameraCapturer.create(cameraCapturerActivity, cameraSource, null);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Add renderer and wait a second
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        // Validate our frame count is atleast 5
        frameCount = frameCountRenderer.getFrameCount();
        assertTrue(frameCount >= 5);
    }

    @Test
    public void shouldStopCapturingFramesWhenRemovedFromVideoTrack() throws InterruptedException {
        cameraCapturer = CameraCapturer.create(cameraCapturerActivity, cameraSource, null);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Add renderer and wait a second
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        // Validate our frame count is atleast 5
        frameCount = frameCountRenderer.getFrameCount();
        assertTrue(frameCount >= 5);

        // Remove the renderer and wait
        frameCount = frameCountRenderer.getFrameCount();
        localVideoTrack.removeRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        /*
         * Ensure our camera capturer is no longer capturing frames with a one frame buffer in the
         * event of a race in test case
         */
        boolean framesNotRenderering = frameCount >= (frameCountRenderer.getFrameCount() - 1);
        assertTrue(framesNotRenderering);
    }

    @Test
    public void canBeRenderedToView() throws InterruptedException {
        final FrameLayout localVideo =
                (FrameLayout) cameraCapturerActivity.findViewById(R.id.local_video);
        final AtomicReference<VideoViewRenderer> videoViewRendererReference =
                new AtomicReference<>();
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        cameraCapturerActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoViewRendererReference.set(new VideoViewRenderer(cameraCapturerActivity,
                        localVideo));
            }
        });
        instrumentation.waitForIdleSync();
        VideoViewRenderer videoViewRenderer = videoViewRendererReference.get();
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
        videoViewRenderer.setListener(rendererListener);
        cameraCapturer = CameraCapturer.create(cameraCapturerActivity, cameraSource, null);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        localVideoTrack.addRenderer(videoViewRenderer);

        /*
         * Validate we rendered the first frame and wait a few seconds so we can see the
         * frames if we are watching :-)
         */
        assertTrue(renderedFirstFrame.await(2, TimeUnit.SECONDS));
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        videoViewRenderer.release();
    }
}
