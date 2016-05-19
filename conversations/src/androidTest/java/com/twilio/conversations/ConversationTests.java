package com.twilio.conversations;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.activity.TwilioConversationsActivity;
import com.twilio.conversations.helper.AccessTokenHelper;
import com.twilio.conversations.helper.CameraCapturerHelper;
import com.twilio.conversations.helper.TwilioConversationsClientHelper;
import com.twilio.conversations.helper.TwilioConversationsHelper;
import com.twilio.conversations.helper.TwilioConversationsTestsBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ConversationTests extends TwilioConversationsTestsBase {
    private static String USER = "john";
    private static String NON_EXISTANT_PARTICIPANT ="non-existant-paritcipant";
    private Context context;

    @Rule
    public ActivityTestRule<TwilioConversationsActivity> activityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
    }

    @After
    public void teardown() {
        TwilioConversationsHelper.destroy();
    }

    @Test
    @Ignore
    public void muteShouldBeSafeToCallAnytimeDuringAConversation() throws Throwable {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           return;
        }

        TwilioAccessManager accessManager = AccessTokenHelper.obtainTwilioAccessManager(context, USER);
        TwilioConversationsClient twilioConversationsClient = TwilioConversationsClientHelper.registerClient(activityRule.getActivity(), accessManager);

        LocalMedia localMedia = LocalMedia.create(new LocalMedia.Listener() {
            @Override
            public void onLocalVideoTrackAdded(final LocalMedia localMedia,
                                               LocalVideoTrack localVideoTrack) {
                try {
                    activityRule.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Handler handler = new Handler();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    localMedia.mute(!localMedia.isMuted());
                                    handler.postDelayed(this, 100);
                                }
                            });
                        }
                    });
                } catch (Throwable throwable) {
                    fail();
                }
            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack localVideoTrack) {
                // do nothing
            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack localVideoTrack, TwilioConversationsException e) {
                fail();
            }
        });

        CameraCapturer cameraCapturer = CameraCapturerHelper.createCameraCapturer(activityRule.getActivity(), CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA);

        localMedia.addLocalVideoTrack(LocalVideoTrack.create(cameraCapturer));

        Set<String> participants = new HashSet<>();
        participants.add(NON_EXISTANT_PARTICIPANT);

        final CountDownLatch conversationEndsWithException = new CountDownLatch(1);

        twilioConversationsClient.sendConversationInvite(participants, localMedia, new ConversationCallback() {
            @Override
            public void onConversation(Conversation conversation, TwilioConversationsException e) {
                assertNotNull(e);
                conversationEndsWithException.countDown();
            }
        });

        conversationEndsWithException.await(10, TimeUnit.SECONDS);
    }
}
