package com.twilio.video.util;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoCapturer;
import com.twilio.video.VideoFrame;
import com.twilio.video.VideoPixelFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.fail;

public class FakeVideoCapturer implements VideoCapturer {
    private static final long FRAMERATE_MS = 33;

    private VideoFormat captureFormat;
    private AtomicBoolean started = new AtomicBoolean(false);
    private VideoCapturer.Listener capturerListener;

    // Just used for testing we will just capture on main thread
    private FakeCapturerThread fakeCapturerThread = new FakeCapturerThread();
    private Handler fakeVideoCapturerHandler;
    private final Runnable frameGenerator = new Runnable() {
        @Override
        public void run() {
            // TODO: Actually generate some RGBA data for this frame
            int bufferSize = captureFormat.dimensions.width * captureFormat.dimensions.height * 4;
            byte[] emptyBuffer = new byte[bufferSize];
            final long captureTimeNs =
                    TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());

            VideoFrame emptyVideoFrame = new VideoFrame(emptyBuffer,
                    captureFormat.dimensions, 0, captureTimeNs);

            // Only notify the frame listener if we are not stopped
            if (started.get()) {
                capturerListener.onFrameCaptured(emptyVideoFrame);
                fakeVideoCapturerHandler.postDelayed(this, FRAMERATE_MS);
            }
        }
    };

    public VideoFormat getCaptureFormat() {
        return captureFormat;
    }

    public boolean isStarted() {
        return started.get();
    }

    @Override
    public List<VideoFormat> getSupportedFormats() {
        VideoDimensions dimensions = new VideoDimensions(640, 360);
        VideoFormat videoFormat = new VideoFormat(dimensions, 30, VideoPixelFormat.RGBA_8888);
        List<VideoFormat> supportedFormats = new ArrayList<>();

        supportedFormats.add(videoFormat);

        return supportedFormats;
    }

    @Override
    public void startCapture(VideoFormat captureFormat,
                             VideoCapturer.Listener capturerListener) {
        this.captureFormat = captureFormat;
        this.capturerListener = capturerListener;
        this.started.set(true);

        // Will asynchronously start the capturer
        fakeCapturerThread.startAsync();
    }

    @Override
    public void stopCapture() {
        this.started.set(false);

        // Blocking call that ensures the capturer is stopped
        fakeCapturerThread.stopSync();
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
            fakeVideoCapturerHandler.post(new Runnable() {
                @Override
                public void run() {
                    Looper.myLooper().quit();
                    capturerStopped.countDown();
                }
            });
            try {
                capturerStopped.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail("Failed to stop fake capturer");
            }
        }
    }
}
