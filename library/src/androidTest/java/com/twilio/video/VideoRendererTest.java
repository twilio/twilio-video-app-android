package com.twilio.video;

import android.graphics.Bitmap;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ImageView;

import com.twilio.video.ui.VideoRendererTestActivity;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.PermissionUtils;

import com.twilio.video.test.R;
import com.twilio.video.util.BitmapVideoRenderer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class VideoRendererTest {
    private static final int BITMAP_TIMEOUT_MS = 10000;

    @Rule
    public ActivityTestRule<VideoRendererTestActivity> activityRule =
            new ActivityTestRule<>(VideoRendererTestActivity.class);
    private VideoRendererTestActivity videoRendererTestActivity;
    private CameraCapturer cameraCapturer;
    private FakeVideoCapturer fakeVideoCapturer;
    private LocalVideoTrack cameraVideoTrack;
    private LocalVideoTrack fakeVideoTrack;
    private VideoView videoView;
    private ImageView imageView;

    @Before
    public void setup() {
        videoRendererTestActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(videoRendererTestActivity);
        cameraCapturer = new CameraCapturer(videoRendererTestActivity,
                CameraCapturer.CameraSource.FRONT_CAMERA);
        fakeVideoCapturer = new FakeVideoCapturer();
        cameraVideoTrack = LocalVideoTrack.create(videoRendererTestActivity, true, cameraCapturer);
        fakeVideoTrack = LocalVideoTrack.create(videoRendererTestActivity, true, fakeVideoCapturer);
        videoView = (VideoView) videoRendererTestActivity.findViewById(R.id.video);
        imageView = (ImageView) videoRendererTestActivity.findViewById(R.id.image_view);
    }

    @After
    public void teardown() {
        if (cameraVideoTrack != null) {
            cameraVideoTrack.release();
        }
        if (fakeVideoTrack != null) {
            fakeVideoTrack.release();
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void canRenderCameraCapturerFrameToBitmap() throws InterruptedException {
        BitmapVideoRenderer bitmapVideoRenderer = new BitmapVideoRenderer();
        final CountDownLatch bitmapCaptured = new CountDownLatch(1);

        // Add renderers
        cameraVideoTrack.addRenderer(videoView);
        cameraVideoTrack.addRenderer(bitmapVideoRenderer);

        // Request bitmap
        bitmapVideoRenderer.captureBitmap(new BitmapVideoRenderer.BitmapListener() {
            @Override
            public void onBitmapCaptured(final Bitmap bitmap) {
                assertNotNull(bitmap);
                videoRendererTestActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                        bitmapCaptured.countDown();
                    }
                });
            }
        });

        // Validate we received bitmap
        assertTrue(bitmapCaptured.await(BITMAP_TIMEOUT_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void canRenderFakeVideoCapturerFrameToBitmap() throws InterruptedException {
        BitmapVideoRenderer bitmapVideoRenderer = new BitmapVideoRenderer();
        final CountDownLatch bitmapCaptured = new CountDownLatch(1);

        // Add renderers
        fakeVideoTrack.addRenderer(videoView);
        fakeVideoTrack.addRenderer(bitmapVideoRenderer);

        // Request bitmap
        bitmapVideoRenderer.captureBitmap(new BitmapVideoRenderer.BitmapListener() {
            @Override
            public void onBitmapCaptured(final Bitmap bitmap) {
                assertNotNull(bitmap);
                videoRendererTestActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                        bitmapCaptured.countDown();
                    }
                });
            }
        });

        // Validate we received bitmap
        assertTrue(bitmapCaptured.await(BITMAP_TIMEOUT_MS, TimeUnit.MILLISECONDS));
    }
}
