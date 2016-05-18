package com.twilio.conversations;

public interface EndpointObserver {
    void onRegistrationDidComplete(CoreError error);

    void onUnregistrationDidComplete(CoreError error);

    void onStateDidChange(EndpointState state);

    void onIncomingCallDidReceive(long nativeSession, String[] participants);
}
