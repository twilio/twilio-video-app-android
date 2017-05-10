package com.twilio.video;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class VideoUnitTest {
    @Mock Context mockContext;
    @Mock ConnectOptions mockConnectOptions;
    @Mock Room.Listener mockRoomListener;

    @Test(expected = NullPointerException.class)
    public void connect_shouldFailWithNullContext() {
        Video.connect(null, mockConnectOptions, mockRoomListener);
    }

    @Test(expected = NullPointerException.class)
    public void connect_shouldFailWithNullConnectOptions() {
        Video.connect(mockContext, null, mockRoomListener);
    }

    @Test(expected = NullPointerException.class)
    public void connect_shouldFailWithNullRoomListener() {
        Video.connect(mockContext, mockConnectOptions, null);
    }
}
