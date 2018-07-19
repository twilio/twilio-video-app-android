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

import android.support.annotation.NonNull;
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

    private IceOptions(Builder builder) {
        this.iceServers = builder.iceServers;
        this.iceTransportPolicy = builder.iceTransportPolicy;
    }

    @NonNull
    public Set<IceServer> getIceServers() {
        return iceServers;
    }

    @NonNull
    public IceTransportPolicy getIceTransportPolicy() {
        return iceTransportPolicy;
    }

    IceServer[] getIceServersArray() {
        IceServer[] iceServersArray = new IceServer[0];
        if (iceServers != null && iceServers.size() > 0) {
            iceServersArray = iceServers.toArray(new IceServer[iceServers.size()]);
        }
        return iceServersArray;
    }

    public static class Builder {
        private Set<IceServer> iceServers;
        private IceTransportPolicy iceTransportPolicy = IceTransportPolicy.ALL;

        public Builder() {}

        /** Set of {@link IceServer} objects to be used during connection establishment. */
        @NonNull
        public Builder iceServers(@NonNull Set<IceServer> iceServers) {
            this.iceServers = iceServers;
            return this;
        }

        /** The transport policy to use. Defaults to {@link IceTransportPolicy#ALL}. */
        @NonNull
        public Builder iceTransportPolicy(@NonNull IceTransportPolicy iceTransportPolicy) {
            this.iceTransportPolicy = iceTransportPolicy;
            return this;
        }

        @NonNull
        public IceOptions build() {
            return new IceOptions(this);
        }
    }
}
