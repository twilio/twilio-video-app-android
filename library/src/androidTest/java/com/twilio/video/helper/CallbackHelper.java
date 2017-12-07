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


import android.util.Pair;

import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalDataTrack;
import com.twilio.video.LocalDataTrackPublication;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalAudioTrackPublication;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.LocalVideoTrackPublication;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.Track;
import com.twilio.video.TwilioException;
import com.twilio.video.StatsListener;
import com.twilio.video.StatsReport;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CallbackHelper {

    private static void triggerLatch(@Nullable CountDownLatch latch) {
        if (latch != null) {
            latch.countDown();
        }
    }

    public static class FakeRoomListener implements Room.Listener {

        public CountDownLatch onConnectedLatch;
        public CountDownLatch onConnectFailureLatch;
        public CountDownLatch onDisconnectedLatch;
        public CountDownLatch onParticipantConnectedLatch;
        public CountDownLatch onParticipantDisconnectedLatch;
        public CountDownLatch onRecordingStartedLatch;
        public CountDownLatch onRecordingStoppedLatch;

        private Room room;
        private TwilioException twilioException;
        private RemoteParticipant remoteParticipant;

        @Override
        public void onConnected(Room room) {
            this.room = room;
            triggerLatch(onConnectedLatch);
        }

        @Override
        public void onConnectFailure(Room room, TwilioException twilioException) {
            this.room = room;
            this.twilioException = twilioException;
            triggerLatch(onConnectFailureLatch);
        }

        @Override
        public void onDisconnected(Room room, TwilioException twilioException) {
            this.room = room;
            this.twilioException = twilioException;
            triggerLatch(onDisconnectedLatch);
        }

        @Override
        public void onParticipantConnected(Room room, RemoteParticipant remoteParticipant) {
            this.room = room;
            this.remoteParticipant = remoteParticipant;
            triggerLatch(onParticipantConnectedLatch);
        }

        @Override
        public void onParticipantDisconnected(Room room, RemoteParticipant remoteParticipant) {
            this.room = room;
            this.remoteParticipant = remoteParticipant;
            triggerLatch(onParticipantDisconnectedLatch);
        }

        @Override
        public void onRecordingStarted(Room room) {
            this.room = room;
            triggerLatch(onRecordingStartedLatch);
        }

        @Override
        public void onRecordingStopped(Room room) {
            this.room = room;
            triggerLatch(onRecordingStoppedLatch);
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
    }

    public static class EmptyRoomListener implements Room.Listener {

        @Override
        public void onConnected(Room room) {

        }

        @Override
        public void onConnectFailure(Room room, TwilioException twilioException) {

        }

        @Override
        public void onDisconnected(Room room, TwilioException twilioException) {

        }

        @Override
        public void onParticipantConnected(Room room, RemoteParticipant remoteParticipant) {

        }

        @Override
        public void onParticipantDisconnected(Room room, RemoteParticipant remoteParticipant) {

        }

        @Override
        public void onRecordingStarted(Room room) {

        }

        @Override
        public void onRecordingStopped(Room room) {

        }
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

        @Override
        public void onAudioTrackPublished(RemoteParticipant remoteParticipant,
                                          RemoteAudioTrackPublication remoteAudioTrackPublication) {
            participantEvents.add("onAudioTrackPublished");
            triggerLatch(onAudioTrackPublishedLatch);
        }

        @Override
        public void onAudioTrackUnpublished(RemoteParticipant remoteParticipant,
                                            RemoteAudioTrackPublication remoteAudioTrackPublication) {
            participantEvents.add("onAudioTrackUnpublished");
            triggerLatch(onAudioTrackUnpublishedLatch);
        }

        @Override
        public void onAudioTrackSubscribed(RemoteParticipant remoteParticipant,
                                           RemoteAudioTrackPublication remoteAudioTrackPublication,
                                           RemoteAudioTrack remoteAudioTrack) {
            assertTrue(remoteAudioTrackPublication.isTrackSubscribed());
            participantEvents.add("onAudioTrackSubscribed");
            triggerLatch(onSubscribedToAudioTrackLatch);
        }

        @Override
        public void onAudioTrackSubscriptionFailed(RemoteParticipant remoteParticipant,
                                                   RemoteAudioTrackPublication remoteAudioTrackPublication,
                                                   TwilioException twilioException) {
            assertFalse(remoteAudioTrackPublication.isTrackSubscribed());
            participantEvents.add("onAudioTrackSubscriptionFailed");
            triggerLatch(onAudioTrackSubscriptionFailedLatch);
        }

        @Override
        public void onAudioTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                             RemoteAudioTrackPublication remoteAudioTrackPublication,
                                             RemoteAudioTrack remoteAudioTrack) {
            assertFalse(remoteAudioTrackPublication.isTrackSubscribed());
            participantEvents.add("onAudioTrackUnsubscribed");
            triggerLatch(onUnsubscribedFromAudioTrackLatch);
        }

        @Override
        public void onVideoTrackPublished(RemoteParticipant remoteParticipant,
                                          RemoteVideoTrackPublication remoteVideoTrackPublication) {
            participantEvents.add("onVideoTrackPublished");
            triggerLatch(onVideoTrackPublishedLatch);
        }

        @Override
        public void onVideoTrackUnpublished(RemoteParticipant remoteParticipant,
                                            RemoteVideoTrackPublication remoteVideoTrackPublication) {
            participantEvents.add("onVideoTrackUnpublished");
            triggerLatch(onVideoTrackUnpublishedLatch);
        }

        @Override
        public void onVideoTrackSubscribed(RemoteParticipant remoteParticipant,
                                           RemoteVideoTrackPublication remoteVideoTrackPublication,
                                           RemoteVideoTrack remoteVideoTrack) {
            assertTrue(remoteVideoTrackPublication.isTrackSubscribed());
            participantEvents.add("onVideoTrackSubscribed");
            triggerLatch(onSubscribedToVideoTrackLatch);
        }

        @Override
        public void onVideoTrackSubscriptionFailed(RemoteParticipant remoteParticipant,
                                                   RemoteVideoTrackPublication remoteVideoTrackPublication,
                                                   TwilioException twilioException) {
            assertFalse(remoteVideoTrackPublication.isTrackSubscribed());
            participantEvents.add("onVideoTrackSubscriptionFailed");
            triggerLatch(onVideoTrackSubscriptionFailedLatch);
        }

        @Override
        public void onVideoTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                             RemoteVideoTrackPublication remoteVideoTrackPublication,
                                             RemoteVideoTrack remoteVideoTrack) {
            assertFalse(remoteVideoTrackPublication.isTrackSubscribed());
            participantEvents.add("onVideoTrackUnsubscribed");
            triggerLatch(onUnsubscribedFromVideoTrackLatch);
        }

        @Override
        public void onDataTrackPublished(RemoteParticipant remoteParticipant,
                                         RemoteDataTrackPublication remoteDataTrackPublication) {
            participantEvents.add("onDataTrackPublished");
            triggerLatch(onDataTrackPublishedLatch);
        }

        @Override
        public void onDataTrackUnpublished(RemoteParticipant remoteParticipant,
                                           RemoteDataTrackPublication remoteDataTrackPublication) {
            participantEvents.add("onDataTrackUnpublished");
            triggerLatch(onDataTrackUnpublishedLatch);
        }

        @Override
        public void onDataTrackSubscribed(RemoteParticipant remoteParticipant,
                                          RemoteDataTrackPublication remoteDataTrackPublication,
                                          RemoteDataTrack remoteDataTrack) {
            assertTrue(remoteDataTrackPublication.isTrackSubscribed());
            participantEvents.add("onDataTrackSubscribed");
            triggerLatch(onSubscribedToDataTrackLatch);
        }

        @Override
        public void onDataTrackSubscriptionFailed(RemoteParticipant remoteParticipant,
                                                  RemoteDataTrackPublication remoteDataTrackPublication,
                                                  TwilioException twilioException) {
            assertFalse(remoteDataTrackPublication.isTrackSubscribed());
            participantEvents.add("onDataTrackSubscriptionFailed");
            triggerLatch(onDataTrackSubscriptionFailedLatch);
        }

        @Override
        public void onDataTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                            RemoteDataTrackPublication remoteDataTrackPublication,
                                            RemoteDataTrack remoteDataTrack) {
            assertFalse(remoteDataTrackPublication.isTrackSubscribed());
            participantEvents.add("onDataTrackUnsubscribed");
            triggerLatch(onUnsubscribedFromDataTrackLatch);
        }

        @Override
        public void onAudioTrackEnabled(RemoteParticipant remoteParticipant,
                                        RemoteAudioTrackPublication remoteAudioTrackPublication) {
            assertTrue(remoteAudioTrackPublication.isTrackEnabled());
            participantEvents.add("onAudioTrackEnabled");
            triggerLatch(onAudioTrackEnabledLatch);
        }

        @Override
        public void onAudioTrackDisabled(RemoteParticipant remoteParticipant,
                                         RemoteAudioTrackPublication remoteAudioTrackPublication) {
            assertFalse(remoteAudioTrackPublication.isTrackEnabled());
            participantEvents.add("onAudioTrackDisabled");
            triggerLatch(onAudioTrackDisabledLatch);
        }

        @Override
        public void onVideoTrackEnabled(RemoteParticipant remoteParticipant,
                                        RemoteVideoTrackPublication remoteVideoTrackPublication) {
            assertTrue(remoteVideoTrackPublication.isTrackEnabled());
            participantEvents.add("onVideoTrackEnabled");
            triggerLatch(onVideoTrackEnabledLatch);
        }

        @Override
        public void onVideoTrackDisabled(RemoteParticipant remoteParticipant,
                                         RemoteVideoTrackPublication remoteVideoTrackPublication) {
            assertFalse(remoteVideoTrackPublication.isTrackEnabled());
            participantEvents.add("onVideoTrackDisabled");
            triggerLatch(onVideoTrackDisabledLatch);
        }
    }

    public static class FakeStatsListener implements StatsListener {
        private List<StatsReport> statsReports;
        public CountDownLatch onStatsLatch;

        @Override
        public void onStats(List<StatsReport> statsReports) {
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
        public final Map<Track, TwilioException> publicationFailures =
                Collections.synchronizedMap(new HashMap<Track, TwilioException>());
        public final List<String> localParticipantEvents = new ArrayList<>();

        @Override
        public void onAudioTrackPublished(LocalParticipant localParticipant,
                                          LocalAudioTrackPublication localAudioTrackPublication) {
            localParticipantEvents.add("onAudioTrackPublished");
            triggerLatch(onPublishedAudioTrackLatch);
        }

        @Override
        public void onAudioTrackPublicationFailed(LocalParticipant localParticipant,
                                                  LocalAudioTrack localAudioTrack,
                                                  TwilioException twilioException) {
            localParticipantEvents.add("onAudioTrackPublicationFailed");
            publicationFailures.put(localAudioTrack, twilioException);
            triggerLatch(onAudioTrackPublicationFailedLatch);
        }

        @Override
        public void onVideoTrackPublished(LocalParticipant localParticipant,
                                          LocalVideoTrackPublication localVideoTrackPublication) {
            localParticipantEvents.add("onVideoTrackPublished");
            triggerLatch(onPublishedVideoTrackLatch);
        }

        @Override
        public void onVideoTrackPublicationFailed(LocalParticipant localParticipant,
                                                  LocalVideoTrack localVideoTrack,
                                                  TwilioException twilioException) {
            localParticipantEvents.add("onVideoTrackPublicationFailed");
            publicationFailures.put(localVideoTrack, twilioException);
            triggerLatch(onVideoTrackPublicationFailedLatch);
        }

        @Override
        public void onDataTrackPublished(LocalParticipant localParticipant,
                                         LocalDataTrackPublication localDataTrackPublication) {
            localParticipantEvents.add("onDataTrackPublished");
            triggerLatch(onPublishedDataTrackLatch);
        }

        @Override
        public void onDataTrackPublicationFailed(LocalParticipant localParticipant,
                                                 LocalDataTrack localDataTrack,
                                                 TwilioException twilioException) {
            localParticipantEvents.add("onDataTrackPublicationFailed");
            publicationFailures.put(localDataTrack, twilioException);
            triggerLatch(onDataTrackPublicationFailedLatch);
        }
    }

    public static class FakeRemoteDataTrackListener implements RemoteDataTrack.Listener {
        public CountDownLatch onBufferMessageLatch;
        public CountDownLatch onStringMessageLatch;
        public final List<Pair<Integer, ByteBuffer>> bufferMessages = new ArrayList<>();
        public final List<Pair<Integer, String>> messages = new ArrayList<>();
        public final Set<String> messagesSet = Collections.synchronizedSet(new HashSet<String>());
        public final Set<ByteBuffer> bufferMessagesSet = Collections
                .synchronizedSet(new HashSet<ByteBuffer>());
        public final AtomicInteger messageCount = new AtomicInteger(1);

        @Override
        public void onMessage(RemoteDataTrack remoteDataTrack, ByteBuffer messageBuffer) {
            bufferMessages.add(new Pair<>(messageCount.getAndIncrement(), messageBuffer));
            bufferMessagesSet.add(messageBuffer);
            triggerLatch(onBufferMessageLatch);
        }

        @Override
        public void onMessage(RemoteDataTrack remoteDataTrack, String message) {
            messages.add(new Pair<>(messageCount.getAndIncrement(), message));
            messagesSet.add(message);
            triggerLatch(onStringMessageLatch);
        }
    }
}
