package com.twilio.signal.impl;

import org.webrtc.VideoTrack;

import com.twilio.signal.impl.core.DisconnectReason;
import com.twilio.signal.impl.core.SessionState;

interface SessionObserver {
	
	void onStartCompleted(CoreError error);
	
	void onStopCompleted(CoreError error);
	
	void onConnectParticipant(String participant, CoreError error);

	//void onFailToConnectParticipant(String participant, int error, String errorMessage);

	void onDisconnectParticipant(String participant, DisconnectReason reason);

	void onMediaStreamAdded(String participant);

	void onMediaStreamRemoved(String participant);

	void onLocalStatusChanged(SessionState status);

	void onConversationEnded();

	void onConversationEnded(int error, String errorMessage);

	void onVideoTrackAdded(TrackInfo trackInfo, VideoTrack videoTrack);

	void onVideoTrackRemoved(TrackInfo trackInfo);
	
	//void onReceiveSessionStatistics();

}

