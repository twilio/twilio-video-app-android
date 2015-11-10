package com.twilio.signal.impl.core;

import com.twilio.signal.CameraException;


public interface CoreSession {
	
	public void start() throws CameraException;
	
	public void stop();

}
