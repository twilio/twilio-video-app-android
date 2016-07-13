package com.twilio.conversations;

/**
 * Specifies status of {@link OutgoingInvite} or {@link IncomingInvite}
 */
public enum InviteStatus {
    PENDING,
    ACCEPTING,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    FAILED
}
