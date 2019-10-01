package com.twilio.video;

/**
 * Twilio's Video SDKs, where possible, attempt to calculate a singular Network Quality Level
 * describing the quality of a Participant's connection to a Room. This value ranges from unknown to
 * 5, with unknown representing that the Network Quality Level can not be determined, 0 representing
 * a failed network connection, 1 representing a poor network connection, and 5 representing an
 * excellent network connection. The SDK calculates this metric only when connected to Group Rooms.
 * In case of a connection to Peer-to-Peer Room the value is expected to be unknown at all times.
 *
 * <p>Note that the Network Quality Level is not an absolute metric but a score relative to the
 * demand being placed on the network. For example, the NQ score might be a 5 while on a good
 * network and publishing only an AudioTrack. Later, if a HD VideoTrack is added, the score might
 * come down to 2. This also means that when the network is not being used at all (i.e. the Client
 * is neither publishing nor subscribing to any tracks) the Network Quality Level will always be 5
 * given that any network will be capable of complying with a zero communications demand.
 */
public enum NetworkQualityLevel {

    /**
     * The Network Quality Level cannot be determined or the Network Quality API has not been
     * enabled.
     */
    NETWORK_QUALITY_LEVEL_UNKNOWN,
    /** The network connection has failed */
    NETWORK_QUALITY_LEVEL_ZERO,
    /** The Network Quality is Very Bad. */
    NETWORK_QUALITY_LEVEL_ONE,
    /** The Network Quality is Bad. */
    NETWORK_QUALITY_LEVEL_TWO,
    /** The Network Quality is Good. */
    NETWORK_QUALITY_LEVEL_THREE,
    /** The Network Quality is Very Good. */
    NETWORK_QUALITY_LEVEL_FOUR,
    /** The Network Quality is Excellent. */
    NETWORK_QUALITY_LEVEL_FIVE;
}
