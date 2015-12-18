package com.twilio.conversations.impl.core;

import com.twilio.conversations.impl.ConversationImpl;

public interface CoreEndpoint {
	
	public void accept(ConversationImpl conv);
	
	public void reject(ConversationImpl conv);
	
	public void ignore(ConversationImpl conv);

}
