package com.twilio.conversations.videoconstraints;

import android.content.Context;
import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationCallback;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.LocalMedia;
import com.twilio.conversations.LocalMediaFactory;
import com.twilio.conversations.LocalMediaListener;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.LocalVideoTrackFactory;
import com.twilio.conversations.TwilioConversations;
import com.twilio.conversations.TwilioConversationsActivity;
import com.twilio.conversations.TwilioConversationsException;
import com.twilio.conversations.VideoConstraints;
import com.twilio.conversations.VideoDimensions;
import com.twilio.conversations.helper.AccessTokenHelper;
import com.twilio.conversations.helper.CameraCapturerHelper;
import com.twilio.conversations.helper.ConversationsClientHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class VideoConstrainedConversationTests {
    private static final String SELF_TEST_USER = "SELF_TEST_USER";

    @Rule
    public ActivityTestRule<TwilioConversationsActivity> activityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @Test
    public void startConversationWithInvalidVideoConstraints() throws InterruptedException {
        if(requiresRuntimePermissions()) {
            return;
        }

        TwilioConversations.setLogLevel(TwilioConversations.LogLevel.DEBUG);

        TwilioAccessManager twilioAccessManager = AccessTokenHelper.obtainTwilioAccessManager(SELF_TEST_USER);
        ConversationsClient conversationsClient = ConversationsClientHelper.registerClient(activityRule.getActivity(), twilioAccessManager);

        final CountDownLatch localVideoTrackFailedLatch = new CountDownLatch(1);

        LocalVideoTrack localVideoTrack = createLocalVideoTrackWithVideoConstraints(
                activityRule.getActivity(),
                new VideoConstraints.Builder()
                        .minVideoDimensions(new VideoDimensions(1,2))
                        .maxVideoDimensions(new VideoDimensions(10,20))
                        .build());

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack videoTrack) {
                fail();
            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack videoTrack) {
                fail();
            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack track, TwilioConversationsException exception) {
                localVideoTrackFailedLatch.countDown();
            }
        });

        localMedia.addLocalVideoTrack(localVideoTrack);

        /*
         * Intentionally call the user registered to this client to allow the conversation
         * to setup its local media without requiring a remote participant.
         */
        Set<String> participants = new HashSet<>();
        participants.add(SELF_TEST_USER);

        conversationsClient.sendConversationInvite(participants, localMedia, new ConversationCallback() {
            @Override
            public void onConversation(final Conversation conversation, TwilioConversationsException exception) {
                // The invite from self is never accepted so this should not get called.
                fail();
            }
        });

        localVideoTrackFailedLatch.await(20, TimeUnit.SECONDS);

    }

    private LocalVideoTrack createLocalVideoTrackWithVideoConstraints(Context context, VideoConstraints videoConstraints) throws InterruptedException {
        CameraCapturer cameraCapturer = CameraCapturerHelper.createCameraCapturer(context, CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA);
        return LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer, videoConstraints);
    }

    /*
     * Runtime permissions to enable the camera are not enabled in this test
     */
    private boolean requiresRuntimePermissions() {
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

}
