package com.twilio.video.helper;


import com.twilio.video.AudioTrack;
import com.twilio.video.Media;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.RoomException;
import com.twilio.video.StatsListener;
import com.twilio.video.StatsReport;
import com.twilio.video.VideoTrack;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class CallbackHelper {

    public static class FakeRoomListener implements Room.Listener {

        public CountDownLatch onConnectedLatch;
        public CountDownLatch onConnectFailureLatch;
        public CountDownLatch onDisconnectedLatch;
        public CountDownLatch onParticipantConnectedLatch;
        public CountDownLatch onParticipantDisconnectedLatch;

        private void triggerLatch(CountDownLatch latch) {
            if (latch != null) {
                latch.countDown();
            }
        }

        @Override
        public void onConnected(Room room) {
            triggerLatch(onConnectedLatch);
        }

        @Override
        public void onConnectFailure(Room room, RoomException roomException) {
            triggerLatch(onConnectFailureLatch);
        }

        @Override
        public void onDisconnected(Room room, RoomException roomException) {
            triggerLatch(onDisconnectedLatch);
        }

        @Override
        public void onParticipantConnected(Room room, Participant participant) {
            triggerLatch(onParticipantConnectedLatch);
        }

        @Override
        public void onParticipantDisconnected(Room room, Participant participant) {
            triggerLatch(onParticipantDisconnectedLatch);
        }
    }

    public static class EmptyRoomListener implements Room.Listener {

        @Override
        public void onConnected(Room room) {

        }

        @Override
        public void onConnectFailure(Room room, RoomException roomException) {

        }

        @Override
        public void onDisconnected(Room room, RoomException roomException) {

        }

        @Override
        public void onParticipantConnected(Room room, Participant participant) {

        }

        @Override
        public void onParticipantDisconnected(Room room, Participant participant) {

        }
    }

    public static class FakeMediaListener implements Media.Listener {

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
        public void onAudioTrackAdded(Media media, AudioTrack audioTrack) {
            triggerLatch(onAudioTrackAddedLatch);
        }

        @Override
        public void onAudioTrackRemoved(Media media, AudioTrack audioTrack) {
            triggerLatch(onAudioTrackRemovedLatch);
        }

        @Override
        public void onVideoTrackAdded(Media media, VideoTrack videoTrack) {
            triggerLatch(onVideoTrackAddedLatch);
        }

        @Override
        public void onVideoTrackRemoved(Media media, VideoTrack videoTrack) {
            triggerLatch(onVideoTrackRemovedLatch);
        }

        @Override
        public void onAudioTrackEnabled(Media media, AudioTrack audioTrack) {
            triggerLatch(onAudioTrackEnabledLatch);
        }

        @Override
        public void onAudioTrackDisabled(Media media, AudioTrack audioTrack) {
            triggerLatch(onAudioTrackDisabledLatch);
        }

        @Override
        public void onVideoTrackEnabled(Media media, VideoTrack videoTrack) {
            triggerLatch(onVideoTrackEnabledLatch);
        }

        @Override
        public void onVideoTrackDisabled(Media media, VideoTrack videoTrack) {
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
