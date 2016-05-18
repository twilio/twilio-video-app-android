package com.twilio.conversations;

public enum EndpointState {
    INITIALIZED,
    REGISTERING,
    REGISTERED,
    REGISTRATION_FAILED,
    UNREGISTERING,
    UNREGISTERED,
    UNREGISTRATION_FAILED,
    RECONNECTING
}
