package com.twilio.signal.impl;

import org.webrtc.VideoTrack;

import com.twilio.signal.impl.core.CoreError;
import com.twilio.signal.impl.core.DisconnectReason;
import com.twilio.signal.impl.core.MediaStreamInfo;
import com.twilio.signal.impl.core.SessionState;
import com.twilio.signal.impl.core.TrackInfo;

interface SessionObserver {
	
	void onStartCompleted(CoreError error);
	
	void onStopCompleted(CoreError error);
	
	void onConnectParticipant(String participant, CoreError error);

	void onDisconnectParticipant(String participant, DisconnectReason reason);

	void onMediaStreamAdded(MediaStreamInfo stream);

	void onMediaStreamRemoved(MediaStreamInfo stream);

	void onLocalStatusChanged(SessionState status);

	void onVideoTrackAdded(TrackInfo trackInfo, VideoTrack videoTrack);

	void onVideoTrackRemoved(TrackInfo trackInfo);
	

}

