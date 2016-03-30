package com.twilio.conversations.videoconstraints;

import android.content.Context;
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class VideoConstrainedConversationTests {
    private static final String TEST_USER = "TEST_USER";
    private static final String BOT_TEST_USER = "TEST_USER";

    @Rule
    public ActivityTestRule<TwilioConversationsActivity> activityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @Test
    public void startConversationWithVideoConstraints() throws InterruptedException {
        TwilioAccessManager twilioAccessManager = AccessTokenHelper.obtainTwilioAccessManager(TEST_USER);
        ConversationsClient conversationsClient = ConversationsClientHelper.registerClient(activityRule.getActivity(), twilioAccessManager);

        final CountDownLatch conversationLatch = new CountDownLatch(1);
        final CountDownLatch localVideoTrackAddedLatch = new CountDownLatch(1);
        final Conversation[] createdConversation = new Conversation[1];

        LocalVideoTrack localVideoTrack = createLocalVideoTrack(activityRule.getActivity());

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack videoTrack) {
                localVideoTrackAddedLatch.countDown();
            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack videoTrack) {
                fail();
            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack track, TwilioConversationsException exception) {
                fail();
            }
        });

        localMedia.addLocalVideoTrack(localVideoTrack);

        Set<String> participants = new HashSet<>();
        participants.add(BOT_TEST_USER);

        conversationsClient.sendConversationInvite(participants, localMedia, new ConversationCallback() {
            @Override
            public void onConversation(final Conversation conversation, TwilioConversationsException exception) {
                if(exception != null) {
                    fail(exception.getMessage());
                }
                createdConversation[0] = conversation;
                conversationLatch.countDown();
            }
        });

        conversationLatch.await(10, TimeUnit.SECONDS);
        localVideoTrackAddedLatch.await(10, TimeUnit.SECONDS);

        assertNotNull(createdConversation[0]);
    }

    private LocalVideoTrack createLocalVideoTrack(Context context) throws InterruptedException {
        CameraCapturer cameraCapturer = CameraCapturerHelper.createCameraCapturer(context, CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA);
        return LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer);
    }

    private LocalVideoTrack createLocalVideoTrackWithVideoConstraints(Context context, VideoConstraints videoConstraints) throws InterruptedException {
        CameraCapturer cameraCapturer = CameraCapturerHelper.createCameraCapturer(context, CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA);
        return LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer, videoConstraints);
    }

}
