package com.twilio.conversations;

import java.util.Set;

/**
 * A Conversation represents communication between the client and one or more participants.
 *
 */
public interface Conversation {
    /**
     * Returns the list of participants in this conversation.
     *
     * @return participants list of {@link Participant} in this conversation.
     */
    Set<Participant> getParticipants();

    /**
     * Returns the {@link LocalMedia} for this conversation
     *
     * @return local media
     */
    LocalMedia getLocalMedia();

    /**
     * Gets the {@link ConversationListener} of this conversation
     *
     * @return listener of this conversation
     */
    ConversationListener getConversationListener();

    /**
     * Sets the {@link ConversationListener} of this conversation
     *
     * @param listener A listener of this conversation
     */
    void setConversationListener(ConversationListener listener);

    /**
     * Invites one or more participants to this conversation.
     *
     * <p>Results of this call will propagate up according to the following scenarios:
     * <ol>
     * <li>{@link ConversationListener#onParticipantConnected(Conversation, Participant)} will be
     * invoked if recipient accepts invite and is connected.
     * <li>{@link ConversationListener#onFailedToConnectParticipant(Conversation, Participant,
     * TwilioConversationsException)} will be invoked with error code
     * {@link TwilioConversations#CONVERSATION_FAILED} if the recipient rejected the invite.
     * <li>{@link ConversationListener#onFailedToConnectParticipant(Conversation, Participant,
     * TwilioConversationsException)} will be invoked with error code
     * {@link TwilioConversations#CONVERSATION_IGNORED} if the recipient ignored the invite.
     * </ol>
     *
     * @param participantIdentities A set of strings representing the identities of these
     *                              participants.
     */
    void invite(Set<String> participantIdentities) throws IllegalArgumentException;

    /**
     * Disconnects from this conversation.
     * {@link ConversationListener#onConversationEnded(Conversation, TwilioConversationsException)}
     * will be invoked upon the completion of this process.
     */
    void disconnect();

    /**
     * Gets the conversation SID
     *
     * @return conversation SID
     */
    String getSid();

    /**
     * Gets the {@link StatsListener} of this conversation
     *
     * @return listener of this conversation media tracks stats
     */
    StatsListener getStatsListener();

    /**
     * Sets the {@link StatsListener} of this conversation
     *
     * @param listener A listener of this conversation media tracks stats
     */
    void setStatsListener(StatsListener listener);
}
