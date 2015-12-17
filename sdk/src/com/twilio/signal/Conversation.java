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
	 * @param ConversationListener listener
	 */
	public void setConversationListener(ConversationListener listener);
	
	
	/**
	 * Invite participant(s) to conversation
	 *
	 * @param participantAddresses A set of strings representing the names of the participants.
	 * @throws IllegalArgumentException is thrown if participantAddresses is null or empty.
	 */
	public void invite(Set<String> participantAddresses) throws IllegalArgumentException;

	/**
	 * Disconnect from this conversation
	 */
	public void disconnect();

	/**
	 * Get conversation SID
	 * 
	 * @return String conversation SID
	 */
	public String getConversationSid();
	
	/**
	 * Releases resources associated with this Conversation object.
	 * 
	 * Attempts to use this Conversation object after disposal will result in an IllegalStateException.
	 */
	public void dispose();


}
