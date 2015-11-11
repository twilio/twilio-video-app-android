package com.twilio.signal.impl.core;

import com.twilio.signal.CapturerException;


public interface CoreSession {
	
	public void start() throws CapturerException;
	
	public void stop();

}
