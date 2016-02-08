package com.twilio.conversations.impl.core;

public interface CoreError {
	
	public int getCode();
    
	public String getDomain();
    
	public String getMessage();
}
