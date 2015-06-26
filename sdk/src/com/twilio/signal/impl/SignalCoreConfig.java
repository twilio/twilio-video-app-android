package com.twilio.signal.impl;


public class SignalCoreConfig {	
	
	public interface Callbacks
	{
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
