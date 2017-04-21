package com.twilio.video.base;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.twilio.video.LocalVideoTrack;
import com.twilio.video.util.FakeVideoCapturer;

public abstract class BaseLocalVideoTrackTest {
    protected Context context;
    protected LocalVideoTrack localVideoTrack;
    protected FakeVideoCapturer fakeVideoCapturer;

    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        fakeVideoCapturer = new FakeVideoCapturer();
    }

    public void teardown() {
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
    }
}
