package com.twilio.signal;

/**
 * ConversationsClientListener interface defines a set of callbacks for events related to a
 * {@link ConversationsClient}.
 * 
 */
public interface ConversationsClientListener {

	/**
	 * This method notifies the listener that the conversationsClient is successfully listening for invitations.
	 *
	 * @param conversationsClient The conversationsClient that is listening for incoming connetions.
	 *
	 */
	public void onStartListeningForInvites(ConversationsClient conversationsClient);

	/**
	 * This method notifies the listener that the conversationsClient is no longer listening for invitations.
	 * 
	 * @param conversationsClient The conversationsClient that is no longer listening for incoming connections.
	 *
	 */
	public void onStopListeningForInvites(ConversationsClient conversationsClient);

	/**
	 * 
	 * This method notifies the listener that the conversations client failed to start listening for invitations.
	 * 
	 * @param conversationsClient The conversations client that failed to start listening for incoming connections.
	 * @param exception Exception containing information that caused the failure.
	 */
	public void onFailedToStartListening(ConversationsClient conversationsClient, ConversationException exception);
		
	/**
	 * This method notifies the listener that the conversationsClient has a pending invitation for a conversation.
	 *
	 * @param invite The invitation object.
	 */
	public void onReceiveConversationInvite(ConversationsClient conversationsClient, Invite invite);
	
}
