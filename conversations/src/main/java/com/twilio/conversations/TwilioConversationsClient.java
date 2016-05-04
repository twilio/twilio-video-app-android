package com.twilio.conversations;

import java.util.Set;

/**
 * TwilioConversationsClient allows user to create or participate in conversations.
 *
 * @see ConversationsClientListener
 */
public interface TwilioConversationsClient {
    /**
     *  Authenticating your Client failed due to invalid auth credentials.
     */
    int INVALID_AUTH_DATA = 100;
    /**
     *  The SIP account was invalid.
     */
    int INVALID_SIP_ACCOUNT = 102;
    /**
     *  There was an error during Client registration.
     */
    int CLIENT_REGISTATION_ERROR= 103;
    /**
     *  The Conversation was invalid.
     */
    int INVALID_CONVERSATION = 105;
    /**
     *  The Client you invited was not available.
     */
    int CONVERSATION_PARTICIPANT_NOT_AVAILABLE = 106;
    /**
     *  The Client rejected your invitation.
     */
    int CONVERSATION_REJECTED = 107;
    /**
     *  The Client was busy; and could not handle your invitation.
     */
    int CONVERSATION_IGNORED = 108;
    /**
     *  The Conversation failed to start.
     */
    int CONVERSATION_FAILED = 109;
    /**
     *  The Conversation was terminated due to an unforeseen error.
     */
    int CONVERSATION_TERMINATED = 110;
    /**
     *  Establishing a media connection with the remote peer failed.
     */
    int PEER_CONNECTION_FAILED = 111;
    /**
     *  The remote client address was invalid.
     */
    int INVALID_PARTICIPANT_ADDRESSES = 112;
    /**
     *  The client disconnected unexpectedly.
     */
    int CLIENT_DISCONNECTED = 200;
    /**
     *  Too many active Conversations.
     */
    int TOO_MANY_ACTIVE_CONVERSATIONS = 201;
    /**
     *  A track was created with constraints that could not be satisfied.
     */
    int TRACK_CREATION_FAILED = 207;
    /**
     *  Too many tracks were added to the local media.
     *  @note: The current maximum is one video track at a time.
     */
    int TOO_MANY_TRACKS = 300;
    /**
     *  An invalid video capturer was added to the local media
     *  @note: At the moment, only {@link CameraCapturer} is supported.
     */
    int INVALID_VIDEO_CAPTURER = 301;
    /**
     *  An attempt was made to add or remove a track that is already being operated on.
     *  @note: Retry your request at a later time.
     */
    int TRACK_OPERATION_IN_PROGRESS = 303;
    /**
     *  An attempt was made to remove a track that has already ended.
     *  @note: The video track is in the {@link MediaTrackState} ENDED state.
     */
    int INVALID_VIDEO_TRACK_STATE = 305;

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
     *     <li>{@link ConversationsClientListener#onStartListeningForInvites(TwilioConversationsClient)}
     *     will be invoked if the client is listening for invites</li>
     *     <li>{@link ConversationsClientListener#onFailedToStartListening(TwilioConversationsClient,
     *     TwilioConversationsException)} (ConversationsClient)} will be invoked if an issue
     *     occurred while attempting to listen</li>
     * </ol>
     */
    void listen();

    /**
     * Stops listening for incoming conversations.
     *
     * <p>{@link ConversationsClientListener#onStopListeningForInvites(TwilioConversationsClient)}
     * will be invoked upon the completion of this process</p>
     */
    void unlisten();

    /**
     * Sends an invitation to start a conversation with the following participants and local media
     * configuration. The {@link TwilioConversationsClient} must be listening before sending an
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
     *         <li>{@link TwilioConversationsClient#CONVERSATION_REJECTED}</li>
     *         <li>{@link TwilioConversationsClient#CONVERSATION_IGNORED}</li>
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
     * configuration. The {@link TwilioConversationsClient} must be listening before sending an
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
     *         <li>{@link TwilioConversationsClient#CONVERSATION_REJECTED}</li>
     *         <li>{@link TwilioConversationsClient#CONVERSATION_IGNORED}</li>
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
