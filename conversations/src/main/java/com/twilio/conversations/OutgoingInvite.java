package com.twilio.conversations;

import java.util.Set;

/**
 * An OutgoingInvite represents an invitation to a list of {@link Participant} to start a conversation.
 *
 */
public interface OutgoingInvite {
    /**
     * Cancels this invitation
     *
     */
    void cancel();

    /**
     * Returns the identities of the participants invited to the conversation
     *
     * @return list of participant identities invited to conversation
     */
    Set<String> getInvitedParticipants();

    /**
     * The status of this invitation
     *
     * @return invite status
     */
    InviteStatus getStatus();
}
