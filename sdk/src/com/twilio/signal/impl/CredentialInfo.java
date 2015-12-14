package com.twilio.signal.impl;

import com.twilio.signal.ConversationsClient;

public class CredentialInfo
{

	private String capabilityToken;
	private ConversationsClient conversationsClient;
	
	public CredentialInfo(String token,ConversationsClient conversationsClient)
	{
		this.capabilityToken = token;
		this.conversationsClient = conversationsClient;
	}
	
	
	public CredentialInfo(String token )
	{
		this.capabilityToken = token;
	
	}
	
	public String getCapabilityToken() {
		return capabilityToken;
	}
	
	public ConversationsClient getConversationsClient() {
		return conversationsClient;
	}

}
