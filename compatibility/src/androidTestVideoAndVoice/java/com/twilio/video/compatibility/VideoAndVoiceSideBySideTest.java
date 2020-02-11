package com.twilio.video.compatibility;

import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.Voice;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VideoAndVoiceSideBySideTest extends BaseCompatibilityTest {
    @Rule
    public GrantPermissionRule recordAudioPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO);

    @Rule public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @Before
    public void setup() {
        super.setup();
    }

    @Test
    public void canExecuteVideoAndVoiceConnect() throws Throwable {
        CountDownLatch callConnectFailed = new CountDownLatch(1);

        assertVideoCanExecuteConnect();

        uiThreadTestRule.runOnUiThread(
                () -> {
                    Voice.connect(
                            context,
                            "fake token",
                            new Call.Listener() {
                                @Override
                                public void onConnectFailure(
                                        @NonNull Call call, @NonNull CallException callException) {
                                    callConnectFailed.countDown();
                                }

                                @Override
                                public void onRinging(@NonNull Call call) {}

                                @Override
                                public void onConnected(@NonNull Call call) {}

                                @Override
                                public void onDisconnected(
                                        @NonNull Call call,
                                        @Nullable CallException callException) {}
                            });
                });

        assertTrue(callConnectFailed.await(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS));
    }
}
