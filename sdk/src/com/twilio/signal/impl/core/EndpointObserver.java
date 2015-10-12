package com.twilio.signal.impl.core;

public interface EndpointObserver {

	public void onRegistrationDidComplete(CoreError error);

	public void onUnregistrationDidComplete(CoreError error);

	public void onStateDidChange(EndpointState state);

	public void onIncomingCallDidReceive(long nativeSession, String[] participants);

}
