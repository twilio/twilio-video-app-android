package com.twilio.video;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class LocalVideoTrackUnitTest {
    @Mock Context mockContext;

    @Test(expected = NullPointerException.class)
    public void create_shouldFailWithNullContext() {
        LocalVideoTrack.create(null, true, new VideoCapturer() {
            @Override
            public List<VideoFormat> getSupportedFormats() {
                return null;
            }

            @Override
            public boolean isScreencast() {
                return false;
            }

            @Override
            public void startCapture(VideoFormat captureFormat, Listener capturerListener) {

            }

            @Override
            public void stopCapture() {

            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void create_shouldFailWithNullCapturer() {
        LocalVideoTrack.create(mockContext, true, null);
    }

    @Test(expected = IllegalStateException.class)
    public void create_shouldFailIfVideoCapturerReturnsNullForSupportedFormats() {
        LocalVideoTrack.create(mockContext, true, new VideoCapturer() {
            @Override
            public List<VideoFormat> getSupportedFormats() {
                return null;
            }

            @Override
            public boolean isScreencast() {
                return false;
            }

            @Override
            public void startCapture(VideoFormat captureFormat, Listener capturerListener) {

            }

            @Override
            public void stopCapture() {

            }
        });
    }

    @Test(expected = IllegalStateException.class)
    public void create_shouldFailIfVideoCapturerProvidesNoSupportedFormats() {
        LocalVideoTrack.create(mockContext, true, new VideoCapturer() {
            @Override
            public List<VideoFormat> getSupportedFormats() {
                return new ArrayList<>();
            }

            @Override
            public boolean isScreencast() {
                return false;
            }

            @Override
            public void startCapture(VideoFormat captureFormat, Listener capturerListener) {

            }

            @Override
            public void stopCapture() {

            }
        });
    }
}
