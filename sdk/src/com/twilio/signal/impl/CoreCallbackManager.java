package com.twilio.signal.impl;


public class CoreCallbackManager implements SignalCoreConfig.Callbacks {
	
	private static CoreCallbackManager instance;

	public static CoreCallbackManager getInstance() {
		if (instance == null) {
			synchronized (CoreCallbackManager.class) {
				if (instance == null) {
					instance = new CoreCallbackManager();
				}
			}
		}
		return instance;
	}

	@Override
	public void onUnRegistrationComplete(EndpointImpl endpoint) {
		if(endpoint != null) {
			//endpoint.onUnRegistration();
		}
		
	}

	@Override
	public void onIncomingCall(EndpointImpl endpoint) {
		if(endpoint != null) {
			endpoint.onIncomingInvite();
		}
		
	}

	@Override
	public void onRegistrationComplete(EndpointImpl endpoint) {
		if(endpoint != null) {
		//	endpoint.onRegistration();
		}
	}


}
