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


import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.StatsListener;
import com.twilio.video.StatsReport;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class CallbackHelper {

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

        private void triggerLatch(CountDownLatch latch) {
            if (latch != null) {
                latch.countDown();
            }
        }

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
        public CountDownLatch onVideoTrackAddedLatch;
        public CountDownLatch onVideoTrackRemovedLatch;
        public CountDownLatch onAudioTrackEnabledLatch;
        public CountDownLatch onAudioTrackDisabledLatch;
        public CountDownLatch onVideoTrackEnabledLatch;
        public CountDownLatch onVideoTrackDisabledLatch;



        private void triggerLatch(CountDownLatch latch) {
            if (latch != null) {
                latch.countDown();
            }
        }

        @Override
        public void onAudioTrackAdded(RemoteParticipant remoteParticipant, RemoteAudioTrack remoteAudioTrack) {
            triggerLatch(onAudioTrackAddedLatch);
        }

        @Override
        public void onAudioTrackRemoved(RemoteParticipant remoteParticipant, RemoteAudioTrack remoteAudioTrack) {
            triggerLatch(onAudioTrackRemovedLatch);
        }

        @Override
        public void onVideoTrackAdded(RemoteParticipant remoteParticipant, RemoteVideoTrack remoteVideoTrack) {
            triggerLatch(onVideoTrackAddedLatch);
        }

        @Override
        public void onVideoTrackRemoved(RemoteParticipant remoteParticipant, RemoteVideoTrack remoteVideoTrack) {
            triggerLatch(onVideoTrackRemovedLatch);
        }

        @Override
        public void onAudioTrackEnabled(RemoteParticipant remoteParticipant, RemoteAudioTrack remoteAudioTrack) {
            triggerLatch(onAudioTrackEnabledLatch);
        }

        @Override
        public void onAudioTrackDisabled(RemoteParticipant remoteParticipant, RemoteAudioTrack remoteAudioTrack) {
            triggerLatch(onAudioTrackDisabledLatch);
        }

        @Override
        public void onVideoTrackEnabled(RemoteParticipant remoteParticipant, RemoteVideoTrack remoteVideoTrack) {
            triggerLatch(onVideoTrackEnabledLatch);
        }

        @Override
        public void onVideoTrackDisabled(RemoteParticipant remoteParticipant, RemoteVideoTrack remoteVideoTrack) {
            triggerLatch(onVideoTrackDisabledLatch);
        }
    }

    public static class FakeStatsListener implements StatsListener {
        private List<StatsReport> statsReports;
        public CountDownLatch onStatsLatch;

        private void triggerLatch(CountDownLatch latch) {
            if (latch != null) {
                latch.countDown();
            }
        }

        @Override
        public void onStats(List<StatsReport> statsReports) {
            this.statsReports = statsReports;
            triggerLatch(onStatsLatch);
        }

        public List<StatsReport> getStatsReports() {
            return statsReports;
        }
    }
}
