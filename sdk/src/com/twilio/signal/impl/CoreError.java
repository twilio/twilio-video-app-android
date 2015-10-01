package com.twilio.signal.impl;

public interface CoreError {
	
	public int getCode();
    
	public String getDomain();
    
	public String getMessage();
}
