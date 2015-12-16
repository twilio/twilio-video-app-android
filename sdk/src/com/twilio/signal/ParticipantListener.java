package com.twilio.signal;

public interface ParticipantListener {

	void onVideoTrackAdded(Conversation conversation, Participant participant, VideoTrack videoTrack);

	void onVideoTrackRemoved(Conversation conversation, Participant participant, VideoTrack videoTrack);

	void onAudioTrackAdded(Conversation conversation, Participant participant, AudioTrack audioTrack);

	void onAudioTrackRemoved(Conversation conversation, Participant participant, AudioTrack audioTrack);

	void onTrackEnabled(Conversation conversation, Participant participant, MediaTrack mediaTrack);

	void onTrackDisabled(Conversation conversation, Participant participant, MediaTrack mediaTrack);

}
