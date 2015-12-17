package com.twilio.conversations;

public interface LocalMediaListener {

	void onLocalVideoTrackAdded(Conversation conversation, LocalVideoTrack videoTrack);

	void onLocalVideoTrackRemoved(Conversation conversation, LocalVideoTrack videoTrack);

}
