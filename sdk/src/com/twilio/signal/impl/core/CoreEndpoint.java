package com.twilio.signal.impl.core;

import com.twilio.signal.impl.ConversationImpl;

public interface CoreEndpoint {
	
	public void accept(ConversationImpl conv);
	
	public void reject(ConversationImpl conv);
	
	public void ignore(ConversationImpl conv);

}
