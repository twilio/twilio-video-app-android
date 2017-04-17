package com.twilio.video;

import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseClientTest;
import com.twilio.video.ui.MediaTestActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ConnectOptionsTest {

    @Before
    public void setup() throws InterruptedException {
    }

    @After
    public void teardown() {
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenTokenIsNull() {
        ConnectOptions connectOptionsBuilder =
            new ConnectOptions.Builder(null).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenTokenEmptyString() {
        ConnectOptions connectOptionsBuilder =
            new ConnectOptions.Builder("").build();
    }

    @Test
    public void shouldEnableInsights() {
        ConnectOptions connectOptions = new ConnectOptions.Builder("test")
            .enableInsights(true)
            .build();
        assertTrue(connectOptions.isInsightsEnabled());
    }
}