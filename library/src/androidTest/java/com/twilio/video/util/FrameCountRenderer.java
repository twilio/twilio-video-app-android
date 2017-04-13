package com.twilio.video.util;

import com.twilio.video.I420Frame;
import com.twilio.video.VideoRenderer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class FrameCountRenderer implements VideoRenderer {
    private final AtomicReference<CountDownLatch> frameArrived =
            new AtomicReference<>(new CountDownLatch(1));
    private int frameCount = 0;

    public int getFrameCount() {
        return frameCount;
    }

    @Override
    public void renderFrame(I420Frame frame) {
        frameCount++;
        frameArrived.get().countDown();
        frame.release();
    }

    public boolean waitForFrame(int timeoutMs) throws InterruptedException {
        frameArrived.set(new CountDownLatch(1));
        return frameArrived.get().await(timeoutMs, TimeUnit.MILLISECONDS);
    }
}
