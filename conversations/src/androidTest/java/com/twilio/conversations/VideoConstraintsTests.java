package com.twilio.conversations;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.TwilioAccessManager;
import com.twilio.conversations.activity.TwilioConversationsActivity;
import com.twilio.conversations.helper.AccessTokenHelper;
import com.twilio.conversations.helper.CameraCapturerHelper;
import com.twilio.conversations.helper.TwilioConversationsClientHelper;
import com.twilio.conversations.helper.OSLevelHelper;
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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
@Ignore
public class VideoConstraintsTests extends TwilioConversationsTestsBase {
    private static final String SELF_TEST_USER = "SELF_TEST_USER";
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

    @Test(expected = NullPointerException.class)
    public void localVideoTrackWithNullVideoConstraints() {
        CameraCapturer cameraCapturer = CameraCapturer.create(
                        activityRule.getActivity(),
                        CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA,
                        new CapturerErrorListener() {
                            @Override
                            public void onError(CapturerException e) {

                            }
                        });

        assertNotNull(cameraCapturer);

        new LocalVideoTrack(cameraCapturer, null);
    }

    @Test
    @Ignore
    public void startConversationWithInvalidVideoConstraints() throws InterruptedException {
        if(OSLevelHelper.requiresRuntimePermissions()) {
            return;
        }

        TwilioConversationsClient.setLogLevel(LogLevel.DEBUG);

        TwilioAccessManager twilioAccessManager = AccessTokenHelper.obtainTwilioAccessManager(context, SELF_TEST_USER);
        TwilioConversationsClient twilioConversationsClient = TwilioConversationsClientHelper.registerClient(activityRule.getActivity(), twilioAccessManager);

        final CountDownLatch localVideoTrackFailedLatch = new CountDownLatch(1);

        LocalVideoTrack localVideoTrack = createLocalVideoTrackWithVideoConstraints(
                activityRule.getActivity(),
                new VideoConstraints.Builder()
                        .minVideoDimensions(new VideoDimensions(1,2))
                        .maxVideoDimensions(new VideoDimensions(10,20))
                        .build());

        LocalMedia localMedia = LocalMedia.create(new LocalMedia.Listener() {
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
        final OutgoingInvite outgoingInvite = twilioConversationsClient
                .sendConversationInvite(participants, localMedia, new ConversationCallback() {
                    @Override
                    public void onConversation(final Conversation conversation,
                                               TwilioConversationsException exception) {
                        // The outgoing invite is cancelled and reported as an exception here
                        assertNotNull(exception);
                        conversationEndsWhenInviteCancelled.countDown();
                    }
                });

        /**
         * Set a new conversations client listener to handle the stop listening for invites event here
         */
        twilioConversationsClient.setListener(new TwilioConversationsClient.Listener() {
            @Override
            public void onStartListeningForInvites(TwilioConversationsClient twilioConversationsClient) {

            }

            @Override
            public void onStopListeningForInvites(TwilioConversationsClient twilioConversationsClient) {
                outgoingInvite.cancel();
            }

            @Override
            public void onFailedToStartListening(TwilioConversationsClient twilioConversationsClient,
                                                 TwilioConversationsException exception) {

            }

            @Override
            public void onIncomingInvite(TwilioConversationsClient twilioConversationsClient,
                                         IncomingInvite incomingInvite) {

            }

            @Override
            public void onIncomingInviteCancelled(TwilioConversationsClient twilioConversationsClient,
                                                  IncomingInvite incomingInvite) {

            }
        });

        assertTrue(localVideoTrackFailedLatch.await(20, TimeUnit.SECONDS));

        twilioConversationsClient.unlisten();

        assertTrue(conversationEndsWhenInviteCancelled.await(20, TimeUnit.SECONDS));

    }

    @Test
    @Ignore
    public void startConversationWithValidVideoConstraints() throws InterruptedException {
        if(OSLevelHelper.requiresRuntimePermissions()) {
            return;
        }

        TwilioConversationsClient.setLogLevel(LogLevel.DEBUG);

        TwilioAccessManager twilioAccessManager = AccessTokenHelper.obtainTwilioAccessManager(context, SELF_TEST_USER);
        TwilioConversationsClient twilioConversationsClient = TwilioConversationsClientHelper.registerClient(activityRule.getActivity(), twilioAccessManager);

        final CountDownLatch localVideoTrackAddedLatch = new CountDownLatch(1);
        final CountDownLatch localVideoTrackRemovedLatch = new CountDownLatch(1);

        LocalVideoTrack localVideoTrack = createLocalVideoTrackWithVideoConstraints(
                activityRule.getActivity(),
                new VideoConstraints.Builder()
                        .minVideoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS)
                        .maxVideoDimensions(VideoDimensions.VGA_VIDEO_DIMENSIONS)
                        .build());

        LocalMedia localMedia = LocalMedia.create(new LocalMedia.Listener() {
            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack videoTrack) {
                localVideoTrackAddedLatch.countDown();
            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack videoTrack) {
                localVideoTrackRemovedLatch.countDown();
            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia,
                                               LocalVideoTrack track,
                                               TwilioConversationsException exception) {
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
        final OutgoingInvite outgoingInvite = twilioConversationsClient.sendConversationInvite(participants, localMedia, new ConversationCallback() {
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
        twilioConversationsClient.setListener(new TwilioConversationsClient.Listener() {
            @Override
            public void onStartListeningForInvites(TwilioConversationsClient twilioConversationsClient) {

            }

            @Override
            public void onStopListeningForInvites(TwilioConversationsClient twilioConversationsClient) {
                outgoingInvite.cancel();
            }

            @Override
            public void onFailedToStartListening(TwilioConversationsClient twilioConversationsClient, TwilioConversationsException exception) {

            }

            @Override
            public void onIncomingInvite(TwilioConversationsClient twilioConversationsClient, IncomingInvite incomingInvite) {

            }

            @Override
            public void onIncomingInviteCancelled(TwilioConversationsClient twilioConversationsClient, IncomingInvite incomingInvite) {

            }
        });

        assertTrue(localVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));

        twilioConversationsClient.unlisten();

        assertTrue(localVideoTrackRemovedLatch.await(20, TimeUnit.SECONDS));

        assertTrue(conversationEndsWhenInviteCancelled.await(20, TimeUnit.SECONDS));

    }

    private LocalVideoTrack createLocalVideoTrackWithVideoConstraints(Context context, VideoConstraints videoConstraints) throws InterruptedException {
        CameraCapturer cameraCapturer = CameraCapturerHelper.createCameraCapturer(context, CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA);
        return new LocalVideoTrack(cameraCapturer, videoConstraints);
    }
}
