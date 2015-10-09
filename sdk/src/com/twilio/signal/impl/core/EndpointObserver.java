package com.twilio.signal.impl.core;

interface EndpointObserver {

	void onRegistrationDidComplete(CoreError error);

	void onUnregistrationDidComplete(CoreError error);

	void onStateDidChange(EndpointState state);

	void onIncomingCallDidReceive(long nativeSession, String[] participants);

}
