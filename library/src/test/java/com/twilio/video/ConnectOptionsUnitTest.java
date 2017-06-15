package com.twilio.video;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
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
    public void insightsShouldBeEnabledByDefault() {
        ConnectOptions connectOptions = new ConnectOptions.Builder("test")
                .build();

        assertTrue(connectOptions.isInsightsEnabled());
    }

    @Test
    public void shouldEnableInsights() {
        ConnectOptions connectOptions = new ConnectOptions.Builder("test")
                .enableInsights(true)
                .build();
        assertTrue(connectOptions.isInsightsEnabled());
    }

    @Test
    public void shouldDisableInsights() {
        ConnectOptions connectOptions = new ConnectOptions.Builder("test")
                .enableInsights(false)
                .build();
        assertFalse(connectOptions.isInsightsEnabled());
    }
}
