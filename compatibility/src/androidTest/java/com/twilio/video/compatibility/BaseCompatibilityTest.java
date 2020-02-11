package com.twilio.video.compatibility;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import com.twilio.video.ConnectOptions;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;

public class BaseCompatibilityTest {
    public static final int CONNECT_TIMEOUT_MS = 30000;

    Context context;

    @Before
    @CallSuper
    public void setup() {
        this.context = InstrumentationRegistry.getTargetContext();
    }

    /*
     * The purpose of this test is to ensure that the Video SDK can connect without any runtime
     * exceptions. The test connects with a fake token to ensure the native library can be loaded
     * and executed to an expected connect failure.
     */
    void assertVideoCanExecuteConnect() throws Throwable {
        CountDownLatch connectFailed = new CountDownLatch(1);

        // Connect with Video
        Video.connect(
                context,
                new ConnectOptions.Builder("fake token").build(),
                new Room.Listener() {
                    @Override
                    public void onConnected(@NonNull Room room) {}

                    @Override
                    public void onConnectFailure(
                            @NonNull Room room, @NonNull TwilioException twilioException) {
                        connectFailed.countDown();
                    }

                    @Override
                    public void onReconnecting(
                            @NonNull Room room, @NonNull TwilioException twilioException) {}

                    @Override
                    public void onReconnected(@NonNull Room room) {}

                    @Override
                    public void onDisconnected(
                            @NonNull Room room, @Nullable TwilioException twilioException) {}

                    @Override
                    public void onParticipantConnected(
                            @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {}

                    @Override
                    public void onParticipantDisconnected(
                            @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {}

                    @Override
                    public void onRecordingStarted(@NonNull Room room) {}

                    @Override
                    public void onRecordingStopped(@NonNull Room room) {}
                });

        assertTrue(connectFailed.await(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS));
    }
}
