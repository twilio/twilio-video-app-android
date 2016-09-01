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

    private final Set<IceServer> iceServers;
    private final IceTransportPolicy iceTransportPolicy;

    private IceOptions(Builder builder) {
        this.iceServers = builder.iceServers;
        this.iceTransportPolicy = builder.iceTransportPolicy;
    }


    public Set<IceServer> getIceServers() {
        return iceServers;
    }

    public IceTransportPolicy getIceTransportPolicy() {
        return iceTransportPolicy;
    }

    long createNativeContext() {
        IceServer[] iceServersArray = new IceServer[0];
        if (iceServers != null && iceServers.size() > 0) {
            iceServersArray = iceServers.toArray(new IceServer[iceServers.size()]);
        }
        return nativeCreate(iceServersArray, iceTransportPolicy);
    }

    private native long nativeCreate(IceServer[] iceServers, IceTransportPolicy iceTransportPolicy);

    public static class Builder {
        private Set<IceServer> iceServers;
        private IceTransportPolicy iceTransportPolicy = IceTransportPolicy.ALL;

        public Builder() {}

        /**
         * Set of {@link IceServer} objects to be used during connection establishment.
         */
        public Builder iceServers(Set<IceServer> iceServers) {
            this.iceServers = iceServers;
            return this;
        }

        /**
         * The transport policy to use. Defaults to {@link IceTransportPolicy#ALL}
         */
        public Builder iceTransportPolicy(IceTransportPolicy iceTransportPolicy) {
            this.iceTransportPolicy = iceTransportPolicy;
            return this;
        }

        public IceOptions build() {
            return new IceOptions(this);
        }
    }
}
