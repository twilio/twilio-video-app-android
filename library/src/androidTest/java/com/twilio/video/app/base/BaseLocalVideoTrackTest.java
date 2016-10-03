package com.twilio.video.app.base;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.twilio.video.LocalMedia;
import com.twilio.video.app.util.FakeVideoCapturer;

public abstract class BaseLocalVideoTrackTest {
    protected Context context;
    protected LocalMedia localMedia;
    protected FakeVideoCapturer fakeVideoCapturer;

    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        localMedia = LocalMedia.create(context);
        fakeVideoCapturer = new FakeVideoCapturer();
    }

    public void teardown() {
        localMedia.release();
    }
}
