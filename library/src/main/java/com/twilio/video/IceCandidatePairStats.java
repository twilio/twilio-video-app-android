package com.twilio.video;

import android.support.annotation.NonNull;

/**
 * Statistics of ICE candidate pair as defined in <a
 * href="https://www.w3.org/TR/webrtc-stats/#candidatepair-dict*">Identifiers for WebRTC's
 * Statistics API</a>.
 */
public class IceCandidatePairStats {
    /** Unique identifier of the underlying candidate pair. */
    @NonNull public final String transportId;

    /** Unique identifier of the underlying local candidate associated with this candidate pair. */
    @NonNull public final String localCandidateId;

    /** Unique identifier of the underlying remote candidate associated with this candidate pair. */
    @NonNull public final String remoteCandidateId;

    /** State of this candidate pair, see {@link IceCandidatePairState}. */
    public final IceCandidatePairState state;

    /** IP address of local candidate. */
    @NonNull public final String localCandidateIp;

    /** IP address of remote candidate. */
    @NonNull public final String remoteCandidateIp;

    /** Candidate priorities as per <a href="https://tools.ietf.org/html/rfc5245">RFC 5245</a>. */
    public final long priority;

    /**
     * Whether the nominated flag was updated as per <a
     * href="https://tools.ietf.org/html/rfc5245">RFC 5245</a>.
     */
    public final boolean nominated;

    /** Has gotten ACK to an ICE request. */
    public final boolean writeable;

    /** Has gotten a valid incoming ICE request. */
    public final boolean readable;

    /** Total number of payload bytes sent on this candidate pair (excluding headers or padding). */
    public final long bytesSent;

    /**
     * Total number of payload bytes received on this candidate pair (excluding headers or padding).
     */
    public final long bytesReceived;

    /** Sum of all round trip time measurements in seconds since the beginning of the session. */
    public final double totalRoundTripTime;

    /** Latest round trip time measured in seconds. */
    public final double currentRoundTripTime;

    /**
     * Available bitrate for all the outgoing RTP streams using this candidate pair measured in bits
     * per second (excluding IP TCP/UDP headers).
     */
    public final double availableOutgoingBitrate;

    /**
     * Available bitrate for all the incoming RTP streams using this candidate pair measured in bits
     * per second (excluding IP TCP/UDP headers).
     */
    public final double availableIncomingBitrate;

    /** Total number of connectivity check requests received. */
    public final long requestsReceived;

    /** Total number of connectivity check requests sent. */
    public final long requestsSent;

    /** Total number of connectivity check responses received. */
    public final long responsesReceived;

    /** Total number of connectivity check responses sent. */
    public final long retransmissionsReceived;

    /** Total number of connectivity check retransmissions received. */
    public final long retransmissionsSent;

    /** Total number of connectivity check retransmissions sent. */
    public final long consentRequestsReceived;

    /** Total number of consent requests received. */
    public final long consentRequestsSent;

    /** Total number of consent responses received. */
    public final long consentResponsesReceived;

    /** Total number of consent responses sent. */
    public final long consentResponsesSent;

    /** Identify whether the candidate pair is active. */
    public final boolean activeCandidatePair;

    /** Relay protocol. */
    public final String relayProtocol;

    IceCandidatePairStats(
            @NonNull final String transportId,
            @NonNull final String localCandidateId,
            @NonNull final String remoteCandidateId,
            final IceCandidatePairState state,
            @NonNull final String localCandidateIp,
            @NonNull final String remoteCandidateIp,
            final long priority,
            final boolean nominated,
            final boolean writeable,
            final boolean readable,
            final long bytesSent,
            final long bytesReceived,
            final double totalRoundTripTime,
            final double currentRoundTripTime,
            final double availableOutgoingBitrate,
            final double availableIncomingBitrate,
            final long requestsReceived,
            final long requestsSent,
            final long responsesReceived,
            final long retransmissionsReceived,
            final long retransmissionsSent,
            final long consentRequestsReceived,
            final long consentRequestsSent,
            final long consentResponsesReceived,
            final long consentResponsesSent,
            final boolean activeCandidatePair,
            final String relayProtocol) {
        this.transportId = transportId;
        this.localCandidateId = localCandidateId;
        this.remoteCandidateId = remoteCandidateId;
        this.state = state;
        this.localCandidateIp = localCandidateIp;
        this.remoteCandidateIp = remoteCandidateIp;
        this.priority = priority;
        this.nominated = nominated;
        this.writeable = writeable;
        this.readable = readable;
        this.bytesSent = bytesSent;
        this.bytesReceived = bytesReceived;
        this.totalRoundTripTime = totalRoundTripTime;
        this.currentRoundTripTime = currentRoundTripTime;
        this.availableOutgoingBitrate = availableOutgoingBitrate;
        this.availableIncomingBitrate = availableIncomingBitrate;
        this.requestsReceived = requestsReceived;
        this.requestsSent = requestsSent;
        this.responsesReceived = responsesReceived;
        this.retransmissionsReceived = retransmissionsReceived;
        this.retransmissionsSent = retransmissionsSent;
        this.consentRequestsReceived = consentRequestsReceived;
        this.consentRequestsSent = consentRequestsSent;
        this.consentResponsesReceived = consentResponsesReceived;
        this.consentResponsesSent = consentResponsesSent;
        this.activeCandidatePair = activeCandidatePair;
        this.relayProtocol = relayProtocol;
    }
}
