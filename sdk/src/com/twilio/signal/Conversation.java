package com.twilio.signal;

import java.util.Set;

/**
 * A Conversation represents a communication from one ConversationsClient to one or more conversation clients.
 */

public interface Conversation {
			
	/**
	 * Returns the list of Participants in a Conversation.
	 * 
	 * @return participants - list of {@link Participant} in this Conversation.
	 */
	public Set<Participant> getParticipants();
	
	
	/**
	 * Get representation of the local video and audio stream
	 * 
	 * @return
	 */
	public LocalMedia getLocalMedia();
	
	
	/**
	 * Get listener for this Conversation
	 * 
	 * @return Conversation listener
	 */
	public ConversationListener getConversationListener();
	
	/**
	 * Set listener for this Conversation
	 * 
	 * @param ConversationListener
	 */
	public void setConversationListener(ConversationListener listener);
	
	
	/**
	 * Invite a set of participants.
	 *
	 * @param participantAddresses A set of strings representing the names of the participants.
	 */
	public void invite(Set<String> participantAddresses) throws IllegalArgumentException;

	/**
	 * Disconnect from this conversation
	 */
	public void disconnect();

	/**
	 *
	 */
	public String getConversationSid();
	
	/**
	 * Releases resources associated with this Conversation object.
	 * 
	 * Attempts to use this Conversation object after disposal will result in an IllegalStateException.
	 */
	public void dispose();


}
