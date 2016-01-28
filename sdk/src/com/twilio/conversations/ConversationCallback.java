package com.twilio.conversations;

/**
 * This callback returns information about the conversation initiated by an {@link IncomingInvite} or {@link OutgoingInvite}
 *
 */
public interface ConversationCallback {

	/**
	 *  Called when a conversation is successfully started.
	 *
	 *  @param conversation The conversation that was created.
	 *  @param exception	An error describing why the conversation was not created.
	 */
	void onConversation(Conversation conversation, TwilioConversationsException exception);

}
