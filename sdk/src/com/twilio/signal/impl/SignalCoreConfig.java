package com.twilio.signal.impl;

import com.twilio.signal.Endpoint;

public class SignalCoreConfig {	
	
	public interface Callbacks
	{
		/*public void onRegistrationComplete(Endpoint endpoint);
		public void onUnRegistrationComplete(Endpoint endpoint);
		public void onIncomingCall(Endpoint endpoint); */
		
		public void onRegistrationComplete(Endpoint endpoint);
		public void onRegistrationComplete();
		public void onUnRegistrationComplete();
		public void onIncomingCall();
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
