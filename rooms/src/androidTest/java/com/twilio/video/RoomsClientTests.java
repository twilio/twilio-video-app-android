package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.AccessManager;
import com.twilio.video.activity.RoomsTestActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RoomsClientTests {

    private Context context;

    @Rule
    public ActivityTestRule<RoomsTestActivity> activityRule = new ActivityTestRule<>(
            RoomsTestActivity.class);

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test(expected = NullPointerException.class)
    public void client_shouldThrowExceptionWhenContextIsNull() {
        new Client(null, accessManager());
    }

    @Test(expected = NullPointerException.class)
    public void client_shouldThrowExceptionWhenAccessManagerIsNull() {
        new Client(context, null);
    }

    @Test
    public void logLevel_shouldBeRetained() {
        Client.setLogLevel(LogLevel.DEBUG);
        assertEquals(LogLevel.DEBUG, Client.getLogLevel());
    }

    @Test
    public void getVersion_shouldReturnValidSemVerFormattedVersion() {
        String semVerRegex = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-" +
                "Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";
        String version = Client.getVersion();

        assertNotNull(version);
        assertTrue(version.matches(semVerRegex));
    }

    @Test
    public void audioOutput_shouldBeRetained() {
        Client client = new Client(context, accessManager());
        client.setAudioOutput(AudioOutput.SPEAKERPHONE);
        assertEquals(AudioOutput.SPEAKERPHONE, client.getAudioOutput());
    }

    private AccessManager accessManager() {
        return new AccessManager(context, null, null);
    }
}

