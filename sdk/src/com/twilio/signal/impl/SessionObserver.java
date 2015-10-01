package com.twilio.signal.impl;

import org.webrtc.VideoTrack;

import com.twilio.signal.Conversation;

interface SessionObserver {

	void onConnectParticipant(String participant, CoreError error);

	void onFailToConnectParticipant(String participant, int error, String errorMessage);

	void onDisconnectParticipant(String participant);

	void onVideoAddedForParticipant(String participant);

	void onVideoRemovedForParticipant(String participant);

	void onLocalStatusChanged(Conversation.Status status);

	void onConversationEnded();

	void onConversationEnded(int error, String errorMessage);

	void onVideoTrackAdded(TrackInfo trackInfo, VideoTrack videoTrack);

	void onVideoTrackRemoved(TrackInfo trackInfo);

}

