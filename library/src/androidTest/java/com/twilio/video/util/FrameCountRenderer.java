package com.twilio.video.util;

import com.twilio.video.I420Frame;
import com.twilio.video.VideoRenderer;

public class FrameCountRenderer implements VideoRenderer {
    private int frameCount = 0;

    public int getFrameCount() {
        return frameCount;
    }

    @Override
    public void renderFrame(I420Frame frame) {
        frameCount++;
    }
}
