package com.twilio.signal;

public interface LocalMediaListener {

	void onLocalVideoTrackAdded(Conversation conversation, LocalVideoTrack videoTrack);

	void onLocalVideoTrackRemoved(Conversation conversation, LocalVideoTrack videoTrack);

}
