package com.twilio.signal;

import java.util.Set;

/**
 * A Conversation represents a communication from one Endpoint to one or more RemoteEndpoint. 
 * You should not call Conversation's constructor directly; instead, call Endpoint#createSession.
 *
 */

public interface Conversation {

	/**
	 * An enum describing the current status of the Conversation.
	 */
	public enum Status
	{
		/** Local Endpoint's connection to Conversation has an unknown status */
		UNKNOWN,
		/** Local Endpoint is connecting to the Conversation */
		CONNECTING,
		/** Local Endpoint is connected to the Conversation.*/
		CONNECTED,
		/** Local Endpoint is disconnected from the Conversation */
		DISCONNECTED,
		/** Local Endpoint failed to connect to Conversation */
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
	 * Returns the list of Participants in an active Session.
	 * 
	 * @return participants - list of {@link Participant} in this Session.
	 */	
	public Set<String> getParticipants();
	
	
	/**
	 * Get a remote endpoint's media stream.
	 * 
	 * @param endpoint The remote endpoint whose stream we are fetching.
	 * @return
	 */
	public Media getLocalMedia();
	
	
	/**
	 * Get a remote endpoint's media stream.
	 * 
	 * @param endpoint The remote endpoint whose stream we are fetching.
	 * @return
	 */
	public ConversationListener getConversationListener();
	
	/**
	 *
	 */
	public void setConversationListener(ConversationListener listener);
	
	
	/**
	 * Invite a set of remote participantAddresses.
	 *
	 * @param participants A set of strings representing the names of the remote endpoints.
	 */
	public void invite(Set<String> participantAddresses);
	
	/**
	 *
	 */
	public String getConversationSid();


}
