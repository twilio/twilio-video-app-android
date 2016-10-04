package com.twilio.video.util;

import com.twilio.video.I420Frame;
import com.twilio.video.VideoRenderer;

public class FakeVideoRenderer implements VideoRenderer {
    @Override
    public void renderFrame(I420Frame frame) {
        frame.release();
    }
}
