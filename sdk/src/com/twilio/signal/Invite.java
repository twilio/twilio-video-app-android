package com.twilio.signal;

public interface Invite {
	
	public String from();
	
	public Endpoint to();
	
	/**
	 * Invoking this method allows the Endpoint to reject the invitation for joining the conversation.
	 */
	public void reject();
	
	/**
	 * Invoking this method allows the Endpoint to accept the invitation to join the conversation and set the delegate for handling
	 * conversation related events.
	 * 
	 */
	public Conversation acceptWithLocalMedia(LocalMedia localMedia, ConversationListener listener);
	

}
