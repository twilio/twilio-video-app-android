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

package com.twilio.video.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.support.annotation.NonNull;
import android.util.Pair;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalAudioTrackPublication;
import com.twilio.video.LocalDataTrack;
import com.twilio.video.LocalDataTrackPublication;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.LocalVideoTrackPublication;
import com.twilio.video.NetworkQualityLevel;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.StatsListener;
import com.twilio.video.StatsReport;
import com.twilio.video.Track;
import com.twilio.video.TrackPublication;
import com.twilio.video.TwilioException;
import com.twilio.video.util.Sequence;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

public class CallbackHelper {

    private static void triggerLatch(@Nullable CountDownLatch latch) {
        if (latch != null) {
            latch.countDown();
        }
    }

    private static void addSequenceEvent(
            @Nullable Sequence sequence, @NonNull String sequenceEvent) {
        if (sequence != null) {
            sequence.addEvent(sequenceEvent);
        }
    }

    public static class FakeRoomListener implements Room.Listener {

        public Sequence sequence;
        public CountDownLatch onConnectedLatch;
        public CountDownLatch onConnectFailureLatch;
        public CountDownLatch onReconnectingLatch;
        public CountDownLatch onReconnectedLatch;
        public CountDownLatch onDisconnectedLatch;
        public CountDownLatch onDominantSpeakerChangedLatch;
        public CountDownLatch onParticipantConnectedLatch;
        public CountDownLatch onParticipantDisconnectedLatch;
        public CountDownLatch onRecordingStartedLatch;
        public CountDownLatch onRecordingStoppedLatch;

        private Room room;
        private TwilioException twilioException;
        private RemoteParticipant remoteParticipant;
        private RemoteParticipant dominantSpeaker;

        @Override
        public void onConnected(@NonNull Room room) {
            this.room = room;
            assertEquals(Room.State.CONNECTED, room.getState());
            triggerLatch(onConnectedLatch);
            addSequenceEvent(sequence, "onConnected");
        }

        @Override
        public void onConnectFailure(@NonNull Room room, @NonNull TwilioException twilioException) {
            this.room = room;
            this.twilioException = twilioException;
            assertEquals(Room.State.DISCONNECTED, room.getState());
            triggerLatch(onConnectFailureLatch);
            addSequenceEvent(sequence, "onConnectFailure");
        }

        @Override
        public void onReconnecting(@NonNull Room room, @NonNull TwilioException twilioException) {
            this.room = room;
            this.twilioException = twilioException;
            assertEquals(Room.State.RECONNECTING, room.getState());
            triggerLatch(onReconnectingLatch);
            addSequenceEvent(sequence, "onReconnecting");
        }

        @Override
        public void onReconnected(@NonNull Room room) {
            this.room = room;
            assertEquals(Room.State.CONNECTED, room.getState());
            triggerLatch(onReconnectedLatch);
            addSequenceEvent(sequence, "onReconnected");
        }

        @Override
        public void onDisconnected(@NonNull Room room, @Nullable TwilioException twilioException) {
            this.room = room;
            this.twilioException = twilioException;
            assertEquals(Room.State.DISCONNECTED, room.getState());
            triggerLatch(onDisconnectedLatch);
            addSequenceEvent(sequence, "onDisconnected");
        }

        @Override
        public void onParticipantConnected(
                @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
            this.room = room;
            this.remoteParticipant = remoteParticipant;
            triggerLatch(onParticipantConnectedLatch);
            addSequenceEvent(sequence, "onParticipantConnected");
        }

        @Override
        public void onParticipantDisconnected(
                @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
            this.room = room;
            this.remoteParticipant = remoteParticipant;
            triggerLatch(onParticipantDisconnectedLatch);
            addSequenceEvent(sequence, "onParticipantDisconnected");
        }

        @Override
        public void onDominantSpeakerChanged(
                @NonNull Room room,
                @android.support.annotation.Nullable RemoteParticipant remoteParticipant) {
            this.room = room;
            this.dominantSpeaker = remoteParticipant;
            triggerLatch(onDominantSpeakerChangedLatch);
            addSequenceEvent(sequence, "onDominantSpeakerChanged");
        }

        @Override
        public void onRecordingStarted(@NonNull Room room) {
            this.room = room;
            triggerLatch(onRecordingStartedLatch);
            addSequenceEvent(sequence, "onRecordingStarted");
        }

        @Override
        public void onRecordingStopped(@NonNull Room room) {
            this.room = room;
            triggerLatch(onRecordingStoppedLatch);
            addSequenceEvent(sequence, "onRecordingStopped");
        }

        public Room getRoom() {
            return room;
        }

        public TwilioException getTwilioException() {
            return twilioException;
        }

        public RemoteParticipant getRemoteParticipant() {
            return remoteParticipant;
        }

        public RemoteParticipant getDominantSpeaker() {
            return dominantSpeaker;
        }
    }

    public static class EmptyRoomListener implements Room.Listener {

        @Override
        public void onConnected(@NonNull Room room) {}

        @Override
        public void onConnectFailure(
                @NonNull Room room, @NonNull TwilioException twilioException) {}

        @Override
        public void onReconnecting(@NonNull Room room, @NonNull TwilioException twilioException) {}

        @Override
        public void onReconnected(@NonNull Room room) {}

        @Override
        public void onDisconnected(@NonNull Room room, @Nullable TwilioException twilioException) {}

        @Override
        public void onParticipantConnected(
                @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {}

        @Override
        public void onParticipantDisconnected(
                @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {}

        @Override
        public void onDominantSpeakerChanged(
                @NonNull Room room,
                @android.support.annotation.Nullable RemoteParticipant remoteParticipant) {}

        @Override
        public void onRecordingStarted(@NonNull Room room) {}

        @Override
        public void onRecordingStopped(@NonNull Room room) {}
    }

    public static class FakeParticipantListener implements RemoteParticipant.Listener {
        public CountDownLatch onAudioTrackPublishedLatch;
        public CountDownLatch onAudioTrackUnpublishedLatch;
        public CountDownLatch onSubscribedToAudioTrackLatch;
        public CountDownLatch onAudioTrackSubscriptionFailedLatch;
        public CountDownLatch onUnsubscribedFromAudioTrackLatch;
        public CountDownLatch onVideoTrackPublishedLatch;
        public CountDownLatch onVideoTrackUnpublishedLatch;
        public CountDownLatch onSubscribedToVideoTrackLatch;
        public CountDownLatch onVideoTrackSubscriptionFailedLatch;
        public CountDownLatch onUnsubscribedFromVideoTrackLatch;
        public CountDownLatch onDataTrackPublishedLatch;
        public CountDownLatch onDataTrackUnpublishedLatch;
        public CountDownLatch onSubscribedToDataTrackLatch;
        public CountDownLatch onDataTrackSubscriptionFailedLatch;
        public CountDownLatch onUnsubscribedFromDataTrackLatch;
        public CountDownLatch onAudioTrackEnabledLatch;
        public CountDownLatch onAudioTrackDisabledLatch;
        public CountDownLatch onVideoTrackEnabledLatch;
        public CountDownLatch onVideoTrackDisabledLatch;
        public final List<String> participantEvents = new ArrayList<>();
        public final Map<TrackPublication, TwilioException> subscriptionFailures =
                Collections.synchronizedMap(new HashMap<TrackPublication, TwilioException>());

        @Override
        public void onAudioTrackPublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            participantEvents.add("onAudioTrackPublished");
            triggerLatch(onAudioTrackPublishedLatch);
        }

        @Override
        public void onAudioTrackUnpublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            participantEvents.add("onAudioTrackUnpublished");
            triggerLatch(onAudioTrackUnpublishedLatch);
        }

        @Override
        public void onAudioTrackSubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                @NonNull RemoteAudioTrack remoteAudioTrack) {
            assertTrue(remoteAudioTrackPublication.isTrackSubscribed());
            participantEvents.add("onAudioTrackSubscribed");
            triggerLatch(onSubscribedToAudioTrackLatch);
        }

        @Override
        public void onAudioTrackSubscriptionFailed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                @NonNull TwilioException twilioException) {
            assertFalse(remoteAudioTrackPublication.isTrackSubscribed());
            participantEvents.add("onAudioTrackSubscriptionFailed");
            subscriptionFailures.put(remoteAudioTrackPublication, twilioException);
            triggerLatch(onAudioTrackSubscriptionFailedLatch);
        }

        @Override
        public void onAudioTrackUnsubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                @NonNull RemoteAudioTrack remoteAudioTrack) {
            assertFalse(remoteAudioTrackPublication.isTrackSubscribed());
            participantEvents.add("onAudioTrackUnsubscribed");
            triggerLatch(onUnsubscribedFromAudioTrackLatch);
        }

        @Override
        public void onVideoTrackPublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            participantEvents.add("onVideoTrackPublished");
            triggerLatch(onVideoTrackPublishedLatch);
        }

        @Override
        public void onVideoTrackUnpublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            participantEvents.add("onVideoTrackUnpublished");
            triggerLatch(onVideoTrackUnpublishedLatch);
        }

        @Override
        public void onVideoTrackSubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                @NonNull RemoteVideoTrack remoteVideoTrack) {
            assertTrue(remoteVideoTrackPublication.isTrackSubscribed());
            participantEvents.add("onVideoTrackSubscribed");
            triggerLatch(onSubscribedToVideoTrackLatch);
        }

        @Override
        public void onVideoTrackSubscriptionFailed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                @NonNull TwilioException twilioException) {
            assertFalse(remoteVideoTrackPublication.isTrackSubscribed());
            participantEvents.add("onVideoTrackSubscriptionFailed");
            subscriptionFailures.put(remoteVideoTrackPublication, twilioException);
            triggerLatch(onVideoTrackSubscriptionFailedLatch);
        }

        @Override
        public void onVideoTrackUnsubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                @NonNull RemoteVideoTrack remoteVideoTrack) {
            assertFalse(remoteVideoTrackPublication.isTrackSubscribed());
            participantEvents.add("onVideoTrackUnsubscribed");
            triggerLatch(onUnsubscribedFromVideoTrackLatch);
        }

        @Override
        public void onDataTrackPublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {
            participantEvents.add("onDataTrackPublished");
            triggerLatch(onDataTrackPublishedLatch);
        }

        @Override
        public void onDataTrackUnpublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {
            participantEvents.add("onDataTrackUnpublished");
            triggerLatch(onDataTrackUnpublishedLatch);
        }

        @Override
        public void onDataTrackSubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                @NonNull RemoteDataTrack remoteDataTrack) {
            assertTrue(remoteDataTrackPublication.isTrackSubscribed());
            participantEvents.add("onDataTrackSubscribed");
            triggerLatch(onSubscribedToDataTrackLatch);
        }

        @Override
        public void onDataTrackSubscriptionFailed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                @NonNull TwilioException twilioException) {
            assertFalse(remoteDataTrackPublication.isTrackSubscribed());
            participantEvents.add("onDataTrackSubscriptionFailed");
            subscriptionFailures.put(remoteDataTrackPublication, twilioException);
            triggerLatch(onDataTrackSubscriptionFailedLatch);
        }

        @Override
        public void onDataTrackUnsubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                @NonNull RemoteDataTrack remoteDataTrack) {
            assertFalse(remoteDataTrackPublication.isTrackSubscribed());
            participantEvents.add("onDataTrackUnsubscribed");
            triggerLatch(onUnsubscribedFromDataTrackLatch);
        }

        @Override
        public void onAudioTrackEnabled(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            assertTrue(remoteAudioTrackPublication.isTrackEnabled());
            participantEvents.add("onAudioTrackEnabled");
            triggerLatch(onAudioTrackEnabledLatch);
        }

        @Override
        public void onAudioTrackDisabled(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            assertFalse(remoteAudioTrackPublication.isTrackEnabled());
            participantEvents.add("onAudioTrackDisabled");
            triggerLatch(onAudioTrackDisabledLatch);
        }

        @Override
        public void onVideoTrackEnabled(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            assertTrue(remoteVideoTrackPublication.isTrackEnabled());
            participantEvents.add("onVideoTrackEnabled");
            triggerLatch(onVideoTrackEnabledLatch);
        }

        @Override
        public void onVideoTrackDisabled(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            assertFalse(remoteVideoTrackPublication.isTrackEnabled());
            participantEvents.add("onVideoTrackDisabled");
            triggerLatch(onVideoTrackDisabledLatch);
        }
    }

    public static class FakeStatsListener implements StatsListener {
        private List<StatsReport> statsReports;
        public CountDownLatch onStatsLatch;

        @Override
        public void onStats(@NonNull List<StatsReport> statsReports) {
            this.statsReports = statsReports;
            triggerLatch(onStatsLatch);
        }

        public List<StatsReport> getStatsReports() {
            return statsReports;
        }
    }

    public static class FakeLocalParticipantListener implements LocalParticipant.Listener {
        public CountDownLatch onPublishedAudioTrackLatch;
        public CountDownLatch onAudioTrackPublicationFailedLatch;
        public CountDownLatch onPublishedVideoTrackLatch;
        public CountDownLatch onVideoTrackPublicationFailedLatch;
        public CountDownLatch onPublishedDataTrackLatch;
        public CountDownLatch onDataTrackPublicationFailedLatch;
        public CountDownLatch onNetworkQualityLevelChangedLatch;
        public final Map<Track, TwilioException> publicationFailures =
                Collections.synchronizedMap(new HashMap<>());
        public final List<String> localParticipantEvents = new ArrayList<>();
        public List<NetworkQualityLevel> onNetworkLevelChangedEvents =
                Collections.synchronizedList(new ArrayList<>());

        @Override
        public void onAudioTrackPublished(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalAudioTrackPublication localAudioTrackPublication) {
            localParticipantEvents.add("onAudioTrackPublished");
            triggerLatch(onPublishedAudioTrackLatch);
        }

        @Override
        public void onAudioTrackPublicationFailed(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalAudioTrack localAudioTrack,
                @NonNull TwilioException twilioException) {
            localParticipantEvents.add("onAudioTrackPublicationFailed");
            publicationFailures.put(localAudioTrack, twilioException);
            triggerLatch(onAudioTrackPublicationFailedLatch);
        }

        @Override
        public void onVideoTrackPublished(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalVideoTrackPublication localVideoTrackPublication) {
            localParticipantEvents.add("onVideoTrackPublished");
            triggerLatch(onPublishedVideoTrackLatch);
        }

        @Override
        public void onVideoTrackPublicationFailed(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalVideoTrack localVideoTrack,
                @NonNull TwilioException twilioException) {
            localParticipantEvents.add("onVideoTrackPublicationFailed");
            publicationFailures.put(localVideoTrack, twilioException);
            triggerLatch(onVideoTrackPublicationFailedLatch);
        }

        @Override
        public void onDataTrackPublished(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalDataTrackPublication localDataTrackPublication) {
            localParticipantEvents.add("onDataTrackPublished");
            triggerLatch(onPublishedDataTrackLatch);
        }

        @Override
        public void onDataTrackPublicationFailed(
                @NonNull LocalParticipant localParticipant,
                @NonNull LocalDataTrack localDataTrack,
                @NonNull TwilioException twilioException) {
            localParticipantEvents.add("onDataTrackPublicationFailed");
            publicationFailures.put(localDataTrack, twilioException);
            triggerLatch(onDataTrackPublicationFailedLatch);
        }

        @Override
        public void onNetworkQualityLevelChanged(
                @NonNull LocalParticipant localParticipant,
                @NonNull NetworkQualityLevel networkQualityLevel) {
            localParticipantEvents.add("onNetworkQualityLevelChanged");
            onNetworkLevelChangedEvents.add(networkQualityLevel);
            triggerLatch(onNetworkQualityLevelChangedLatch);
        }
    }

    public static class FakeRemoteDataTrackListener implements RemoteDataTrack.Listener {
        public CountDownLatch onBufferMessageLatch;
        public CountDownLatch onStringMessageLatch;
        public final List<Pair<Integer, ByteBuffer>> bufferMessages = new ArrayList<>();
        public final List<Pair<Integer, String>> messages = new ArrayList<>();
        public final Set<String> messagesSet = Collections.synchronizedSet(new HashSet<String>());
        public final Set<ByteBuffer> bufferMessagesSet =
                Collections.synchronizedSet(new HashSet<ByteBuffer>());
        public final AtomicInteger messageCount = new AtomicInteger(1);

        @Override
        public void onMessage(
                @NonNull RemoteDataTrack remoteDataTrack, @NonNull ByteBuffer messageBuffer) {
            bufferMessages.add(new Pair<>(messageCount.getAndIncrement(), messageBuffer));
            bufferMessagesSet.add(messageBuffer);
            triggerLatch(onBufferMessageLatch);
        }

        @Override
        public void onMessage(@NonNull RemoteDataTrack remoteDataTrack, @NonNull String message) {
            messages.add(new Pair<>(messageCount.getAndIncrement(), message));
            messagesSet.add(message);
            triggerLatch(onStringMessageLatch);
        }
    }
}
