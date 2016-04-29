package com.twilio.conversations;

import java.util.Set;

/**
 * ConversationsClient allows user to create or participate in conversations.
 *
 * @see ConversationsClientListener
 */
public interface ConversationsClient {
    /**
     * Set a new {@link ConversationsClientListener} object to respond to client events.
     *
     * @param listener A listener for client events.
     */
    void setConversationsClientListener(ConversationsClientListener listener);

    /**
     * Get identity of this conversations client on the network.
     *
     * @return identity of this conversations client
     */
    String getIdentity();

    /**
     * Reflects current listening state of the conversations client.
     *
     * @return <code>true</code> if conversations client is listening, </code>false</code>
     * otherwise.
     */
    boolean isListening();

    /**
     * Starts listening for incoming invites and allows outgoing invites to be sent.
     *
     * <p>The result of this method will propagate via the {@link ConversationsClientListener}:</p>
     * <ol>
     *     <li>{@link ConversationsClientListener#onStartListeningForInvites(ConversationsClient)}
     *     will be invoked if the client is listening for invites</li>
     *     <li>{@link ConversationsClientListener#onFailedToStartListening(ConversationsClient,
     *     TwilioConversationsException)} (ConversationsClient)} will be invoked if an issue
     *     occurred while attempting to listen</li>
     * </ol>
     */
    void listen();

    /**
     * Stops listening for incoming conversations.
     *
     * <p>{@link ConversationsClientListener#onStopListeningForInvites(ConversationsClient)}
     * will be invoked upon the completion of this process</p>
     */
    void unlisten();

    /**
     * Sends an invitation to start a conversation with the following participants and local media
     * configuration. The {@link ConversationsClient} must be listening before sending an
     * outgoing invite.
     *
     * <p>The result of this method will propagate via the {@link ConversationCallback} provided
     * according to the following scenarios:</p>
     *
     * <ol>
     *     <li>{@link ConversationCallback#onConversation(Conversation,
     *     TwilioConversationsException)} will be invoked with a <code>null</code> value of
     *     {@link TwilioConversationsException} if the invite was accepted</li>
     *     <li>{@link ConversationCallback#onConversation(Conversation,
     *     TwilioConversationsException)} will be invoked with one of the following error codes for
     *     the returned {@link TwilioConversationsException}</li>
     *     <ul>
     *         <li>{@link TwilioConversations#CONVERSATION_REJECTED}</li>
     *         <li>{@link TwilioConversations#CONVERSATION_IGNORED}</li>
     *     </ul>
     * </ol>
     *
     * @param participants Set of participant names as Strings
     * @param localMedia Local Media you would like to use when setting up the new conversation
     * @param conversationCallback The callback that will provide the conversation once it's been
     *                             created
     */
    OutgoingInvite sendConversationInvite(Set<String> participants,
                                          LocalMedia localMedia,
                                          ConversationCallback conversationCallback);

    /**
     * Sends an invitation to start a conversation with the following participants and local media
     * configuration. The {@link ConversationsClient} must be listening before sending an
     * outgoing invite.
     *
     * <p>The result of this method will propagate via the {@link ConversationCallback} provided
     * according to the following scenarios:</p>
     *
     * <ol>
     *     <li>{@link ConversationCallback#onConversation(Conversation,
     *     TwilioConversationsException)} will be invoked with a <code>null</code> value of
     *     {@link TwilioConversationsException} if the invite was accepted</li>
     *     <li>{@link ConversationCallback#onConversation(Conversation,
     *     TwilioConversationsException)} will be invoked with one of the following error codes for
     *     the returned {@link TwilioConversationsException}</li>
     *     <ul>
     *         <li>{@link TwilioConversations#CONVERSATION_REJECTED}</li>
     *         <li>{@link TwilioConversations#CONVERSATION_IGNORED}</li>
     *     </ul>
     * </ol>
     *
     * @param participants Set of participant names as Strings
     * @param localMedia Local Media you would like to use when setting up the new conversation
     * @param iceOptions Custom ICE (Interactive Connectivity Establishment) protocol options
     * @param conversationCallback The callback that will provide the conversation once it's been
     *                             created
     */
    OutgoingInvite sendConversationInvite(Set<String> participants,
                                          LocalMedia localMedia,
                                          IceOptions iceOptions,
                                          ConversationCallback conversationCallback);

    /**
     * Sets the audio output speaker for the device.
     *
     * Bluetooth headset is not supported.
     *
     * To use volume up/down keys call
     * 'setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);' in your Android Activity.
     *
     * @param audioOutput that should be used by the system
     */
    void setAudioOutput(AudioOutput audioOutput);

    /**
     * Audio output speaker for the current client device
     *
     * @return audio output speaker
     */
    AudioOutput getAudioOutput();
}
