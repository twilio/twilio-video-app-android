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


import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalAudioTrackPublication;
import com.twilio.video.LocalVideoTrackPublication;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.StatsListener;
import com.twilio.video.StatsReport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
        public CountDownLatch onAudioTrackAddedLatch;
        public CountDownLatch onAudioTrackRemovedLatch;
        public CountDownLatch onSubscribedToAudioTrackLatch;
        public CountDownLatch onUnsubscribedFromAudioTrackLatch;
        public CountDownLatch onVideoTrackAddedLatch;
        public CountDownLatch onVideoTrackRemovedLatch;
        public CountDownLatch onSubscribedToVideoTrackLatch;
        public CountDownLatch onUnsubscribedFromVideoTrackLatch;
        public CountDownLatch onAudioTrackEnabledLatch;
        public CountDownLatch onAudioTrackDisabledLatch;
        public CountDownLatch onVideoTrackEnabledLatch;
        public CountDownLatch onVideoTrackDisabledLatch;
        public final List<String> participantEvents = new ArrayList<>();

        @Override
        public void onAudioTrackPublished(RemoteParticipant remoteParticipant,
                                          RemoteAudioTrackPublication remoteAudioTrackPublication) {
            participantEvents.add("onAudioTrackPublished");
            triggerLatch(onAudioTrackAddedLatch);
        }

        @Override
        public void onAudioTrackUnpublished(RemoteParticipant remoteParticipant,
                                            RemoteAudioTrackPublication remoteAudioTrackPublication) {
            participantEvents.add("onAudioTrackUnpublished");
            triggerLatch(onAudioTrackRemovedLatch);
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
            triggerLatch(onVideoTrackAddedLatch);
        }

        @Override
        public void onVideoTrackUnpublished(RemoteParticipant remoteParticipant,
                                            RemoteVideoTrackPublication remoteVideoTrackPublication) {
            participantEvents.add("onVideoTrackUnpublished");
            triggerLatch(onVideoTrackRemovedLatch);
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
        public void onVideoTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                             RemoteVideoTrackPublication remoteVideoTrackPublication,
                                             RemoteVideoTrack remoteVideoTrack) {
            assertFalse(remoteVideoTrackPublication.isTrackSubscribed());
            participantEvents.add("onVideoTrackUnsubscribed");
            triggerLatch(onUnsubscribedFromVideoTrackLatch);
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
        public CountDownLatch onPublishedVideoTrackLatch;
        public final List<String> localParticipantEvents = new ArrayList<>();

        @Override
        public void onAudioTrackPublished(LocalParticipant localParticipant,
                                          LocalAudioTrackPublication localAudioTrackPublication) {
            localParticipantEvents.add("onAudioTrackPublished");
            triggerLatch(onPublishedAudioTrackLatch);
        }

        @Override
        public void onVideoTrackPublished(LocalParticipant localParticipant,
                                          LocalVideoTrackPublication localVideoTrackPublication) {
            localParticipantEvents.add("onVideoTrackPublished");
            triggerLatch(onPublishedVideoTrackLatch);
        }
    }
}
