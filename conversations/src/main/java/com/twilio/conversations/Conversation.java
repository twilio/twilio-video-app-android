package com.twilio.conversations;

import java.util.Set;

/**
 * A Conversation represents communication between the client and one or more participants.
 *
 */
public interface Conversation {
    interface Listener {
        /**
         * This method notifies the listener when participant has connected to the conversation.
         *
         * @param conversation The conversation.
         * @param participant The participant.
         */
        void onParticipantConnected(Conversation conversation, Participant participant);

        /**
         * This method notifies the listener when a participant was unable to connect to the
         * conversation.
         *
         * @param conversation The conversation.
         * @param participant The participant.
         * @param exception Exception encountered in adding participant to conversation.
         *                  <p>The error codes returned correspond to the following scenarios:
         *                  <ol>
         *                      <li>{@link TwilioConversationsClient#CONVERSATION_REJECTED} returned when
         *                      participant rejects an invite.</li>
         *                      <li>{@link TwilioConversationsClient#CONVERSATION_IGNORED} returned when
         *                      participant ignores an invite</li>
         *                      <li>{@link TwilioConversationsClient#CONVERSATION_FAILED} returned when
         *                      participant rejects an invite to an existing conversation</li>
         *                  </ol>
         */
        void onFailedToConnectParticipant(Conversation conversation,
                                          Participant participant,
                                          TwilioConversationsException exception);

        /**
         * This method notifies the listener when a participant has disconnected from a conversation
         * by request or due to an error.
         *
         * @param conversation The conversation.
         * @param participant The participant.
         */
        void onParticipantDisconnected(Conversation conversation, Participant participant);

        /**
         * This method notifies the listener when the conversation has ended.
         *
         * @param conversation The conversation
         * @param exception Exception (if any) encountered when conversation ends.
         */
        void onConversationEnded(Conversation conversation, TwilioConversationsException exception);
    }

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
     * Gets the {@link Conversation.Listener} of this conversation
     *
     * @return listener of this conversation
     */
    Conversation.Listener getConversationListener();

    /**
     * Sets the {@link Conversation.Listener} of this conversation
     *
     * @param listener A listener of this conversation
     */
    void setConversationListener(Conversation.Listener listener);

    /**
     * Invites one or more participants to this conversation.
     *
     * <p>Results of this method will propagate up according to the following scenarios:
     * <ol>
     *     <li>{@link Conversation.Listener#onParticipantConnected(Conversation, Participant)} will
     *     be invoked if recipient accepts invite and is connected.</li>
     *     <li>{@link Conversation.Listener#onFailedToConnectParticipant(Conversation, Participant,
     *     TwilioConversationsException)} will be invoked with error code
     *     {@link TwilioConversationsClient#CONVERSATION_FAILED} if the recipient rejected the
     *     invite.</li>
     *     <li>{@link Conversation.Listener#onFailedToConnectParticipant(Conversation, Participant,
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
     *     <li>{@link Conversation.Listener#onFailedToConnectParticipant(Conversation, Participant,
     *     TwilioConversationsException)} will be invoked with error code
     *     {@link TwilioConversationsClient#CONVERSATION_TERMINATED} for each participant of the
     *     {@link Conversation}</li>
     *     <li>{@link Conversation.Listener#onConversationEnded(Conversation,
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
