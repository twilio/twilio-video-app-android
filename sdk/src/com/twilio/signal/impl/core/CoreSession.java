package com.twilio.signal.impl.core;

import java.util.Set;



public interface CoreSession {
	
	public void start(CoreSessionMediaConstraints mediaConstraints);
	
	public void stop();
	
	public boolean enableVideo(boolean enabled, boolean paused);
	
	public void inviteParticipants(Set<String> participants);
	
	public boolean enableAudio(boolean enabled, boolean muted);

}
