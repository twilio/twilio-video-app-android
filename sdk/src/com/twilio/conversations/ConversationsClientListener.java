package com.twilio.conversations;

/**
 * ConversationsClientListener interface defines a set of callbacks for events related to a
 * {@link ConversationsClient}.
 * 
 */
public interface ConversationsClientListener {

	/**
	 * This method notifies the listener that the client is successfully listening for incoming invitations.
	 *
	 * @param conversationsClient The client that is listening for incoming invitations.
	 *
	 */
	public void onStartListeningForInvites(ConversationsClient conversationsClient);

	/**
	 * This method notifies the listener that the client is no longer listening for invitations.
	 * 
	 * @param conversationsClient The client that is no longer listening for incoming invitations.
	 *
	 */
	public void onStopListeningForInvites(ConversationsClient conversationsClient);

	/**
	 * 
	 * This method notifies the listener that the client failed to start listening for invitations.
	 * 
	 * @param conversationsClient The conversations client that failed to start listening for incoming invitations.
	 * @param exception Exception containing information that caused the failure.
	 */
	public void onFailedToStartListening(ConversationsClient conversationsClient, TwilioConversationsException exception);
		
	/**
	 * This method notifies the listener that the client has a pending invitation for a conversation.
	 *
	 * @param incomingInvite The invitation object.
	 */
	public void onIncomingInvite(ConversationsClient conversationsClient, IncomingInvite incomingInvite);

	/**
	 * This method notifies the listener that the incoming invite was cancelled.
	 *
	 * @param incomingInvite The invitation object.
	 */
	public void onIncomingInviteCancelled(ConversationsClient conversationsClient, IncomingInvite incomingInvite);

}
