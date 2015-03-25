package com.twilio.signal.impl;

import java.util.Map;
import java.util.UUID;

import android.app.PendingIntent;

import com.twilio.signal.Capability;
import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;

public class EndpointImpl implements Endpoint{
	
	private final UUID uuid = UUID.randomUUID();


	public UUID getUuid() {
		return uuid;
	}


	public EndpointImpl(TwilioSignalImpl twilioSignalImpl,
			String inCapabilityToken, EndpointListener inListener) {
		// TODO Auto-generated constructor stub
	}


	@Override
	public Endpoint initWithToken(String token, EndpointListener listener) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Endpoint initWithToken(String token, Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void listen() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void unlisten() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void leaveConversaton(Conversation conversation) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setMuted(boolean muted) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setMuted(boolean muted, Conversation conversation) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isMuted() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean isMuted(Conversation conversation) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public Map<Capability, Object> getCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void updateCapabilityToken(String token) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setEndpointListener(EndpointListener listener) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setIncomingIntent(PendingIntent intent) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void createConversationWithRemoteEndpoint(String remoteEndpoint,
			Map<String, String> options, ConversationListener linstener) {
		// TODO Auto-generated method stub
		
	}

}
