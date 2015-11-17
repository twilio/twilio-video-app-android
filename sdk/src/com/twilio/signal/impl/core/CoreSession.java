package com.twilio.signal.impl.core;



public interface CoreSession {
	
	public void start();
	
	public void stop();
	
	public boolean enableVideo(boolean enabled, boolean paused);

}
