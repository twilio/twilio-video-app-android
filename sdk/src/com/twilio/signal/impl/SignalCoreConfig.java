package com.twilio.signal.impl;

public class SignalCoreConfig {	
	
	public interface Callbacks
	{
		public void onRegistrationComplete();
		public void onUnRegistrationComplete();
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
