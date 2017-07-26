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


import com.twilio.video.AudioTrack;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.StatsListener;
import com.twilio.video.StatsReport;
import com.twilio.video.VideoTrack;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
        private Participant participant;

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
        public void onParticipantConnected(Room room, Participant participant) {
            this.room = room;
            this.participant = participant;
            triggerLatch(onParticipantConnectedLatch);
        }

        @Override
        public void onParticipantDisconnected(Room room, Participant participant) {
            this.room = room;
            this.participant = participant;
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

        public Participant getParticipant() {
            return participant;
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
        public void onParticipantConnected(Room room, Participant participant) {

        }

        @Override
        public void onParticipantDisconnected(Room room, Participant participant) {

        }

        @Override
        public void onRecordingStarted(Room room) {

        }

        @Override
        public void onRecordingStopped(Room room) {

        }
    }

    public static class FakeParticipantListener implements Participant.Listener {

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
        public void onAudioTrackAdded(Participant participant, AudioTrack audioTrack) {
            triggerLatch(onAudioTrackAddedLatch);
        }

        @Override
        public void onAudioTrackRemoved(Participant participant, AudioTrack audioTrack) {
            triggerLatch(onAudioTrackRemovedLatch);
        }

        @Override
        public void onVideoTrackAdded(Participant participant, VideoTrack videoTrack) {
            triggerLatch(onVideoTrackAddedLatch);
        }

        @Override
        public void onVideoTrackRemoved(Participant participant, VideoTrack videoTrack) {
            triggerLatch(onVideoTrackRemovedLatch);
        }

        @Override
        public void onAudioTrackEnabled(Participant participant, AudioTrack audioTrack) {
            assertTrue(audioTrack.isEnabled());
            triggerLatch(onAudioTrackEnabledLatch);
        }

        @Override
        public void onAudioTrackDisabled(Participant participant, AudioTrack audioTrack) {
            assertFalse(audioTrack.isEnabled());
            triggerLatch(onAudioTrackDisabledLatch);
        }

        @Override
        public void onVideoTrackEnabled(Participant participant, VideoTrack videoTrack) {
            assertTrue(videoTrack.isEnabled());
            triggerLatch(onVideoTrackEnabledLatch);
        }

        @Override
        public void onVideoTrackDisabled(Participant participant, VideoTrack videoTrack) {
            assertFalse(videoTrack.isEnabled());
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
