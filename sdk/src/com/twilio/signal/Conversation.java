package com.twilio.signal;

import java.util.Set;

/**
 * A Conversation represents a communication from one ConversationsClient to one or more conversation clients.
 */

public interface Conversation {

	/**
	 * An enum describing the current status of the Conversation.
	 */
	public enum Status
	{
		/** Local ConversationsClient's connection to Conversation has an unknown status */
		UNKNOWN,
		/** Local ConversationsClient is connecting to the Conversation */
		CONNECTING,
		/** Local ConversationsClient is connected to the Conversation.*/
		CONNECTED,
		/** Local ConversationsClient is disconnected from the Conversation */
		DISCONNECTED,
		/** Local ConversationsClient failed to connect to Conversation */
		FAILED
	};
	
	/**
	 * Retrieves the current state of the connection.
	 * 
	 * @return The connection state, as a {@link State} enum value
	 * 
	 * @see State
	 */
	public Conversation.Status getStatus();
		
	
	/**
	 * Returns the list of Participants in a Conversation.
	 * 
	 * @return participants - list of {@link Participant} in this Conversation.
	 */
	public Set<String> getParticipants();
	
	
	/**
	 * Get a conversation's local media
	 * 
	 * @return
	 */
	public LocalMedia getLocalMedia();
	
	
	/**
	 * Get the conversation's listener
	 * 
	 * @return
	 */
	public ConversationListener getConversationListener();
	
	/**
	 *
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
