package com.twilio.video;

import java.util.Set;

/**
 *  IceOptions specifies custom media connectivity configurations.
 *
 *  Media connections are established using the ICE (Interactive Connectivity Establishment)
 *  protocol. These options allow you to customize how data flows to and from participants, and
 *  which protocols to use. You may also provide your own ICE servers, overriding the defaults.
 *  https://www.twilio.com/stun-turn
 */
public class IceOptions {
    /**
     * Set of {@link IceServer} objects to be used during connection establishment.
     */
    public final Set<IceServer> iceServers;

    /**
     * The transport policy to use. Defaults to {@link IceTransportPolicy#ALL}
     */
    public final IceTransportPolicy iceTransportPolicy;


    public IceOptions(IceTransportPolicy iceTransportPolicy, Set<IceServer> iceServers) {
        this.iceServers = iceServers;
        this.iceTransportPolicy = iceTransportPolicy;
    }

    public IceOptions(IceTransportPolicy iceTransportPolicy) {
        this(iceTransportPolicy, null);
    }

    public IceOptions() {
        this(IceTransportPolicy.ALL, null);
    }
}
