/*
 * Copyright (C) 2018 Twilio, Inc.
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
import android.support.annotation.Nullable;

/**
 * Statistics of ICE candidate as defined in <a
 * href="https://www.w3.org/TR/webrtc-stats/#icecandidate-dict*">Identifiers for WebRTC's Statistics
 * API</a>
 */
public class IceCandidateStats {
    /** Unique identifier of the underlying candidate. */
    @NonNull public final String transportId;

    /** True indicates remote candidate and false indicates local candidate. */
    public final boolean isRemote;

    /** IP address of the candidate. */
    @NonNull public final String ip;

    /** Port number of the candidate. */
    public final int port;

    /** Transport of the candidate, valid values are udp or tcp. */
    @NonNull public final String protocol;

    /**
     * Candidate type. It can be host (host candidate), srflx (server reflexive candidate), prflx
     * (peer reflexive candidate) and relay (relay candidate).
     */
    @NonNull public final String candidateType;

    /** Priority as defined in <a href="https://tools.ietf.org/html/rfc5245">RFC 5245</a>. */
    public final int priority;

    /** The URL of the TURN or STUN server. */
    @Nullable public final String url;

    /** The candidate is no longer active. */
    public final boolean deleted;

    public IceCandidateStats(
            @NonNull final String transportId,
            final boolean isRemote,
            @NonNull final String ip,
            final int port,
            @NonNull final String protocol,
            @NonNull final String candidateType,
            final int priority,
            @Nullable final String url,
            final boolean deleted) {
        this.transportId = transportId;
        this.isRemote = isRemote;
        this.ip = ip;
        this.port = port;
        this.protocol = protocol;
        this.candidateType = candidateType;
        this.priority = priority;
        this.url = url;
        this.deleted = deleted;
    }
}
