package com.twilio.signal;

import java.util.Map;

import android.app.PendingIntent;


/** 
 * An instance of Endpoint is an object that knows how to interface with Twilio SIGNAL Services.
 *
 * A Endpoint is the primary entry point for Twilio SIGNAL Client. An Android application should initialize a Endpoint 
 * with a Capability Token to talk to Twilio SIGNAL services.
 *
 * @see EndpointListener
 */


public interface Endpoint {
	
	/**
	 * Constructs a new Endpoint from a token string.
	 * 
	 * @param token  capability token for this Endpoint.
	 * @param listener A {@link EndpointListener}, or null
	 * 
	 */
	public Endpoint initWithToken(String token, EndpointListener listener);
	
	
	/**
	 * Constructs a new Endpoint from a Map of parameters
	 * 
	 * @param token  capability token for this Endpoint.
	 * @param params - list of media stream constraints.
	 * @param listener A {@link EndpointListener}, or null
	 * 
	 */
	public Endpoint initWithToken(String token, Map<String, String> params);	
	
	/**
	 * Start listening for incoming Conversation.
	 * 
	 * Registers this Endpoint with a given token. Endpoint is ready to receive Conversation invite once it registers. 
	 *
	 */
	public void listen();
	
	/**
	 * Stop listening for incoming Conversations.
	 * 
	 * Unregisters this Endpoint. Once this method is called Endpoint will not be able to receive any Conversation invite
	 * until Endpoint registers again. {@link EndpointListener#onStopListening(Endpoint)} is called after unregistration completes.
	 */
	public void unlisten();

	
	/**
	 * Leave a Conversation.
	 * 
	 * @param Conversation - The {@link Conversation} to leave.
	 * 
	 */
	public void leaveConversaton(Conversation conversation);
	
	
	/**
	 * Mutes or unmutes the microphone's audio for all the Conversations for this Endpoint.
	 * 
	 * @param muted <code>true</code> to mute, <code>false</code> to un-mute
	 * 
	 * @see Endpoint#isMuted()
	 */
	public void setMuted(boolean muted);
	

	/**
	 * Mutes or unmutes the microphone's audio for a given Conversations for this Endpoint.
	 * 
	 * @param muted <code>true</code> to mute, <code>false</code> to un-mute
	 * 
	 * @see Endpoint#isMuted()
	 */
	public void setMuted(boolean muted, Conversation conversation);
	
	
	/**
	 * Reports whether the microphone's audio is muted for all the Conversations for this Endpoint.
	 * 
	 * @return <code>true</code> if the audio is muted, </code>false</code> otherwise.
	 * 
	 * @see Endpoint#setMuted(boolean)
	 */
	public boolean isMuted();
	
	/**
	 * Reports whether the microphone's audio is muted for a given Conversations for this Endpoint.
	 * 
	 * @return <code>true</code> if the audio is muted, </code>false</code> otherwise.
	 * 
	 * @see Endpoint#setMuted(boolean)
	 */
	public boolean isMuted(Conversation conversation);


	/**
	 * Pause or un-pause video for a given Conversation.
	 * 
	 * @param Conversation - suspend video for this {@link Conversation}
	 * @param paused <code>true</code> to pause, <code>false</code> to un-pause
	 * 
	 */
	//public void pauseVideo(Conversation conversation, boolean paused);

	/**
	 * Pause or un-pause video for all Conversations for this Endpoint.
	 * 
	 * @param paused <code>true</code> to pause, <code>false</code> to un-pause
	 * 
	 */
	//public void pauseVideo(boolean paused);
	
	/**
	 * Current capabilities of the Endpoint.
	 * 
	 * The keys are defined by the {@link Capability} enum.
	 * 
	 * @return A key/value mapping of capabilities
	 */
	public Map<Capability, Object> getCapabilities();
	
	/**
	 * Updates the capabilities of the Endpoint.
	 *
	 * @param token The new capability token string.
	 *
	 */
	public void updateCapabilityToken(String token);
	
	
	/**
	 * Sets a new {@link EndpointListener} object to respond to device events.
	 * 
	 * @param listener A {@link EndpointListener}, or null
	 */
	public void setEndpointListener(EndpointListener listener);
	
	/**
	 * Sets a PendingIntent that will be sent when an incoming Conversation invite is received.
	 * 
	 * The PendingIntent passed may be a reference to an Activity, Service, or
	 * BroadcastReceiver. Your component receives the intent, act (accept, ignore, or reject)
	 * on the incoming Conversation invite.
	 * 
	 * @param intent A PendingIntent to call when this Endpoint receives a Conversation invite.
	 */
	public void setIncomingIntent(PendingIntent intent);
	
	/**
	 * This is a convenience method for creating a conversation with a single remote endpoint.
	 * 
	 * @param remoteEndpoint Remote endpoint name of type String.
	 * @param options Dictionary of options with media constraints (audio only, audio & video, etc.)
	 * @param linstener Callback Listener object for handling conversation related events.
	 */
	
	public void createConversationWithRemoteEndpoint(String remoteEndpoint, Map<String, String> options, ConversationListener linstener);
	

}
