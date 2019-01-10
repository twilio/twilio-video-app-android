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

package com.twilio.video.util;

import static junit.framework.TestCase.fail;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import com.twilio.video.TestUtils;
import com.twilio.video.VideoCapturer;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoFrame;
import com.twilio.video.VideoPixelFormat;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FakeVideoCapturer implements VideoCapturer {
    private static final long FRAMERATE_MS = 33;

    private VideoFormat captureFormat;
    private AtomicBoolean started = new AtomicBoolean(false);
    private VideoCapturer.Listener capturerListener;
    private FakeCapturerThread fakeCapturerThread;
    private Handler fakeVideoCapturerHandler;
    private final int[] colors = new int[] {Color.RED, Color.GREEN, Color.BLUE};
    private int frameCounter = 0;
    private int colorIndex = 0;
    private final Runnable frameGenerator =
            new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap =
                            Bitmap.createBitmap(
                                    captureFormat.dimensions.width,
                                    captureFormat.dimensions.height,
                                    Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(getAndUpdateColor());
                    ByteBuffer buffer = ByteBuffer.allocateDirect(bitmap.getByteCount());
                    bitmap.copyPixelsToBuffer(buffer);
                    final long captureTimeNs =
                            TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
                    VideoFrame videoFrame =
                            new VideoFrame(
                                    buffer.array(),
                                    captureFormat.dimensions,
                                    rotationAngle,
                                    captureTimeNs);

                    // Only notify the frame listener if we are not stopped
                    if (started.get() && fakeVideoCapturerHandler != null) {
                        capturerListener.onFrameCaptured(videoFrame);
                        fakeVideoCapturerHandler.postDelayed(this, FRAMERATE_MS);
                    }
                }
            };

    private final List<VideoFormat> supportedFormats;
    private final VideoFrame.RotationAngle rotationAngle;

    public FakeVideoCapturer() {
        this(defaultSupportedFormats());
    }

    public FakeVideoCapturer(List<VideoFormat> supportedFormats) {
        this(supportedFormats, VideoFrame.RotationAngle.ROTATION_90);
    }

    public FakeVideoCapturer(
            List<VideoFormat> supportedFormats, VideoFrame.RotationAngle rotationAngle) {
        this.supportedFormats = supportedFormats;
        this.rotationAngle = rotationAngle;
    }

    public VideoFormat getCaptureFormat() {
        return captureFormat;
    }

    public boolean isStarted() {
        return started.get();
    }

    @Override
    public synchronized List<VideoFormat> getSupportedFormats() {
        return supportedFormats;
    }

    @Override
    public boolean isScreencast() {
        return false;
    }

    @Override
    public void startCapture(VideoFormat captureFormat, VideoCapturer.Listener capturerListener) {
        this.captureFormat = captureFormat;
        this.capturerListener = capturerListener;
        this.started.set(true);

        // Will asynchronously start the capturer
        fakeCapturerThread = new FakeCapturerThread();
        fakeCapturerThread.startAsync();
    }

    @Override
    public void stopCapture() {
        this.started.set(false);

        // Blocking call that ensures the capturer is stopped
        fakeCapturerThread.stopSync();
    }

    private int getAndUpdateColor() {
        // Get current color
        int color = colors[colorIndex];

        // Change the color every 30 frames
        frameCounter++;
        if (frameCounter % 30 == 0) {
            frameCounter = 0;
            colorIndex = (colorIndex + 1) % colors.length;
        }

        return color;
    }

    private static List<VideoFormat> defaultSupportedFormats() {
        VideoDimensions dimensions = new VideoDimensions(640, 360);
        VideoFormat videoFormat = new VideoFormat(dimensions, 30, VideoPixelFormat.RGBA_8888);
        List<VideoFormat> supportedFormats = new ArrayList<>();

        supportedFormats.add(videoFormat);

        return supportedFormats;
    }

    class FakeCapturerThread extends Thread {
        private final Object looperStartedEvent = new Object();
        private boolean running = false;

        public synchronized void startAsync() {
            if (running) {
                return;
            }
            running = true;
            fakeVideoCapturerHandler = null;
            start();
            synchronized (looperStartedEvent) {
                while (fakeVideoCapturerHandler == null) {
                    try {
                        looperStartedEvent.wait();
                    } catch (InterruptedException e) {
                        fail("Can not start fake capturer looper thread");
                        running = false;
                    }
                }
            }
        }

        public void run() {
            Looper.prepare();
            synchronized (looperStartedEvent) {
                fakeVideoCapturerHandler = new Handler();
                if (started.get()) {
                    started.set(fakeVideoCapturerHandler.postDelayed(frameGenerator, FRAMERATE_MS));
                    capturerListener.onCapturerStarted(started.get());
                }
                looperStartedEvent.notify();
            }
            Looper.loop();
        }

        public synchronized void stopSync() {
            if (!running) {
                return;
            }
            running = false;
            final CountDownLatch capturerStopped = new CountDownLatch(1);
            fakeVideoCapturerHandler.post(
                    () -> {
                        Looper.myLooper().quit();
                        capturerStopped.countDown();
                    });
            try {
                capturerStopped.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail("Failed to stop fake capturer");
            }
        }
    }
}
