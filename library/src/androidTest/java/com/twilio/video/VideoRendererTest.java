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

import android.Manifest;
import android.graphics.Bitmap;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ImageView;

import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.ui.VideoRendererTestActivity;
import com.twilio.video.util.FakeVideoCapturer;

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
public class VideoRendererTest extends BaseVideoTest {
    private static final int BITMAP_TIMEOUT_MS = 10000;

    @Rule
    public GrantPermissionRule cameraPermissionsRule = GrantPermissionRule
            .grant(Manifest.permission.CAMERA);
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
    public void setup() throws InterruptedException {
        super.setup();
        videoRendererTestActivity = activityRule.getActivity();
        cameraCapturer = new CameraCapturer(videoRendererTestActivity,
                CameraCapturer.CameraSource.FRONT_CAMERA);
        fakeVideoCapturer = new FakeVideoCapturer();
        cameraVideoTrack = LocalVideoTrack.create(videoRendererTestActivity, true, cameraCapturer);
        fakeVideoTrack = LocalVideoTrack.create(videoRendererTestActivity, true, fakeVideoCapturer);
        videoView = videoRendererTestActivity.findViewById(R.id.video);
        imageView = videoRendererTestActivity.findViewById(R.id.image_view);
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
        bitmapVideoRenderer.captureBitmap(bitmap -> {
            assertNotNull(bitmap);
            videoRendererTestActivity.runOnUiThread(() -> {
                imageView.setImageBitmap(bitmap);
                bitmapCaptured.countDown();
            });
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
        bitmapVideoRenderer.captureBitmap(bitmap -> {
            assertNotNull(bitmap);
            videoRendererTestActivity.runOnUiThread(() -> {
                imageView.setImageBitmap(bitmap);
                bitmapCaptured.countDown();
            });
        });

        // Validate we received bitmap
        assertTrue(bitmapCaptured.await(BITMAP_TIMEOUT_MS, TimeUnit.MILLISECONDS));
    }
}
