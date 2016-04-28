package com.twilio.conversations;

import java.util.Set;

/**
 * An IncomingInvite represents an invitation from a client to a Conversation An IncomingInvite
 * represents an invitation from a client to a {@link Conversation}
 */
public interface IncomingInvite {
    /**
     * Accepts this invitation. {@link ConversationCallback#onConversation(Conversation,
     * TwilioConversationsException)} will be invoked when the {@link Conversation} is available.
     */
    void accept(LocalMedia localMedia, ConversationCallback conversationCallback);

    /**
     * Accepts this invitation with custom ICE options.
     * {@link ConversationCallback#onConversation(Conversation, TwilioConversationsException)}
     * will be invoked when the {@link Conversation} is available.
     */
    void accept(LocalMedia localMedia,
                IceOptions iceOptions,
                ConversationCallback conversationCallback);

    /**
     * Rejects this invitation
     */
    void reject();

    /**
     * Returns the SID of the conversation you are invited to
     *
     * @return conversation SID
     */
    String getConversationSid();

    /**
     * Returns the identity of the inviter
     *
     * @return inviter identity
     */
    String getInviter();

    /**
     * Returns the list of participants already in the conversation
     *
     * @return list of participants
     */
    Set<String> getParticipants();

    /**
     * Gets the status of this invite
     *
     * @return invite status
     */
    InviteStatus getInviteStatus();
}
