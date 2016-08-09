package com.twilio.video;

/**
 * IceTransportPolicy specifies which ICE transports to allow.
 */
public enum IceTransportPolicy {
    // Only TURN relay transports will be used.
    ICE_TRANSPORT_POLICY_RELAY,

    // All transports will be used.
    ICE_TRANSPORT_POLICY_ALL
}
