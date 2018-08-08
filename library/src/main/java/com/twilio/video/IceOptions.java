/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import java.util.Set;

/**
 * IceOptions specifies custom media connectivity configurations.
 *
 * <p>Media connections are established using the ICE (Interactive Connectivity Establishment)
 * protocol. These options allow you to customize how data flows to and from participants, and which
 * protocols to use. You may also provide your own ICE servers, overriding the defaults.
 * https://www.twilio.com/stun-turn.
 */
public class IceOptions {
    private final Set<IceServer> iceServers;
    private final IceTransportPolicy iceTransportPolicy;
    private final boolean abortOnIceServersTimeout;
    private final long iceServersTimeout;

    private IceOptions(Builder builder) {
        this.iceServers = builder.iceServers;
        this.iceTransportPolicy = builder.iceTransportPolicy;
        this.abortOnIceServersTimeout = builder.abortOnIceServersTimeout;
        this.iceServersTimeout = builder.iceServersTimeout;
    }

    public Set<IceServer> getIceServers() {
        return iceServers;
    }

    public IceTransportPolicy getIceTransportPolicy() {
        return iceTransportPolicy;
    }

    public boolean getAbortOnIceServersTimeout() {
        return abortOnIceServersTimeout;
    }

    IceServer[] getIceServersArray() {
        IceServer[] iceServersArray = new IceServer[0];
        if (iceServers != null && iceServers.size() > 0) {
            iceServersArray = iceServers.toArray(new IceServer[iceServers.size()]);
        }
        return iceServersArray;
    }

    public long getIceServersTimeout() {
        return iceServersTimeout;
    }

    public static class Builder {
        private Set<IceServer> iceServers;
        private IceTransportPolicy iceTransportPolicy = IceTransportPolicy.ALL;
        private boolean abortOnIceServersTimeout = false;
        private long iceServersTimeout = 3000;

        public Builder() {}

        /** Set of {@link IceServer} objects to be used during connection establishment. */
        public Builder iceServers(Set<IceServer> iceServers) {
            this.iceServers = iceServers;
            return this;
        }

        /** The transport policy to use. Defaults to {@link IceTransportPolicy#ALL}. */
        public Builder iceTransportPolicy(IceTransportPolicy iceTransportPolicy) {
            this.iceTransportPolicy = iceTransportPolicy;
            return this;
        }

        /**
         * If fetching ICE servers times out (due to a restrictive network or a slow network proxy),
         * then, by default, the Video SDK will fallback to using hard-coded STUN servers and
         * continue connecting to the Room. Setting this property to true will instead abort with
         * error 53500, "Unable to acquire configuration".
         */
        public Builder abortOnIceServersTimeout(boolean abortOnIceServersTimeout) {
            this.abortOnIceServersTimeout = abortOnIceServersTimeout;
            return this;
        }

        /**
         * Time in milliseconds to attempt retrieving ICE servers before timing out. The default is
         * 3000 milliseconds.
         */
        public Builder iceServersTimeout(long iceServersTimeout) {
            this.iceServersTimeout = iceServersTimeout;
            return this;
        }

        public IceOptions build() {
            return new IceOptions(this);
        }
    }
}
