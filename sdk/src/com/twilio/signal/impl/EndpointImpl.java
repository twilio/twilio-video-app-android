package com.twilio.signal.impl;

import java.util.Map;
import java.util.UUID;

import android.app.PendingIntent;
import android.content.Context;

import com.twilio.signal.Capability;
import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationListener;
import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;

public class EndpointImpl implements Endpoint, SignalCoreConfig.Callbacks{
	
	private final UUID uuid = UUID.randomUUID();
	private SignalCore sigalCore;
	private Context context;
	private EndpointListener listener;
	private String userName;
	
	private native Endpoint createEndpoint();


	public UUID getUuid() {
		return uuid;
	}


	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}


	public EndpointImpl(TwilioSignalImpl twilioSignalImpl,
			String inCapabilityToken, EndpointListener inListener) {
		this.context = twilioSignalImpl.getContext();
		this.listener = inListener;
		this.sigalCore = SignalCore.getInstance();
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
		SignalCore.getInstance().register();
	}


	@Override
	public void unlisten() {
		SignalCore.getInstance().unregister(this);	
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
	public Conversation createConversation(String remoteEndpoint,
			Map<String, String> options, ConversationListener linstener) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void onRegistrationComplete() {
		if(this.listener != null) {
		    this.listener.onEndpointStartListeningForInvites(this);
		}
	}

	
	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	@Override
	public void onUnRegistrationComplete() {
		if(this.listener != null) {
		    this.listener.onEndpointStopListeningForInvites(this);
		}
	}
}
