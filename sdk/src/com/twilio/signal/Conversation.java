package com.twilio.signal;

import java.util.Set;

/**
 * A Conversation represents a communication from one ConversationsClient to one or more conversation clients.
 */

public interface Conversation {
	
	/**
	 * An enum describing the current status of the conversation.
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
	 * TODO: Remove from public interface
	 */
	public Conversation.Status getStatus();
			
	/**
	 * Returns the list of participants in a conversation.
	 * 
	 * @return participants list of {@link Participant} in this conversation.
	 */
	public Set<Participant> getParticipants();
	
	
	/**
	 * Get representation of the local video and audio stream
	 * 
	 * @return
	 */
	public LocalMedia getLocalMedia();
	
	
	/**
	 * Get listener for this conversation
	 * 
	 * @return Conversation listener
	 */
	public ConversationListener getConversationListener();
	
	/**
	 * Set listener for this conversation
	 * 
	 * @param ConversationListener listener
	 */
	public void setConversationListener(ConversationListener listener);
	
	
	/**
	 * Invite participant identities to this conversation
	 *
	 * @param participantAddresses A set of strings representing the names of the participants.
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
	 * Releases resources associated with this conversation object.
	 * 
	 * Attempts to use this conversation object after disposal will result in an IllegalStateException.
	 */
	public void dispose();


}
