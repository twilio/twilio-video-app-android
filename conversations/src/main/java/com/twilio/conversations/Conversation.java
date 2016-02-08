package com.twilio.conversations;

import com.twilio.conversations.impl.core.ConversationStatus;

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
	public Set<Participant> getParticipants();

	/**
	 * Returns the {@link LocalMedia} for this conversation
	 * 
	 * @return local media
	 */
	public LocalMedia getLocalMedia();

	/**
	 * Gets the {@link ConversationListener} of this conversation
	 * 
	 * @return listener of this conversation
	 */
	public ConversationListener getConversationListener();

	/**
	 * Sets the {@link ConversationListener} of this conversation
	 * 
	 * @param listener A listener of this conversation
	 */
	public void setConversationListener(ConversationListener listener);
	
	
	/**
	 * Invites one or more participants to this conversation
	 *
	 * @param participantIdentities A set of strings representing the identities of these participants.
	 */
	public void invite(Set<String> participantIdentities) throws IllegalArgumentException;

	/**
	 * Disconnects from this conversation
	 *
	 */
	public void disconnect();

	/**
	 * Gets the conversation SID
	 * 
	 * @return conversation SID
	 */
	public String getConversationSid();

	/**
	 * Releases resources associated with this conversation.
	 * 
	 * Attempts to use this conversation object after disposal will result in an IllegalStateException.
	 */
	public void dispose();

}
