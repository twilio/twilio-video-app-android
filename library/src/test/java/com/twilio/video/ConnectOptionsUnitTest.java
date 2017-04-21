package com.twilio.video;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ConnectOptionsUnitTest {
    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenTokenIsNull() {
        new ConnectOptions.Builder(null).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenTokenEmptyString() {
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
