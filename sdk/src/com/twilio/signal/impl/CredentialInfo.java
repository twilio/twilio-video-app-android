package com.twilio.signal.impl;

import com.twilio.signal.Endpoint;

public class CredentialInfo
{

	private String capabilityToken;
	private String stunURL;
	private String turnURL;
	private String userName;
	private String password;
	private Endpoint endpoint;
	
	public CredentialInfo(String token, String sURL, String tURL, String userName, String passWord,Endpoint endpoint )
	{
		this.capabilityToken = token;
		this.stunURL = sURL;
		this.turnURL = tURL;
		this.userName = userName;
		this.password = passWord;
		this.endpoint = endpoint;
	}
	
	
	public CredentialInfo(String token, String sURL, String tURL, String userName, String passWord,SignalCore signalCore )
	{
		this.capabilityToken = token;
		this.stunURL = sURL;
		this.turnURL = tURL;
		this.userName = userName;
		this.password = passWord;
	}
	
	public String getCapabilityToken() {
		return capabilityToken;
	}

	public String getStunURL() {
		return stunURL;
	}

	public String getTurnUrl() {
		return turnURL;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}
	
	public Endpoint getEndpoint() {
		return endpoint;
	}

}
