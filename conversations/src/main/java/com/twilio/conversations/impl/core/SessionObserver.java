package com.twilio.conversations.impl.core;

import org.webrtc.VideoTrack;
import org.webrtc.AudioTrack;

public interface SessionObserver {
    void onSessionStateChanged(SessionState status);

    void onStartCompleted(CoreError error);

    void onStopCompleted(CoreError error);

    void onParticipantConnected(String participantIdentity, String participantSid, CoreError error);

    void onParticipantDisconnected(String participantIdentity, String participantSid, DisconnectReason reason);

    void onMediaStreamAdded(MediaStreamInfo stream);

    void onMediaStreamRemoved(MediaStreamInfo stream);

    void onVideoTrackAdded(TrackInfo trackInfo, VideoTrack videoTrack);

    void onVideoTrackFailedToAdd(TrackInfo trackInfo, CoreError error);

    void onVideoTrackRemoved(TrackInfo trackInfo);

    void onVideoTrackStateChanged(TrackInfo trackInfo);

    void onAudioTrackAdded(TrackInfo trackInfo, AudioTrack audioTrack);

    void onAudioTrackRemoved(TrackInfo trackInfo);

    void onAudioTrackStateChanged(TrackInfo trackInfo);

    void onReceiveTrackStatistics(CoreTrackStatsReport report);
}
