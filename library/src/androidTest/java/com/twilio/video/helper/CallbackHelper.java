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
import com.twilio.video.PublishedAudioTrack;
import com.twilio.video.PublishedVideoTrack;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
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
        public void onAudioTrackAdded(RemoteParticipant remoteParticipant,
                                      RemoteAudioTrack remoteAudioTrack) {
            participantEvents.add("onAudioTrackAdded");
            triggerLatch(onAudioTrackAddedLatch);
        }

        @Override
        public void onAudioTrackRemoved(RemoteParticipant remoteParticipant,
                                        RemoteAudioTrack remoteAudioTrack) {
            participantEvents.add("onAudioTrackRemoved");
            triggerLatch(onAudioTrackRemovedLatch);
        }

        @Override
        public void onSubscribedToAudioTrack(RemoteParticipant remoteParticipant,
                                             RemoteAudioTrack remoteAudioTrack) {
            assertTrue(remoteAudioTrack.isSubscribed());
            participantEvents.add("onSubscribedToAudioTrack");
            triggerLatch(onSubscribedToAudioTrackLatch);
        }

        @Override
        public void onUnsubscribedFromAudioTrack(RemoteParticipant remoteParticipant,
                                                 RemoteAudioTrack remoteAudioTrack) {
            assertFalse(remoteAudioTrack.isSubscribed());
            participantEvents.add("onUnsubscribedFromAudioTrack");
            triggerLatch(onUnsubscribedFromAudioTrackLatch);
        }

        @Override
        public void onVideoTrackAdded(RemoteParticipant remoteParticipant,
                                      RemoteVideoTrack remoteVideoTrack) {
            participantEvents.add("onVideoTrackAdded");
            triggerLatch(onVideoTrackAddedLatch);
        }

        @Override
        public void onVideoTrackRemoved(RemoteParticipant remoteParticipant,
                                        RemoteVideoTrack remoteVideoTrack) {
            participantEvents.add("onVideoTrackRemoved");
            triggerLatch(onVideoTrackRemovedLatch);
        }

        @Override
        public void onSubscribedToVideoTrack(RemoteParticipant remoteParticipant,
                                             RemoteVideoTrack remoteVideoTrack) {
            assertTrue(remoteVideoTrack.isSubscribed());
            participantEvents.add("onSubscribedToVideoTrack");
            triggerLatch(onSubscribedToVideoTrackLatch);
        }

        @Override
        public void onUnsubscribedFromVideoTrack(RemoteParticipant remoteParticipant,
                                                 RemoteVideoTrack remoteVideoTrack) {
            assertFalse(remoteVideoTrack.isSubscribed());
            participantEvents.add("onUnsubscribedFromVideoTrack");
            triggerLatch(onUnsubscribedFromVideoTrackLatch);
        }

        @Override
        public void onAudioTrackEnabled(RemoteParticipant remoteParticipant,
                                        RemoteAudioTrack remoteAudioTrack) {
            assertTrue(remoteAudioTrack.isEnabled());
            participantEvents.add("onAudioTrackEnabled");
            triggerLatch(onAudioTrackEnabledLatch);
        }

        @Override
        public void onAudioTrackDisabled(RemoteParticipant remoteParticipant,
                                         RemoteAudioTrack remoteAudioTrack) {
            assertFalse(remoteAudioTrack.isEnabled());
            participantEvents.add("onAudioTrackDisabled");
            triggerLatch(onAudioTrackDisabledLatch);
        }

        @Override
        public void onVideoTrackEnabled(RemoteParticipant remoteParticipant,
                                        RemoteVideoTrack remoteVideoTrack) {
            assertTrue(remoteVideoTrack.isEnabled());
            participantEvents.add("onVideoTrackEnabled");
            triggerLatch(onVideoTrackEnabledLatch);
        }

        @Override
        public void onVideoTrackDisabled(RemoteParticipant remoteParticipant,
                                         RemoteVideoTrack remoteVideoTrack) {
            assertFalse(remoteVideoTrack.isEnabled());
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
        public void onPublishedAudioTrack(LocalParticipant localParticipant,
                                          PublishedAudioTrack publishedAudioTrack) {
            localParticipantEvents.add("onPublishedAudioTrack");
            triggerLatch(onPublishedAudioTrackLatch);
        }

        @Override
        public void onPublishedVideoTrack(LocalParticipant localParticipant,
                                          PublishedVideoTrack publishedVideoTrack) {
            localParticipantEvents.add("onPublishedVideoTrack");
            triggerLatch(onPublishedVideoTrackLatch);
        }
    }
}
