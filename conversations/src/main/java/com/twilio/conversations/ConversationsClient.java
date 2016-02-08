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
	 * Reflects current listening state of the conversations client
	 * 
	 * @return <code>true</code> if conversations client is listening, </code>false</code> otherwise.
 	 */
	boolean isListening();

	/**
	 * Starts listening for incoming conversations.
	 * 
	 */
	void listen();

	/**
	 * 
	 * Stops listening for incoming conversations.
	 * 
	 */
	void unlisten();

	/**
	 * Sends an invitation to start a conversation with the following participants and local media configuration
	 * 
	 * @param participants Set of participant names as Strings
	 * @param localMedia Local Media you would like to use when setting up the new conversation
	 * @param conversationCallback The callback that will provide the conversation once it's been created
	 * @throws ConversationClientException
	 */
	OutgoingInvite sendConversationInvite(Set<String> participants, LocalMedia localMedia, ConversationCallback conversationCallback);

	/**
	 * Releases resources associated with this ConversationsClient object.
	 * 
	 * Attempts to use this ConversationsClient object after disposal will result in an IllegalStateException.
	 */
	void dispose();

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
