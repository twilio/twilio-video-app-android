package com.twilio.signal;

import com.twilio.signal.Track.TrackId;

public interface ConversationListener {

	
	/**
	 * Called after the {@link RemoteEndpoint} has successfully joined a
	 * {@link Conversation}.
	 * 
	 * @param endpoint The RemoteEndpoint that has joined the conversation.
	 */
	void onRemoteEndpointJoined(RemoteEndpoint endpoint);
	
	/**
	 * This method notifies the listener that a remote endpoint has left the conversation.
	 * 
	 * @param endpoint The remote endpoint that left the conversation.
	 */
	void onRemoteEndpointLeftConversation(RemoteEndpoint endpoint);

	/**
	 * This method notifies the listener that a remote endpoint has rejected the invitation to join the conversation.
	 * 
	 * @param endpoint The remote endpoint that rejected the conversation.
	 *           
	 */
	void onRemoteEndpointRejectedInvite(RemoteEndpoint endpoint);
	
	/**
	 * This method notifies the listener that a remote endpoint has added a track to its media stream.
	 * 
	 * @param endpoint The remote endpoint that added the track.
	 * @param trackId The track id.
	 */
	void onAddTrackWithId(RemoteEndpoint endpoint, TrackId trackId);
	
	/**
	 * This method notifies the listener that a remote endpoint has removed a track from its media stream.
	 * 
	 * @param endpoint The remote endpoint that added the track.
	 * @param trackId The track id.
	 */
	void onRemoveTrackWithId(RemoteEndpoint endpoint, TrackId trackId);
	
	/**
	 * This method notifies the listener that a remote endpoint paused one or more video tracks in its media stream.
	 * 
	 * @param endpoint The remote endpoint that paused the video track(s).
	 * @param stream The stream object.
	 */
	void onPauseVideo(RemoteEndpoint endpoint, Stream stream);
	
	/**
	 * This method notifies the listener that a remote endpoint muted one or more audio tracks in its media stream.
	 * 
	 * @param endpoint The remote endpoint that muted the audio track(s).
	 * @param stream The stream object.
	 */
	void onMuteAudio(RemoteEndpoint endpoint, Stream stream);

}
