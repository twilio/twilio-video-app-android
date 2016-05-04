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
     * <p>Results of this method will propagate up according to the following scenarios:
     * <ol>
     *     <li>{@link ConversationListener#onParticipantConnected(Conversation, Participant)} will
     *     be invoked if recipient accepts invite and is connected.</li>
     *     <li>{@link ConversationListener#onFailedToConnectParticipant(Conversation, Participant,
     *     TwilioConversationsException)} will be invoked with error code
     *     {@link TwilioConversationsClient#CONVERSATION_FAILED} if the recipient rejected the
     *     invite.</li>
     *     <li>{@link ConversationListener#onFailedToConnectParticipant(Conversation, Participant,
     *     TwilioConversationsException)} will be invoked with error code
     *     {@link TwilioConversationsClient#CONVERSATION_IGNORED} if the recipient ignored the
     *     invite.</li>
     * </ol>
     *
     * @param participantIdentities A set of strings representing the identities of these
     *                              participants.
     */
    void invite(Set<String> participantIdentities) throws IllegalArgumentException;

    /**
     * Disconnects from this conversation.
     *
     * <p>Results of this method will propagate up in the following order:
     * <ol>
     *     <li>{@link ConversationListener#onFailedToConnectParticipant(Conversation, Participant,
     *     TwilioConversationsException)} will be invoked with error code
     *     {@link TwilioConversationsClient#CONVERSATION_TERMINATED} for each participant of the
     *     {@link Conversation}</li>
     *     <li>{@link ConversationListener#onConversationEnded(Conversation,
     *     TwilioConversationsException)} will be invoked upon the completion of this process.</li>
     * </ol>
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
     * @param listener Listens to media tracks stats from this conversation
     */
    void setStatsListener(StatsListener listener);
}
