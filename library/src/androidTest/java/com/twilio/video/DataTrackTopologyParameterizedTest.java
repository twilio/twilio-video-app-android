/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import static com.twilio.video.util.VideoAssert.assertIsTrackSid;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import com.twilio.video.base.BaseParticipantTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.Topology;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@LargeTest
public class DataTrackTopologyParameterizedTest extends BaseParticipantTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {{Topology.P2P}, {Topology.GROUP}, {Topology.GROUP_SMALL}});
    }

    private final Topology topology;
    private final CallbackHelper.FakeRemoteDataTrackListener dataTrackListener =
            new CallbackHelper.FakeRemoteDataTrackListener();

    public DataTrackTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        super.baseSetup(topology);
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void shouldHaveTrackSid() throws InterruptedException {
        publishDataTrack();

        // Validate track was added
        List<RemoteDataTrackPublication> remoteDataTrackPublications =
                bobRemoteParticipant.getRemoteDataTracks();
        assertEquals(1, remoteDataTrackPublications.size());

        // Validate track sid
        assertTrue(remoteDataTrackPublications.get(0).isTrackSubscribed());
        assertIsTrackSid(remoteDataTrackPublications.get(0).getTrackSid());
        assertIsTrackSid(remoteDataTrackPublications.get(0).getRemoteDataTrack().getSid());
    }

    @Test
    public void canSendMessage() throws InterruptedException {
        publishDataTrack();
        String expectedMessage = "Hello DataTrack!";
        dataTrackListener.onStringMessageLatch = new CountDownLatch(1);
        bobLocalDataTrack.send(expectedMessage);
        assertTrue(dataTrackListener.onStringMessageLatch.await(20, TimeUnit.SECONDS));
        assertEquals(Integer.valueOf(1), dataTrackListener.messages.get(0).first);
        assertEquals(expectedMessage, dataTrackListener.messages.get(0).second);
    }

    @Test
    public void canSendMessageWithUnorderedDataTrack() throws InterruptedException {
        DataTrackOptions dataTrackOptions = new DataTrackOptions.Builder().ordered(false).build();
        publishDataTrack(dataTrackOptions);
        String firstExpectedMessage = "Hello";
        String secondExpectedMessage = "DataTrack!";
        dataTrackListener.onStringMessageLatch = new CountDownLatch(2);
        bobLocalDataTrack.send(firstExpectedMessage);
        bobLocalDataTrack.send(secondExpectedMessage);
        assertTrue(dataTrackListener.onStringMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(dataTrackListener.messagesSet.contains(firstExpectedMessage));
        assertTrue(dataTrackListener.messagesSet.contains(secondExpectedMessage));
    }

    @Test
    public void canSendBufferMessageWithUnorderedDataTrack() throws InterruptedException {
        DataTrackOptions dataTrackOptions = new DataTrackOptions.Builder().ordered(false).build();
        publishDataTrack(dataTrackOptions);
        ByteBuffer firstExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0x1, 0x2});
        ByteBuffer secondExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0xa, 0xb});
        dataTrackListener.onBufferMessageLatch = new CountDownLatch(2);
        bobLocalDataTrack.send(firstExpectedBufferMessage);
        bobLocalDataTrack.send(secondExpectedBufferMessage);
        assertTrue(dataTrackListener.onBufferMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(dataTrackListener.bufferMessagesSet.contains(firstExpectedBufferMessage));
        assertTrue(dataTrackListener.bufferMessagesSet.contains(secondExpectedBufferMessage));
    }

    @Test
    public void canSendMessageWithMaxPacketLifeTime() throws InterruptedException {
        DataTrackOptions dataTrackOptions =
                new DataTrackOptions.Builder().maxPacketLifeTime(1000).build();
        publishDataTrack(dataTrackOptions);
        String firstExpectedMessage = "Hello";
        String secondExpectedMessage = "DataTrack!";
        dataTrackListener.onStringMessageLatch = new CountDownLatch(2);
        bobLocalDataTrack.send(firstExpectedMessage);
        bobLocalDataTrack.send(secondExpectedMessage);
        assertTrue(dataTrackListener.onStringMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(dataTrackListener.messagesSet.contains(firstExpectedMessage));
        assertTrue(dataTrackListener.messagesSet.contains(secondExpectedMessage));
    }

    @Test
    public void canSendBufferMessageWithMaxPacketLifeTime() throws InterruptedException {
        DataTrackOptions dataTrackOptions =
                new DataTrackOptions.Builder().maxPacketLifeTime(1000).build();
        publishDataTrack(dataTrackOptions);
        ByteBuffer firstExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0x1, 0x2});
        ByteBuffer secondExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0xa, 0xb});
        dataTrackListener.onBufferMessageLatch = new CountDownLatch(2);
        bobLocalDataTrack.send(firstExpectedBufferMessage);
        bobLocalDataTrack.send(secondExpectedBufferMessage);
        assertTrue(dataTrackListener.onBufferMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(dataTrackListener.bufferMessagesSet.contains(firstExpectedBufferMessage));
        assertTrue(dataTrackListener.bufferMessagesSet.contains(secondExpectedBufferMessage));
    }

    @Test
    public void canSendMessageWithMaxRetransmits() throws InterruptedException {
        DataTrackOptions dataTrackOptions =
                new DataTrackOptions.Builder().maxRetransmits(1000).build();
        publishDataTrack(dataTrackOptions);
        String firstExpectedMessage = "Hello";
        String secondExpectedMessage = "DataTrack!";
        dataTrackListener.onStringMessageLatch = new CountDownLatch(2);
        bobLocalDataTrack.send(firstExpectedMessage);
        bobLocalDataTrack.send(secondExpectedMessage);
        assertTrue(dataTrackListener.onStringMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(dataTrackListener.messagesSet.contains(firstExpectedMessage));
        assertTrue(dataTrackListener.messagesSet.contains(secondExpectedMessage));
    }

    @Test
    public void canSendBufferMessageWithMaxRetransmits() throws InterruptedException {
        DataTrackOptions dataTrackOptions =
                new DataTrackOptions.Builder().maxRetransmits(1000).build();
        publishDataTrack(dataTrackOptions);
        ByteBuffer firstExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0x1, 0x2});
        ByteBuffer secondExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0xa, 0xb});
        dataTrackListener.onBufferMessageLatch = new CountDownLatch(2);
        bobLocalDataTrack.send(firstExpectedBufferMessage);
        bobLocalDataTrack.send(secondExpectedBufferMessage);
        assertTrue(dataTrackListener.onBufferMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(dataTrackListener.bufferMessagesSet.contains(firstExpectedBufferMessage));
        assertTrue(dataTrackListener.bufferMessagesSet.contains(secondExpectedBufferMessage));
    }

    @Test
    public void canSendBufferMessage() throws InterruptedException {
        publishDataTrack();
        ByteBuffer expectedMessageBuffer = ByteBuffer.wrap(new byte[] {0x0, 0x1, 0x2, 0x3});
        dataTrackListener.onBufferMessageLatch = new CountDownLatch(1);
        bobLocalDataTrack.send(expectedMessageBuffer);
        assertTrue(dataTrackListener.onBufferMessageLatch.await(20, TimeUnit.SECONDS));
        assertEquals(Integer.valueOf(1), dataTrackListener.bufferMessages.get(0).first);
        assertArrayEquals(
                expectedMessageBuffer.array(),
                dataTrackListener.bufferMessages.get(0).second.array());
    }

    @Test
    public void canSendMultipleMessages() throws InterruptedException {
        publishDataTrack();
        String firstExpectedMessage = "Hello";
        ByteBuffer firstExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0x0, 0x1, 0x2, 0x3});
        String secondExpectedMessage = "DataTrack!";
        ByteBuffer secondExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0xa, 0xb, 0xc, 0xd});
        dataTrackListener.onStringMessageLatch = new CountDownLatch(2);
        dataTrackListener.onBufferMessageLatch = new CountDownLatch(2);
        bobLocalDataTrack.send(firstExpectedMessage);
        bobLocalDataTrack.send(firstExpectedBufferMessage);
        bobLocalDataTrack.send(secondExpectedMessage);
        bobLocalDataTrack.send(secondExpectedBufferMessage);
        assertTrue(dataTrackListener.onStringMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(dataTrackListener.onBufferMessageLatch.await(20, TimeUnit.SECONDS));
        assertEquals(Integer.valueOf(1), dataTrackListener.messages.get(0).first);
        assertEquals(firstExpectedMessage, dataTrackListener.messages.get(0).second);
        assertEquals(Integer.valueOf(2), dataTrackListener.bufferMessages.get(0).first);
        assertArrayEquals(
                firstExpectedBufferMessage.array(),
                dataTrackListener.bufferMessages.get(0).second.array());
        assertEquals(Integer.valueOf(3), dataTrackListener.messages.get(1).first);
        assertEquals(secondExpectedMessage, dataTrackListener.messages.get(1).second);
        assertEquals(Integer.valueOf(4), dataTrackListener.bufferMessages.get(1).first);
        assertArrayEquals(
                secondExpectedBufferMessage.array(),
                dataTrackListener.bufferMessages.get(1).second.array());
    }

    @Test
    public void canSendMessagesOnMultipleDataTracks() throws InterruptedException {
        // Publish multiple data tracks
        LocalDataTrack bobFirstLocalDataTrack = LocalDataTrack.create(mediaTestActivity);
        LocalDataTrack bobSecondLocalDataTrack = LocalDataTrack.create(mediaTestActivity);
        publishDataTrack(bobFirstLocalDataTrack);
        publishDataTrack(bobSecondLocalDataTrack);

        String firstExpectedMessage = "Hello";
        ByteBuffer firstExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0x0, 0x1, 0x2, 0x3});
        String secondExpectedMessage = "DataTrack!";
        ByteBuffer secondExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0xa, 0xb, 0xc, 0xd});
        dataTrackListener.onStringMessageLatch = new CountDownLatch(2);
        dataTrackListener.onBufferMessageLatch = new CountDownLatch(2);

        // Send messages on different data tracks
        bobFirstLocalDataTrack.send(firstExpectedMessage);
        bobFirstLocalDataTrack.send(firstExpectedBufferMessage);
        bobSecondLocalDataTrack.send(secondExpectedMessage);
        bobSecondLocalDataTrack.send(secondExpectedBufferMessage);

        // Validate the messages
        assertTrue(dataTrackListener.onStringMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(dataTrackListener.onBufferMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(dataTrackListener.messagesSet.contains(firstExpectedMessage));
        assertTrue(dataTrackListener.messagesSet.contains(secondExpectedMessage));
        assertTrue(dataTrackListener.bufferMessagesSet.contains(firstExpectedBufferMessage));
        assertTrue(dataTrackListener.bufferMessagesSet.contains(secondExpectedBufferMessage));

        // Release data tracks
        bobFirstLocalDataTrack.release();
        bobSecondLocalDataTrack.release();
    }

    @Test
    public void canSendMessagesToMultipleParticipants() throws InterruptedException {
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        aliceRoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        participantListener.onSubscribedToDataTrackLatch = new CountDownLatch(2);
        participantListener.onDataTrackPublishedLatch = new CountDownLatch(2);
        CallbackHelper.FakeRemoteDataTrackListener aliceDataTrackListener =
                new CallbackHelper.FakeRemoteDataTrackListener();
        CallbackHelper.FakeRemoteDataTrackListener bobDataTrackListener =
                new CallbackHelper.FakeRemoteDataTrackListener();

        // Connect charlie
        String charlieToken =
                CredentialsUtils.getAccessToken(Constants.PARTICIPANT_CHARLIE, topology);
        ConnectOptions charlieConnectOptions =
                new ConnectOptions.Builder(charlieToken).roomName(testRoomName).build();
        charlieRoom = connect(charlieConnectOptions, charlieRoomListener);

        // Alice and Bob wait to see charlie connected
        assertTrue(aliceRoomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(bobRoomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        RemoteParticipant charlieAliceRemoteParticipant =
                getRemoteParticipant(Constants.PARTICIPANT_CHARLIE, aliceRoom);
        charlieAliceRemoteParticipant.setListener(participantListener);
        RemoteParticipant charlieBobRemoteParticipant =
                getRemoteParticipant(Constants.PARTICIPANT_CHARLIE, bobRoom);
        charlieBobRemoteParticipant.setListener(participantListener);

        // Charlie publish data track
        LocalParticipant charlieLocalParticipant = charlieRoom.getLocalParticipant();
        charlieLocalDataTrack = LocalDataTrack.create(mediaTestActivity);
        charlieLocalParticipant.publishTrack(charlieLocalDataTrack);
        assertTrue(participantListener.onDataTrackPublishedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(participantListener.onSubscribedToDataTrackLatch.await(20, TimeUnit.SECONDS));

        // Alice and bob observer charlie remote data track
        charlieAliceRemoteParticipant
                .getRemoteDataTracks()
                .get(0)
                .getRemoteDataTrack()
                .setListener(aliceDataTrackListener);
        charlieBobRemoteParticipant
                .getRemoteDataTracks()
                .get(0)
                .getRemoteDataTrack()
                .setListener(bobDataTrackListener);

        // Wait to ensure the data channels reach opened state
        Thread.sleep(1000);

        String firstExpectedMessage = "Hello";
        ByteBuffer firstExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0x0, 0x1, 0x2, 0x3});
        String secondExpectedMessage = "DataTrack!";
        ByteBuffer secondExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0xa, 0xb, 0xc, 0xd});
        aliceDataTrackListener.onStringMessageLatch = new CountDownLatch(2);
        aliceDataTrackListener.onBufferMessageLatch = new CountDownLatch(2);
        bobDataTrackListener.onStringMessageLatch = new CountDownLatch(2);
        bobDataTrackListener.onBufferMessageLatch = new CountDownLatch(2);

        // Charlie sends messages
        charlieLocalDataTrack.send(firstExpectedMessage);
        charlieLocalDataTrack.send(firstExpectedBufferMessage);
        charlieLocalDataTrack.send(secondExpectedMessage);
        charlieLocalDataTrack.send(secondExpectedBufferMessage);

        // Validate all messages were received
        assertTrue(aliceDataTrackListener.onStringMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(aliceDataTrackListener.onBufferMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(bobDataTrackListener.onStringMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(bobDataTrackListener.onBufferMessageLatch.await(20, TimeUnit.SECONDS));

        // Validate all messages received are correct
        assertEquals(Integer.valueOf(1), aliceDataTrackListener.messages.get(0).first);
        assertEquals(firstExpectedMessage, aliceDataTrackListener.messages.get(0).second);
        assertEquals(Integer.valueOf(2), aliceDataTrackListener.bufferMessages.get(0).first);
        assertArrayEquals(
                firstExpectedBufferMessage.array(),
                aliceDataTrackListener.bufferMessages.get(0).second.array());
        assertEquals(Integer.valueOf(3), aliceDataTrackListener.messages.get(1).first);
        assertEquals(secondExpectedMessage, aliceDataTrackListener.messages.get(1).second);
        assertEquals(Integer.valueOf(4), aliceDataTrackListener.bufferMessages.get(1).first);
        assertArrayEquals(
                secondExpectedBufferMessage.array(),
                aliceDataTrackListener.bufferMessages.get(1).second.array());
        assertEquals(Integer.valueOf(1), bobDataTrackListener.messages.get(0).first);
        assertEquals(firstExpectedMessage, bobDataTrackListener.messages.get(0).second);
        assertEquals(Integer.valueOf(2), bobDataTrackListener.bufferMessages.get(0).first);
        assertArrayEquals(
                firstExpectedBufferMessage.array(),
                bobDataTrackListener.bufferMessages.get(0).second.array());
        assertEquals(Integer.valueOf(3), bobDataTrackListener.messages.get(1).first);
        assertEquals(secondExpectedMessage, bobDataTrackListener.messages.get(1).second);
        assertEquals(Integer.valueOf(4), bobDataTrackListener.bufferMessages.get(1).first);
        assertArrayEquals(
                secondExpectedBufferMessage.array(),
                bobDataTrackListener.bufferMessages.get(1).second.array());
    }

    @Test
    public void canSendMessageToParticipantAfterAnotherDisconnects() throws InterruptedException {
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        aliceRoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        participantListener.onSubscribedToDataTrackLatch = new CountDownLatch(2);
        participantListener.onDataTrackPublishedLatch = new CountDownLatch(2);
        CallbackHelper.FakeRemoteDataTrackListener aliceDataTrackListener =
                new CallbackHelper.FakeRemoteDataTrackListener();
        CallbackHelper.FakeRemoteDataTrackListener bobDataTrackListener =
                new CallbackHelper.FakeRemoteDataTrackListener();

        // Connect charlie
        String charlieToken =
                CredentialsUtils.getAccessToken(Constants.PARTICIPANT_CHARLIE, topology);
        ConnectOptions charlieConnectOptions =
                new ConnectOptions.Builder(charlieToken).roomName(testRoomName).build();
        charlieRoom = connect(charlieConnectOptions, charlieRoomListener);

        // Alice and Bob wait to see charlie connected
        assertTrue(aliceRoomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(bobRoomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        RemoteParticipant charlieAliceRemoteParticipant =
                getRemoteParticipant(Constants.PARTICIPANT_CHARLIE, aliceRoom);
        charlieAliceRemoteParticipant.setListener(participantListener);
        RemoteParticipant charlieBobRemoteParticipant =
                getRemoteParticipant(Constants.PARTICIPANT_CHARLIE, bobRoom);
        charlieBobRemoteParticipant.setListener(participantListener);

        // Charlie publish data track
        LocalParticipant charlieLocalParticipant = charlieRoom.getLocalParticipant();
        charlieLocalDataTrack = LocalDataTrack.create(mediaTestActivity);
        charlieLocalParticipant.publishTrack(charlieLocalDataTrack);
        assertTrue(participantListener.onDataTrackPublishedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(participantListener.onSubscribedToDataTrackLatch.await(20, TimeUnit.SECONDS));

        // Alice and bob observer charlie remote data track
        charlieAliceRemoteParticipant
                .getRemoteDataTracks()
                .get(0)
                .getRemoteDataTrack()
                .setListener(aliceDataTrackListener);
        charlieBobRemoteParticipant
                .getRemoteDataTracks()
                .get(0)
                .getRemoteDataTrack()
                .setListener(bobDataTrackListener);

        // Wait to ensure the data channels reach opened state
        Thread.sleep(1000);

        String firstExpectedMessage = "Hello";
        ByteBuffer firstExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0x0, 0x1, 0x2, 0x3});
        String secondExpectedMessage = "DataTrack!";
        ByteBuffer secondExpectedBufferMessage = ByteBuffer.wrap(new byte[] {0xa, 0xb, 0xc, 0xd});
        aliceDataTrackListener.onStringMessageLatch = new CountDownLatch(2);
        aliceDataTrackListener.onBufferMessageLatch = new CountDownLatch(2);
        bobDataTrackListener.onStringMessageLatch = new CountDownLatch(2);
        bobDataTrackListener.onBufferMessageLatch = new CountDownLatch(2);

        // Charlie sends messages
        charlieLocalDataTrack.send(firstExpectedMessage);
        charlieLocalDataTrack.send(firstExpectedBufferMessage);
        charlieLocalDataTrack.send(secondExpectedMessage);
        charlieLocalDataTrack.send(secondExpectedBufferMessage);

        // Validate all messages were received
        assertTrue(aliceDataTrackListener.onStringMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(aliceDataTrackListener.onBufferMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(bobDataTrackListener.onStringMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(bobDataTrackListener.onBufferMessageLatch.await(20, TimeUnit.SECONDS));

        // Validate all messages received are correct
        assertEquals(Integer.valueOf(1), aliceDataTrackListener.messages.get(0).first);
        assertEquals(firstExpectedMessage, aliceDataTrackListener.messages.get(0).second);
        assertEquals(Integer.valueOf(2), aliceDataTrackListener.bufferMessages.get(0).first);
        assertArrayEquals(
                firstExpectedBufferMessage.array(),
                aliceDataTrackListener.bufferMessages.get(0).second.array());
        assertEquals(Integer.valueOf(3), aliceDataTrackListener.messages.get(1).first);
        assertEquals(secondExpectedMessage, aliceDataTrackListener.messages.get(1).second);
        assertEquals(Integer.valueOf(4), aliceDataTrackListener.bufferMessages.get(1).first);
        assertArrayEquals(
                secondExpectedBufferMessage.array(),
                aliceDataTrackListener.bufferMessages.get(1).second.array());
        assertEquals(Integer.valueOf(1), bobDataTrackListener.messages.get(0).first);
        assertEquals(firstExpectedMessage, bobDataTrackListener.messages.get(0).second);
        assertEquals(Integer.valueOf(2), bobDataTrackListener.bufferMessages.get(0).first);
        assertArrayEquals(
                firstExpectedBufferMessage.array(),
                bobDataTrackListener.bufferMessages.get(0).second.array());
        assertEquals(Integer.valueOf(3), bobDataTrackListener.messages.get(1).first);
        assertEquals(secondExpectedMessage, bobDataTrackListener.messages.get(1).second);
        assertEquals(Integer.valueOf(4), bobDataTrackListener.bufferMessages.get(1).first);
        assertArrayEquals(
                secondExpectedBufferMessage.array(),
                bobDataTrackListener.bufferMessages.get(1).second.array());

        // Alice disconnects
        disconnect(aliceRoom, aliceRoomListener);

        // Charlie sends same messages
        bobDataTrackListener.onStringMessageLatch = new CountDownLatch(2);
        bobDataTrackListener.onBufferMessageLatch = new CountDownLatch(2);
        charlieLocalDataTrack.send(firstExpectedMessage);
        charlieLocalDataTrack.send(firstExpectedBufferMessage);
        charlieLocalDataTrack.send(secondExpectedMessage);
        charlieLocalDataTrack.send(secondExpectedBufferMessage);

        // Validate all messages were received by Bob
        assertTrue(bobDataTrackListener.onStringMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(bobDataTrackListener.onBufferMessageLatch.await(20, TimeUnit.SECONDS));

        // Validate all messages Bob received are correct
        assertEquals(Integer.valueOf(5), bobDataTrackListener.messages.get(2).first);
        assertEquals(firstExpectedMessage, bobDataTrackListener.messages.get(2).second);
        assertEquals(Integer.valueOf(6), bobDataTrackListener.bufferMessages.get(2).first);
        assertArrayEquals(
                firstExpectedBufferMessage.array(),
                bobDataTrackListener.bufferMessages.get(2).second.array());
        assertEquals(Integer.valueOf(7), bobDataTrackListener.messages.get(3).first);
        assertEquals(secondExpectedMessage, bobDataTrackListener.messages.get(3).second);
        assertEquals(Integer.valueOf(8), bobDataTrackListener.bufferMessages.get(3).first);
        assertArrayEquals(
                secondExpectedBufferMessage.array(),
                bobDataTrackListener.bufferMessages.get(3).second.array());
    }

    @Test
    public void canSendAfterUnpublished() throws InterruptedException {
        publishDataTrack();
        String expectedMessage = "Hello";
        ByteBuffer expectedBufferMessage = ByteBuffer.wrap(new byte[] {0x0, 0x1, 0x2, 0x3});
        dataTrackListener.onStringMessageLatch = new CountDownLatch(1);
        dataTrackListener.onBufferMessageLatch = new CountDownLatch(1);
        bobLocalDataTrack.send(expectedMessage);
        bobLocalDataTrack.send(expectedBufferMessage);
        assertTrue(dataTrackListener.onStringMessageLatch.await(20, TimeUnit.SECONDS));
        assertTrue(dataTrackListener.onBufferMessageLatch.await(20, TimeUnit.SECONDS));
        assertEquals(Integer.valueOf(1), dataTrackListener.messages.get(0).first);
        assertEquals(expectedMessage, dataTrackListener.messages.get(0).second);
        assertEquals(Integer.valueOf(2), dataTrackListener.bufferMessages.get(0).first);
        assertArrayEquals(
                expectedBufferMessage.array(),
                dataTrackListener.bufferMessages.get(0).second.array());
        bobLocalParticipant.unpublishTrack(bobLocalDataTrack);

        // Try to send messages after unpublishing
        for (int i = 0; i < 5; i++) {
            bobLocalDataTrack.send(expectedMessage);
            bobLocalDataTrack.send(expectedBufferMessage);
        }
    }

    @Test
    public void onMessageReturnedOnSameThreadThatSetListener() throws InterruptedException {
        aliceParticipantListener.onSubscribedToDataTrackLatch = new CountDownLatch(1);
        aliceParticipantListener.onDataTrackPublishedLatch = new CountDownLatch(1);
        bobRemoteParticipant.setListener(aliceParticipantListener);
        bobLocalDataTrack = LocalDataTrack.create(mediaTestActivity);
        assertTrue(bobLocalParticipant.publishTrack(bobLocalDataTrack));
        assertTrue(aliceParticipantListener.onDataTrackPublishedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(
                aliceParticipantListener.onSubscribedToDataTrackLatch.await(20, TimeUnit.SECONDS));
        final RemoteDataTrack remoteDataTrack =
                bobRemoteParticipant.getRemoteDataTracks().get(0).getRemoteDataTrack();
        // Wait to ensure the data channels reach opened state
        Thread.sleep(1000);

        /*
         * Set listener on main thread
         */
        final AtomicReference<Long> expectedThreadId = new AtomicReference<>();
        final AtomicReference<Long> onBufferMessageThreadId = new AtomicReference<>();
        final AtomicReference<Long> onStringMessageThreadId = new AtomicReference<>();
        final CountDownLatch messagesReceived = new CountDownLatch(2);
        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(
                        () -> {
                            expectedThreadId.set(Thread.currentThread().getId());
                            final RemoteDataTrack.Listener dataTrackListener =
                                    new RemoteDataTrack.Listener() {
                                        @Override
                                        public void onMessage(
                                                RemoteDataTrack remoteDataTrack1,
                                                ByteBuffer messageBuffer) {
                                            onBufferMessageThreadId.set(
                                                    Thread.currentThread().getId());
                                            messagesReceived.countDown();
                                        }

                                        @Override
                                        public void onMessage(
                                                RemoteDataTrack remoteDataTrack1, String message) {
                                            onStringMessageThreadId.set(
                                                    Thread.currentThread().getId());
                                            messagesReceived.countDown();
                                        }
                                    };

                            remoteDataTrack.setListener(dataTrackListener);
                            bobLocalDataTrack.send("hello data track");
                            bobLocalDataTrack.send(ByteBuffer.wrap(new byte[] {0xf, 0xe}));
                        });

        assertTrue(messagesReceived.await(10, TimeUnit.SECONDS));
        assertEquals(expectedThreadId.get(), onBufferMessageThreadId.get());
        assertEquals(expectedThreadId.get(), onStringMessageThreadId.get());
    }

    private void publishDataTrack() throws InterruptedException {
        bobLocalDataTrack =
                LocalDataTrack.create(
                        mediaTestActivity, DataTrackOptions.DEFAULT_DATA_TRACK_OPTIONS);
        publishDataTrack(bobLocalDataTrack);
    }

    private void publishDataTrack(DataTrackOptions dataTrackOptions) throws InterruptedException {
        bobLocalDataTrack = LocalDataTrack.create(mediaTestActivity, dataTrackOptions);
        publishDataTrack(bobLocalDataTrack);
    }

    private void publishDataTrack(LocalDataTrack localDataTrack) throws InterruptedException {
        aliceParticipantListener.onSubscribedToDataTrackLatch = new CountDownLatch(1);
        aliceParticipantListener.onDataTrackPublishedLatch = new CountDownLatch(1);
        bobRemoteParticipant.setListener(aliceParticipantListener);
        bobLocalParticipantListener = new CallbackHelper.FakeLocalParticipantListener();
        bobLocalParticipantListener.onPublishedDataTrackLatch = new CountDownLatch(1);
        bobLocalParticipant.setListener(bobLocalParticipantListener);
        assertTrue(bobLocalParticipant.publishTrack(localDataTrack));
        assertTrue(
                bobLocalParticipantListener.onPublishedDataTrackLatch.await(20, TimeUnit.SECONDS));
        LocalDataTrackPublication localDataTrackPublication =
                getLocalDataTrackPublication(localDataTrack, bobLocalParticipant);
        assertTrue(aliceParticipantListener.onDataTrackPublishedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(
                aliceParticipantListener.onSubscribedToDataTrackLatch.await(20, TimeUnit.SECONDS));
        getRemoteDataTrack(localDataTrackPublication.getTrackSid(), bobRemoteParticipant)
                .setListener(dataTrackListener);

        // Wait to ensure the data channels reach opened state
        Thread.sleep(1000);
    }

    private RemoteParticipant getRemoteParticipant(String identity, Room room) {
        for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {
            if (remoteParticipant.getIdentity().equals(identity)) {
                return remoteParticipant;
            }
        }

        return null;
    }

    private LocalDataTrackPublication getLocalDataTrackPublication(
            LocalDataTrack localDataTrack, LocalParticipant localParticipant) {
        for (LocalDataTrackPublication localDataTrackPublication :
                localParticipant.getLocalDataTracks()) {
            if (localDataTrackPublication
                    .getLocalDataTrack()
                    .getName()
                    .equals(localDataTrack.getName())) {
                return localDataTrackPublication;
            }
        }

        return null;
    }

    private RemoteDataTrack getRemoteDataTrack(
            String trackSid, RemoteParticipant remoteParticipant) {
        for (RemoteDataTrackPublication remoteDataTrackPublication :
                remoteParticipant.getRemoteDataTracks()) {
            if (remoteDataTrackPublication.getTrackSid().equals(trackSid)) {
                return remoteDataTrackPublication.getRemoteDataTrack();
            }
        }

        return null;
    }
}
