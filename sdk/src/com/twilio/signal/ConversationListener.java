package com.twilio.signal;

public interface ConversationListener {

	/**
	 * 
	 */
	void onParticipantConnected(Conversation conversation, Participant participant);
	
	/**
	 * 
	 */
	void onFailedToConnectParticipant(Conversation conversation, Participant participant, ConversationException e);
	
	
	/**
	 * 
	 */
	void onParticipantDisconnected(Conversation conversation, Participant participant);
	
	/**
	 * 
	 */
	void onLocalVideoAdded(Conversation conversation, LocalVideoTrack videoTrack);
	
	/**
	 * 
	 */
	void onLocalVideoRemoved(Conversation conversation, LocalVideoTrack videoTrack);
	
	/**
	 * 
	 */
	void onVideoAddedForParticipant(Conversation conversation, Participant participant, VideoTrack videoTrack);
	
	/**
	 * 
	 */
	void onVideoRemovedForParticipant(Conversation conversation, Participant participant, VideoTrack videoTrack);

	/**
	 * 
	 */
	void onLocalStatusChanged(Conversation conversation, Conversation.Status status);

	/**
	 * 
	 */
	void onConversationEnded(Conversation conversation, ConversationException e);

}
