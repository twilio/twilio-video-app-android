package com.twilio.signal.impl;

import com.twilio.signal.Endpoint;

public class CredentialInfo
{

	private String capabilityToken;
	private Endpoint endpoint;
	
	public CredentialInfo(String token,Endpoint endpoint )
	{
		this.capabilityToken = token;
		this.endpoint = endpoint;
	}
	
	
	public CredentialInfo(String token )
	{
		this.capabilityToken = token;
	
	}
	
	public String getCapabilityToken() {
		return capabilityToken;
	}
	
	public Endpoint getEndpoint() {
		return endpoint;
	}

}
