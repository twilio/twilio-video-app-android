package com.twilio.conversations;

import android.content.Context;
import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.activity.TwilioConversationsActivity;
import com.twilio.conversations.helper.AccessTokenHelper;
import com.twilio.conversations.helper.CameraCapturerHelper;
import com.twilio.conversations.helper.ConversationsClientHelper;
import com.twilio.conversations.helper.TwilioConversationsHelper;
import com.twilio.conversations.helper.TwilioConversationsTestsBase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class VideoConstraintsTests extends TwilioConversationsTestsBase {
    private static final String SELF_TEST_USER = "SELF_TEST_USER";

    @Rule
    public ActivityTestRule<TwilioConversationsActivity> activityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @After
    public void teardown() {
        TwilioConversationsHelper.destroy();
    }

    @Test(expected = NullPointerException.class)
    public void localVideoTrackWithNullVideoConstraints() {
        ViewGroup viewGroup = new LinearLayout(activityRule.getActivity());
        CameraCapturer cameraCapturer = CameraCapturerFactory.
                createCameraCapturer(
                        activityRule.getActivity(),
                        CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA,
                        viewGroup,
                        new CapturerErrorListener() {
                            @Override
                            public void onError(CapturerException e) {

                            }
                        });

        assertNotNull(cameraCapturer);

        LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer, null);
    }

    @Test
    @Ignore
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

        /**
         * Intentionally call the user registered to this client to allow the conversation
         * to setup its local media without requiring a remote participant.
         */
        Set<String> participants = new HashSet<>();
        participants.add(SELF_TEST_USER);

        final CountDownLatch conversationEndsWhenInviteCancelled = new CountDownLatch(1);
        final OutgoingInvite outgoingInvite = conversationsClient.sendConversationInvite(participants, localMedia, new ConversationCallback() {
            @Override
            public void onConversation(final Conversation conversation, TwilioConversationsException exception) {
                // The outgoing invite is cancelled and reported as an exception here
                assertNotNull(exception);
                conversationEndsWhenInviteCancelled.countDown();
            }
        });

        /**
         * Set a new conversations client listener to handle the stop listening for invites event here
         */
        conversationsClient.setConversationsClientListener(new ConversationsClientListener() {
            @Override
            public void onStartListeningForInvites(ConversationsClient conversationsClient) {

            }

            @Override
            public void onStopListeningForInvites(ConversationsClient conversationsClient) {
                outgoingInvite.cancel();
            }

            @Override
            public void onFailedToStartListening(ConversationsClient conversationsClient, TwilioConversationsException exception) {

            }

            @Override
            public void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {

            }

            @Override
            public void onIncomingInviteCancelled(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {

            }
        });

        assertTrue(localVideoTrackFailedLatch.await(20, TimeUnit.SECONDS));

        conversationsClient.unlisten();

        assertTrue(conversationEndsWhenInviteCancelled.await(20, TimeUnit.SECONDS));

        TwilioConversationsHelper.destroy();
    }

    @Test
    @Ignore
    public void startConversationWithValidVideoConstraints() throws InterruptedException {
        if(requiresRuntimePermissions()) {
            return;
        }

        TwilioConversations.setLogLevel(TwilioConversations.LogLevel.DEBUG);

        TwilioAccessManager twilioAccessManager = AccessTokenHelper.obtainTwilioAccessManager(SELF_TEST_USER);
        ConversationsClient conversationsClient = ConversationsClientHelper.registerClient(activityRule.getActivity(), twilioAccessManager);

        final CountDownLatch localVideoTrackAddedLatch = new CountDownLatch(1);
        final CountDownLatch localVideoTrackRemovedLatch = new CountDownLatch(1);

        LocalVideoTrack localVideoTrack = createLocalVideoTrackWithVideoConstraints(
                activityRule.getActivity(),
                new VideoConstraints.Builder()
                        .minVideoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS)
                        .maxVideoDimensions(VideoDimensions.VGA_VIDEO_DIMENSIONS)
                        .build());

        LocalMedia localMedia = LocalMediaFactory.createLocalMedia(new LocalMediaListener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack videoTrack) {
                localVideoTrackAddedLatch.countDown();
            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack videoTrack) {
                localVideoTrackRemovedLatch.countDown();
            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack track, TwilioConversationsException exception) {
                fail();
            }
        });

        localMedia.addLocalVideoTrack(localVideoTrack);

        /*
         * Intentionally call the user registered to this client to allow the conversation
         * to setup its local media without requiring a remote participant.
         */
        Set<String> participants = new HashSet<>();
        participants.add(SELF_TEST_USER);

        final CountDownLatch conversationEndsWhenInviteCancelled = new CountDownLatch(1);
        final OutgoingInvite outgoingInvite = conversationsClient.sendConversationInvite(participants, localMedia, new ConversationCallback() {
            @Override
            public void onConversation(final Conversation conversation, TwilioConversationsException exception) {
                // The outgoing invite is cancelled and reported as an exception here
                assertNotNull(exception);
                conversationEndsWhenInviteCancelled.countDown();
            }
        });

        /**
         * Set a new conversations client listener to handle the stop listening for invites event here
         */
        conversationsClient.setConversationsClientListener(new ConversationsClientListener() {
            @Override
            public void onStartListeningForInvites(ConversationsClient conversationsClient) {

            }

            @Override
            public void onStopListeningForInvites(ConversationsClient conversationsClient) {
                outgoingInvite.cancel();
            }

            @Override
            public void onFailedToStartListening(ConversationsClient conversationsClient, TwilioConversationsException exception) {

            }

            @Override
            public void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {

            }

            @Override
            public void onIncomingInviteCancelled(ConversationsClient conversationsClient, IncomingInvite incomingInvite) {

            }
        });

        assertTrue(localVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));

        conversationsClient.unlisten();

        assertTrue(localVideoTrackRemovedLatch.await(20, TimeUnit.SECONDS));

        assertTrue(conversationEndsWhenInviteCancelled.await(20, TimeUnit.SECONDS));

        TwilioConversationsHelper.destroy();
    }

    private LocalVideoTrack createLocalVideoTrackWithVideoConstraints(Context context, VideoConstraints videoConstraints) throws InterruptedException {
        CameraCapturer cameraCapturer = CameraCapturerHelper.createCameraCapturer(context, CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA);
        return LocalVideoTrackFactory.createLocalVideoTrack(cameraCapturer, videoConstraints);
    }

    /**
     * Runtime permissions to enable the camera are not enabled in this test
     */
    private boolean requiresRuntimePermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
