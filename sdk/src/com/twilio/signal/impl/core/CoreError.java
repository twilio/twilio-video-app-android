package com.twilio.signal.impl.core;

public interface CoreError {
	
	public int getCode();
    
	public String getDomain();
    
	public String getMessage();
}
