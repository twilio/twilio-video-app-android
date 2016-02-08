package com.twilio.conversations.impl.core;

import com.twilio.conversations.impl.ConversationImpl;

public interface CoreEndpoint {

	public void accept(ConversationImpl conversationImpl);

	public void reject(ConversationImpl conversationImpl);

	public void ignore(ConversationImpl conversationImpl);

}
