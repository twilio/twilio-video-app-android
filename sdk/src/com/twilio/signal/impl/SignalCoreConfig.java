package com.twilio.signal.impl;

import com.twilio.signal.Endpoint;

public class SignalCoreConfig {	
	
	public interface Callbacks
	{
		/*public void onRegistrationComplete(Endpoint endpoint);
		public void onUnRegistrationComplete(Endpoint endpoint);
		public void onIncomingCall(Endpoint endpoint); */
		
		public void onRegistrationComplete(EndpointImpl endpoint);
		public void onUnRegistrationComplete(EndpointImpl endpoint);
		public void onIncomingCall(EndpointImpl endpoint);
	}
	
	private Callbacks callbacks;	
	

	public SignalCoreConfig(Callbacks callbacks) 
	{
		this.callbacks = callbacks;
	}

	public Callbacks getCallbacks() {
		return callbacks;
	}
}
