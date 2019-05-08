package com.twilio.video;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.Manifest;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.util.Pair;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.twilioapi.model.VideoRoom;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TrackSubscriptionTest extends BaseVideoTest {
    public static final String VIDEO = "video";
    public static final String AUDIO = "audio";
    public static final String DATA = "data";

    private static final int PARTICIPANT_NUM = 3;
    private static final String[] PARTICIPANTS = {
        Constants.PARTICIPANT_ALICE, Constants.PARTICIPANT_BOB, Constants.PARTICIPANT_CHARLIE
    };

    private String roomName;
    private Context context;
    private List<String> tokens;
    private TrackContainer trackContainer;

    private Room aliceRoom, bobRoom, charlieRoom;
    private VideoRoom videoRoom;

    /**
     * Container for parameterizing by tracks, map key is the type of track and the value is a pair,
     * first being a boolean denoting whether the track is enabled for the test, second being the
     * track itself. Track can't be initialized until the test is fully setup.
     */
    static class TrackContainer {
        private Map<String, Pair<Boolean, Track>> trackMap = new HashMap<>();

        TrackContainer(boolean hasVideo, boolean hasAudio, boolean hasData) {
            trackMap.put(AUDIO, new Pair<>(hasAudio, null));
            trackMap.put(VIDEO, new Pair<>(hasVideo, null));
            trackMap.put(DATA, new Pair<>(hasData, null));
        }
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {new TrackContainer(true, false, false)},
                    {new TrackContainer(false, true, false)},
                    {new TrackContainer(false, false, true)},
                    {new TrackContainer(true, true, false)},
                    {new TrackContainer(false, true, true)},
                    {new TrackContainer(true, true, true)}
                });
    }

    public TrackSubscriptionTest(TrackContainer trackContainer) {
        this.roomName = getClass().getSimpleName();
        this.trackContainer = trackContainer;
    }

    @Override
    public void setup() throws InterruptedException {
        super.setup();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        roomName = RandomStringUtils.random(12);
    }

    @After
    public void teardown() throws InterruptedException {
        LocalAudioTrack audioTrack = (LocalAudioTrack) trackContainer.trackMap.get(AUDIO).second;
        if (audioTrack != null) {
            audioTrack.release();
        }
        LocalVideoTrack videoTrack = (LocalVideoTrack) trackContainer.trackMap.get(VIDEO).second;
        if (videoTrack != null) {
            videoTrack.release();
        }
        LocalDataTrack dataTrack = (LocalDataTrack) trackContainer.trackMap.get(DATA).second;
        if (dataTrack != null) {
            dataTrack.release();
        }
        /*
         * After all participants have disconnected complete the room to clean up backend
         * resources.
         */
        if (aliceRoom != null) {
            aliceRoom.disconnect();
        }
        if (bobRoom != null) {
            bobRoom.disconnect();
            RoomUtils.completeRoom(bobRoom);
        }
        if (charlieRoom != null) {
            charlieRoom.disconnect();
        }

        assertTrue(MediaFactory.isReleased());
    }

    @Rule
    public GrantPermissionRule recordAudioPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO);

    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);

    /**
     * Alice joins room, publishes tracks, bob joins with automatic subscription disabled, bob
     * should not subscribe
     *
     * @throws InterruptedException
     */
    @Test
    public void shouldNotSubscribeToTracksIfAutomaticSubcriptionDisabled()
            throws InterruptedException {
        initTokensWithTopology(Topology.GROUP);

        CallbackHelper.FakeRoomListener aliceRoomListener = new CallbackHelper.FakeRoomListener();
        CallbackHelper.FakeRoomListener bobRoomListener = new CallbackHelper.FakeRoomListener();
        aliceRoomListener.onConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onConnectedLatch = new CountDownLatch(1);

        aliceRoom = createRoom(tokens.get(0), aliceRoomListener, roomName, false);
        assertTrue(
                aliceRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant alice = aliceRoom.getLocalParticipant();
        assertNotNull(alice);

        Pair<Boolean, Track> audioPair = trackContainer.trackMap.get(AUDIO);
        if (audioPair.first) {
            audioPair = new Pair<Boolean, Track>(true, LocalAudioTrack.create(context, true));
            alice.publishTrack((LocalAudioTrack) audioPair.second);
            trackContainer.trackMap.put(AUDIO, audioPair);
        }

        Pair<Boolean, Track> videoPair = trackContainer.trackMap.get(VIDEO);
        if (videoPair.first) {
            videoPair =
                    new Pair<Boolean, Track>(
                            true, LocalVideoTrack.create(context, true, new FakeVideoCapturer()));
            alice.publishTrack((LocalVideoTrack) videoPair.second);
            trackContainer.trackMap.put(VIDEO, videoPair);
        }
        Pair<Boolean, Track> dataPair = trackContainer.trackMap.get(DATA);
        if (dataPair.first) {
            dataPair = new Pair<Boolean, Track>(true, LocalDataTrack.create(context));
            alice.publishTrack((LocalDataTrack) dataPair.second);
            trackContainer.trackMap.put(DATA, dataPair);
        }

        bobRoom = createRoom(tokens.get(1), bobRoomListener, roomName, false);
        assertTrue(
                bobRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant bob = bobRoom.getLocalParticipant();
        assertNotNull(bob);
        bobRoom.getRemoteParticipants()
                .get(0)
                .setListener(
                        new RemoteParticipant.Listener() {
                            @Override
                            public void onAudioTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {
                                fail();
                            }

                            @Override
                            public void onAudioTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onAudioTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {}

                            @Override
                            public void onVideoTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {}

                            @Override
                            public void onDataTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {
                                fail();
                            }

                            @Override
                            public void onDataTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onDataTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {}

                            @Override
                            public void onAudioTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onVideoTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}
                        });

        TestUtils.blockingWait(TestUtils.SMALL_WAIT);

        tearDownRoom(aliceRoom, aliceRoomListener);
        tearDownRoom(bobRoom, bobRoomListener);
    }

    /**
     * Alice joins P2P room, publishes tracks, bob joins P2P room with automatic subscription
     * disabled, bob should auto subscribe
     *
     * @throws InterruptedException
     */
    @Test
    public void shouldSubscribeToTracksInP2PIfAutomaticSubscriptionDisabled()
            throws InterruptedException {
        initTokensWithTopology(Topology.P2P);
        videoRoom = RoomUtils.createRoom(roomName, Topology.P2P, false, null);

        SubscriptionCountHolder subscriptionCountHolder = new SubscriptionCountHolder();

        CallbackHelper.FakeRoomListener aliceRoomListener = new CallbackHelper.FakeRoomListener();
        CallbackHelper.FakeRoomListener bobRoomListener = new CallbackHelper.FakeRoomListener();
        aliceRoomListener.onConnectedLatch = new CountDownLatch(1);
        aliceRoomListener.onDisconnectedLatch = new CountDownLatch(1);

        aliceRoom = createRoom(tokens.get(0), aliceRoomListener, roomName, false);
        assertTrue(
                aliceRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant alice = aliceRoom.getLocalParticipant();
        assertNotNull(alice);

        Pair<Boolean, Track> audioPair = trackContainer.trackMap.get(AUDIO);
        if (audioPair.first) {
            audioPair = new Pair<Boolean, Track>(true, LocalAudioTrack.create(context, true));
            assertTrue(alice.publishTrack((LocalAudioTrack) audioPair.second));
            trackContainer.trackMap.put(AUDIO, audioPair);
            subscriptionCountHolder.subscribesExpected++;
        }

        Pair<Boolean, Track> videoPair = trackContainer.trackMap.get(VIDEO);
        if (videoPair.first) {
            videoPair =
                    new Pair<Boolean, Track>(
                            true, LocalVideoTrack.create(context, true, new FakeVideoCapturer()));
            assertTrue(alice.publishTrack((LocalVideoTrack) videoPair.second));
            trackContainer.trackMap.put(VIDEO, videoPair);
            subscriptionCountHolder.subscribesExpected++;
        }
        Pair<Boolean, Track> dataPair = trackContainer.trackMap.get(DATA);
        if (dataPair.first) {
            dataPair = new Pair<Boolean, Track>(true, LocalDataTrack.create(context));
            assertTrue(alice.publishTrack((LocalDataTrack) dataPair.second));
            trackContainer.trackMap.put(DATA, dataPair);
            subscriptionCountHolder.subscribesExpected++;
        }

        bobRoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onDisconnectedLatch = new CountDownLatch(1);

        bobRoom = createRoom(tokens.get(1), bobRoomListener, roomName, false);
        assertTrue(
                bobRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant bob = bobRoom.getLocalParticipant();
        assertNotNull(bob);

        bobRoom.getRemoteParticipants()
                .get(0)
                .setListener(
                        new RemoteParticipant.Listener() {
                            @Override
                            public void onAudioTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {
                                subscriptionCountHolder.subscribesReceived++;
                            }

                            @Override
                            public void onAudioTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onAudioTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {}

                            @Override
                            public void onVideoTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {
                                subscriptionCountHolder.subscribesReceived++;
                            }

                            @Override
                            public void onVideoTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {}

                            @Override
                            public void onDataTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {
                                subscriptionCountHolder.subscribesReceived++;
                            }

                            @Override
                            public void onDataTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onDataTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {}

                            @Override
                            public void onAudioTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onVideoTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}
                        });

        TestUtils.blockingWait(TestUtils.SMALL_WAIT);
        assertTrue(subscriptionCountHolder.didSucceed());

        tearDownRoom(aliceRoom, aliceRoomListener);
        tearDownRoom(bobRoom, bobRoomListener);
    }

    /**
     * Bob joins room, alice joins room, alice publishes tracks, bob should not subscribe
     *
     * @throws InterruptedException
     */
    @Test
    public void shouldNotSubscribeToTracksIfRemoteParticipantJoinsWithTracks()
            throws InterruptedException {
        initTokensWithTopology(Topology.GROUP);

        CallbackHelper.FakeRoomListener aliceRoomListener = new CallbackHelper.FakeRoomListener();
        CallbackHelper.FakeRoomListener bobRoomListener = new CallbackHelper.FakeRoomListener();
        aliceRoomListener.onConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onParticipantConnectedLatch = new CountDownLatch(1);

        bobRoom = createRoom(tokens.get(1), bobRoomListener, roomName, false);
        assertTrue(
                bobRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant bob = bobRoom.getLocalParticipant();
        assertNotNull(bob);

        aliceRoom = createRoom(tokens.get(0), aliceRoomListener, roomName, false);
        assertTrue(
                aliceRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant alice = aliceRoom.getLocalParticipant();
        assertNotNull(alice);
        assertTrue(
                bobRoomListener.onParticipantConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        bobRoom.getRemoteParticipants()
                .get(0)
                .setListener(
                        new RemoteParticipant.Listener() {
                            @Override
                            public void onAudioTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {
                                fail();
                            }

                            @Override
                            public void onAudioTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onAudioTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {}

                            @Override
                            public void onVideoTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {}

                            @Override
                            public void onDataTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {
                                fail();
                            }

                            @Override
                            public void onDataTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onDataTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {}

                            @Override
                            public void onAudioTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onVideoTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}
                        });

        Pair<Boolean, Track> audioPair = trackContainer.trackMap.get(AUDIO);
        if (audioPair.first) {
            audioPair = new Pair<Boolean, Track>(true, LocalAudioTrack.create(context, true));
            alice.publishTrack((LocalAudioTrack) audioPair.second);
            trackContainer.trackMap.put(AUDIO, audioPair);
        }

        Pair<Boolean, Track> videoPair = trackContainer.trackMap.get(VIDEO);
        if (videoPair.first) {
            videoPair =
                    new Pair<Boolean, Track>(
                            true, LocalVideoTrack.create(context, true, new FakeVideoCapturer()));
            alice.publishTrack((LocalVideoTrack) videoPair.second);
            trackContainer.trackMap.put(VIDEO, videoPair);
        }
        Pair<Boolean, Track> dataPair = trackContainer.trackMap.get(DATA);
        if (dataPair.first) {
            dataPair = new Pair<Boolean, Track>(true, LocalDataTrack.create(context));
            alice.publishTrack((LocalDataTrack) dataPair.second);
            trackContainer.trackMap.put(DATA, dataPair);
        }
        TestUtils.blockingWait(TestUtils.SMALL_WAIT);

        if (audioPair.first) {
            alice.unpublishTrack((LocalAudioTrack) audioPair.second);
        }

        if (videoPair.first) {
            alice.unpublishTrack((LocalVideoTrack) videoPair.second);
        }

        if (dataPair.first) {
            alice.unpublishTrack((LocalDataTrack) dataPair.second);
        }

        tearDownRoom(aliceRoom, aliceRoomListener);
        tearDownRoom(bobRoom, bobRoomListener);
    }

    /**
     * Alice joins room, bob joins room with automatic subscription disabled, alice publishes
     * tracks, bob should not subscribe
     *
     * @throws InterruptedException
     */
    @Test
    public void shouldNotSubscribeToTracksIfRemoteParticipantPublishesTracks()
            throws InterruptedException {
        initTokensWithTopology(Topology.GROUP);

        CallbackHelper.FakeRoomListener aliceRoomListener = new CallbackHelper.FakeRoomListener();
        CallbackHelper.FakeRoomListener bobRoomListener = new CallbackHelper.FakeRoomListener();
        aliceRoomListener.onConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onParticipantConnectedLatch = new CountDownLatch(1);

        SubscriptionCountHolder countHolder = new SubscriptionCountHolder();

        aliceRoom = createRoom(tokens.get(0), aliceRoomListener, roomName, false);
        assertTrue(
                aliceRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant alice = aliceRoom.getLocalParticipant();
        assertNotNull(alice);

        bobRoom = createRoom(tokens.get(1), bobRoomListener, roomName, false);
        assertTrue(
                bobRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant bob = bobRoom.getLocalParticipant();
        assertNotNull(bob);

        bobRoom.getRemoteParticipants()
                .get(0)
                .setListener(
                        new RemoteParticipant.Listener() {
                            @Override
                            public void onAudioTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {
                                countHolder.subscribesReceived++;
                            }

                            @Override
                            public void onAudioTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {
                                fail();
                            }

                            @Override
                            public void onAudioTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onAudioTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {}

                            @Override
                            public void onVideoTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {
                                countHolder.subscribesReceived++;
                            }

                            @Override
                            public void onVideoTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {}

                            @Override
                            public void onDataTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication remoteDataTrackPublication) {
                                countHolder.subscribesReceived++;
                            }

                            @Override
                            public void onDataTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {
                                fail();
                            }

                            @Override
                            public void onDataTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onDataTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {}

                            @Override
                            public void onAudioTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onVideoTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}
                        });

        Pair<Boolean, Track> audioPair = trackContainer.trackMap.get(AUDIO);
        if (audioPair.first) {
            audioPair = new Pair<Boolean, Track>(true, LocalAudioTrack.create(context, true));
            alice.publishTrack((LocalAudioTrack) audioPair.second);
            trackContainer.trackMap.put(AUDIO, audioPair);
            countHolder.subscribesExpected++;
        }

        Pair<Boolean, Track> videoPair = trackContainer.trackMap.get(VIDEO);
        if (videoPair.first) {
            videoPair =
                    new Pair<Boolean, Track>(
                            true, LocalVideoTrack.create(context, true, new FakeVideoCapturer()));
            alice.publishTrack((LocalVideoTrack) videoPair.second);
            trackContainer.trackMap.put(VIDEO, videoPair);
            countHolder.subscribesExpected++;
        }
        Pair<Boolean, Track> dataPair = trackContainer.trackMap.get(DATA);
        if (dataPair.first) {
            dataPair = new Pair<Boolean, Track>(true, LocalDataTrack.create(context));
            alice.publishTrack((LocalDataTrack) dataPair.second);
            trackContainer.trackMap.put(DATA, dataPair);
            countHolder.subscribesExpected++;
        }

        TestUtils.blockingWait(TestUtils.SMALL_WAIT);
        tearDownRoom(aliceRoom, aliceRoomListener);
        tearDownRoom(bobRoom, bobRoomListener);
    }

    /**
     * Bob joins room with automatic subscription disabled, alice joins room, bob publishes tracks,
     * alice should subscribe
     *
     * @throws InterruptedException
     */
    @Test
    public void canPublishTracksIfAutomaticSubscriptionDisabled() throws InterruptedException {
        initTokensWithTopology(Topology.GROUP);
        SubscriptionCountHolder subscriptionCountHolder = new SubscriptionCountHolder();

        CallbackHelper.FakeRoomListener aliceRoomListener = new CallbackHelper.FakeRoomListener();
        CallbackHelper.FakeRoomListener bobRoomListener = new CallbackHelper.FakeRoomListener();
        aliceRoomListener.onConnectedLatch = new CountDownLatch(1);
        aliceRoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onConnectedLatch = new CountDownLatch(1);

        bobRoom = createRoom(tokens.get(1), bobRoomListener, roomName, false);
        assertTrue(
                bobRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant bob = bobRoom.getLocalParticipant();
        assertNotNull(bob);

        aliceRoom = createRoom(tokens.get(0), aliceRoomListener, roomName, true);
        assertTrue(
                aliceRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant alice = aliceRoom.getLocalParticipant();
        assertNotNull(alice);

        aliceRoom
                .getRemoteParticipants()
                .get(0)
                .setListener(
                        new RemoteParticipant.Listener() {
                            @Override
                            public void onAudioTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {
                                subscriptionCountHolder.subscribesReceived++;
                            }

                            @Override
                            public void onAudioTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onAudioTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {}

                            @Override
                            public void onVideoTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {
                                subscriptionCountHolder.subscribesReceived++;
                            }

                            @Override
                            public void onVideoTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {}

                            @Override
                            public void onDataTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {
                                subscriptionCountHolder.subscribesReceived++;
                            }

                            @Override
                            public void onDataTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onDataTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {}

                            @Override
                            public void onAudioTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onVideoTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}
                        });

        Pair<Boolean, Track> audioPair = trackContainer.trackMap.get(AUDIO);
        if (audioPair.first) {
            audioPair = new Pair<Boolean, Track>(true, LocalAudioTrack.create(context, true));
            bob.publishTrack((LocalAudioTrack) audioPair.second);
            trackContainer.trackMap.put(AUDIO, audioPair);
            subscriptionCountHolder.subscribesExpected++;
        }

        Pair<Boolean, Track> videoPair = trackContainer.trackMap.get(VIDEO);
        if (videoPair.first) {
            videoPair =
                    new Pair<Boolean, Track>(
                            true, LocalVideoTrack.create(context, true, new FakeVideoCapturer()));
            bob.publishTrack((LocalVideoTrack) videoPair.second);
            trackContainer.trackMap.put(VIDEO, videoPair);
            subscriptionCountHolder.subscribesExpected++;
        }
        Pair<Boolean, Track> dataPair = trackContainer.trackMap.get(DATA);
        if (dataPair.first) {
            dataPair = new Pair<Boolean, Track>(true, LocalDataTrack.create(context));
            bob.publishTrack((LocalDataTrack) dataPair.second);
            trackContainer.trackMap.put(DATA, dataPair);
            subscriptionCountHolder.subscribesExpected++;
        }
        TestUtils.blockingWait(TestUtils.SMALL_WAIT);

        tearDownRoom(aliceRoom, aliceRoomListener);
        tearDownRoom(bobRoom, bobRoomListener);

        assertTrue(subscriptionCountHolder.didSucceed());
    }

    /**
     * Bob joins with automatic subscription disabled, alice joins, charlie joins, alice and charlie
     * should subscribe
     *
     * @throws InterruptedException
     */
    @Test
    public void canPublishTracksIfAutomaticSubscriptionDisabledMultiParty()
            throws InterruptedException {
        initTokensWithTopology(Topology.GROUP);
        SubscriptionCountHolder aliceSubscriptionCountHolder = new SubscriptionCountHolder();
        SubscriptionCountHolder charlieSubscriptionCountHolder = new SubscriptionCountHolder();

        CallbackHelper.FakeRoomListener aliceRoomListener = new CallbackHelper.FakeRoomListener();
        CallbackHelper.FakeRoomListener bobRoomListener = new CallbackHelper.FakeRoomListener();
        CallbackHelper.FakeRoomListener charlieRoomListener = new CallbackHelper.FakeRoomListener();

        aliceRoomListener.onConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onConnectedLatch = new CountDownLatch(1);
        charlieRoomListener.onConnectedLatch = new CountDownLatch(1);

        bobRoom = createRoom(tokens.get(1), bobRoomListener, roomName, false);
        assertTrue(
                bobRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant bob = bobRoom.getLocalParticipant();
        assertNotNull(bob);

        aliceRoom = createRoom(tokens.get(0), aliceRoomListener, roomName, true);
        assertTrue(
                aliceRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant alice = aliceRoom.getLocalParticipant();
        assertNotNull(alice);

        for (RemoteParticipant remoteParticipant : aliceRoom.getRemoteParticipants()) {
            if (remoteParticipant.getSid().equals(bob.getSid())) {
                remoteParticipant.setListener(
                        new RemoteParticipant.Listener() {
                            @Override
                            public void onAudioTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {
                                aliceSubscriptionCountHolder.subscribesReceived++;
                            }

                            @Override
                            public void onAudioTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onAudioTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {}

                            @Override
                            public void onVideoTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {
                                aliceSubscriptionCountHolder.subscribesReceived++;
                            }

                            @Override
                            public void onVideoTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {}

                            @Override
                            public void onDataTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {
                                aliceSubscriptionCountHolder.subscribesReceived++;
                            }

                            @Override
                            public void onDataTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onDataTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {}

                            @Override
                            public void onAudioTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onVideoTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}
                        });
            }
        }

        charlieRoom = createRoom(tokens.get(2), charlieRoomListener, roomName, true);
        assertTrue(
                charlieRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant charlie = charlieRoom.getLocalParticipant();
        assertNotNull(charlie);

        for (RemoteParticipant remoteParticipant : charlieRoom.getRemoteParticipants()) {
            if (remoteParticipant.getSid().equals(bob.getSid())) {
                remoteParticipant.setListener(
                        new RemoteParticipant.Listener() {
                            @Override
                            public void onAudioTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {
                                charlieSubscriptionCountHolder.subscribesReceived++;
                            }

                            @Override
                            public void onAudioTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onAudioTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {}

                            @Override
                            public void onVideoTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {
                                charlieSubscriptionCountHolder.subscribesReceived++;
                            }

                            @Override
                            public void onVideoTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {}

                            @Override
                            public void onDataTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {
                                charlieSubscriptionCountHolder.subscribesReceived++;
                            }

                            @Override
                            public void onDataTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onDataTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {}

                            @Override
                            public void onAudioTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onVideoTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}
                        });
            }
        }

        Pair<Boolean, Track> audioPair = trackContainer.trackMap.get(AUDIO);
        if (audioPair.first) {
            audioPair = new Pair<Boolean, Track>(true, LocalAudioTrack.create(context, true));
            bob.publishTrack((LocalAudioTrack) audioPair.second);
            trackContainer.trackMap.put(AUDIO, audioPair);
            aliceSubscriptionCountHolder.subscribesExpected++;
            charlieSubscriptionCountHolder.subscribesExpected++;
        }

        Pair<Boolean, Track> videoPair = trackContainer.trackMap.get(VIDEO);
        if (videoPair.first) {
            videoPair =
                    new Pair<Boolean, Track>(
                            true, LocalVideoTrack.create(context, true, new FakeVideoCapturer()));
            bob.publishTrack((LocalVideoTrack) videoPair.second);
            trackContainer.trackMap.put(VIDEO, videoPair);
            aliceSubscriptionCountHolder.subscribesExpected++;
            charlieSubscriptionCountHolder.subscribesExpected++;
        }
        Pair<Boolean, Track> dataPair = trackContainer.trackMap.get(DATA);
        if (dataPair.first) {
            dataPair = new Pair<Boolean, Track>(true, LocalDataTrack.create(context));
            bob.publishTrack((LocalDataTrack) dataPair.second);
            trackContainer.trackMap.put(DATA, dataPair);
            aliceSubscriptionCountHolder.subscribesExpected++;
            charlieSubscriptionCountHolder.subscribesExpected++;
        }
        TestUtils.blockingWait(TestUtils.SMALL_WAIT);

        tearDownRoom(aliceRoom, aliceRoomListener);
        tearDownRoom(bobRoom, bobRoomListener);
        tearDownRoom(charlieRoom, charlieRoomListener);

        assertTrue(aliceSubscriptionCountHolder.didSucceed());
        assertTrue(charlieSubscriptionCountHolder.didSucceed());
    }

    /**
     * Alice joins room, publishes tracks, bob joins with automatic subscription disabled, should
     * not subscribe
     *
     * @throws InterruptedException
     */
    @Test
    public void shouldReceivePublishEventsIfAutomaticSubscriptionDisabled()
            throws InterruptedException {
        SubscriptionCountHolder countHolder = new SubscriptionCountHolder();
        initTokensWithTopology(Topology.GROUP);

        CallbackHelper.FakeRoomListener aliceRoomListener = new CallbackHelper.FakeRoomListener();
        CallbackHelper.FakeRoomListener bobRoomListener = new CallbackHelper.FakeRoomListener();
        aliceRoomListener.onConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onConnectedLatch = new CountDownLatch(1);

        aliceRoom = createRoom(tokens.get(0), aliceRoomListener, roomName, false);
        assertTrue(
                aliceRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant alice = aliceRoom.getLocalParticipant();
        assertNotNull(alice);

        bobRoom = createRoom(tokens.get(1), bobRoomListener, roomName, false);
        assertTrue(
                bobRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        LocalParticipant bob = bobRoom.getLocalParticipant();
        assertNotNull(bob);
        bobRoom.getRemoteParticipants()
                .get(0)
                .setListener(
                        new RemoteParticipant.Listener() {
                            @Override
                            public void onAudioTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {
                                countHolder.subscribesReceived++;
                            }

                            @Override
                            public void onAudioTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {
                                fail();
                            }

                            @Override
                            public void onAudioTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onAudioTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    @NonNull RemoteAudioTrack remoteAudioTrack) {}

                            @Override
                            public void onVideoTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {

                                countHolder.subscribesReceived++;
                            }

                            @Override
                            public void onVideoTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    @NonNull RemoteVideoTrack remoteVideoTrack) {}

                            @Override
                            public void onDataTrackPublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication remoteDataTrackPublication) {
                                countHolder.subscribesReceived++;
                            }

                            @Override
                            public void onDataTrackUnpublished(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteDataTrackPublication
                                                    remoteDataTrackPublication) {}

                            @Override
                            public void onDataTrackSubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {
                                fail();
                            }

                            @Override
                            public void onDataTrackSubscriptionFailed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onDataTrackUnsubscribed(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                    @NonNull RemoteDataTrack remoteDataTrack) {}

                            @Override
                            public void onAudioTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onAudioTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteAudioTrackPublication
                                                    remoteAudioTrackPublication) {}

                            @Override
                            public void onVideoTrackEnabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}

                            @Override
                            public void onVideoTrackDisabled(
                                    @NonNull RemoteParticipant remoteParticipant,
                                    @NonNull
                                            RemoteVideoTrackPublication
                                                    remoteVideoTrackPublication) {}
                        });

        Pair<Boolean, Track> audioPair = trackContainer.trackMap.get(AUDIO);
        if (audioPair.first) {
            audioPair = new Pair<Boolean, Track>(true, LocalAudioTrack.create(context, true));
            alice.publishTrack((LocalAudioTrack) audioPair.second);
            trackContainer.trackMap.put(AUDIO, audioPair);
            countHolder.subscribesExpected++;
        }

        Pair<Boolean, Track> videoPair = trackContainer.trackMap.get(VIDEO);
        if (videoPair.first) {
            videoPair =
                    new Pair<Boolean, Track>(
                            true, LocalVideoTrack.create(context, true, new FakeVideoCapturer()));
            alice.publishTrack((LocalVideoTrack) videoPair.second);
            trackContainer.trackMap.put(VIDEO, videoPair);
            countHolder.subscribesExpected++;
        }
        Pair<Boolean, Track> dataPair = trackContainer.trackMap.get(DATA);
        if (dataPair.first) {
            dataPair = new Pair<Boolean, Track>(true, LocalDataTrack.create(context));
            alice.publishTrack((LocalDataTrack) dataPair.second);
            trackContainer.trackMap.put(DATA, dataPair);
            countHolder.subscribesExpected++;
        }

        TestUtils.blockingWait(TestUtils.SMALL_WAIT);

        tearDownRoom(aliceRoom, aliceRoomListener);
        tearDownRoom(bobRoom, bobRoomListener);

        assertTrue(countHolder.didSucceed());
    }

    private Room createRoom(
            String token,
            CallbackHelper.FakeRoomListener listener,
            String roomName,
            boolean enableAutomaticSubscription)
            throws InterruptedException {
        listener.onConnectedLatch = new CountDownLatch(1);
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token)
                        .roomName(roomName)
                        .enableAutomaticSubscription(enableAutomaticSubscription)
                        .iceOptions(iceOptions)
                        .build();
        Room room = Video.connect(context, connectOptions, listener);
        assertTrue(
                listener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        return room;
    }

    private void tearDownRoom(Room room, CallbackHelper.FakeRoomListener listener)
            throws InterruptedException {
        listener.onDisconnectedLatch = new CountDownLatch(1);
        room.disconnect();
        assertTrue(
                listener.onDisconnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
    }

    private void initTokensWithTopology(Topology topology) {
        tokens = new ArrayList<>();
        for (int i = 0; i < PARTICIPANT_NUM; i++) {
            tokens.add(CredentialsUtils.getAccessToken(PARTICIPANTS[i], topology));
        }
    }

    class SubscriptionCountHolder {
        int subscribesExpected = 0;
        int subscribesReceived = 0;

        boolean didSucceed() {
            return subscribesExpected == subscribesReceived;
        }
    }
}
