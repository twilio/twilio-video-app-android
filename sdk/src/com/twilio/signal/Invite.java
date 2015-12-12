package com.twilio.signal;

public interface Invite {
	
	public String from();
	
	public ConversationsClient to();
	
	/**
	 * Invoking this method allows the ConversationsClient to reject the invitation for joining the conversation.
	 */
	public void reject();
	
	/**
	 * Invoking this method allows the ConversationsClient to accept the invitation to join the conversation and set the delegate for handling
	 * conversation related events.
	 * 
	 */
	public Conversation accept(LocalMedia localMedia, ConversationListener listener) throws IllegalArgumentException;
	

}
