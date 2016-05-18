package com.twilio.conversations.core;

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
