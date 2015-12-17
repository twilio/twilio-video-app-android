package com.twilio.signal;

public interface ConversationCallback {

	/**
	 *  A callback which is fired when conversation creation completes.
	 *
	 *  @param conversation The conversation that was created. Populated on success.
	 *  @param exception	An error describing why the conversation was not created. Populated on failure.
	 */
	void onConversation(Conversation conversation, ConversationException exception);

}
