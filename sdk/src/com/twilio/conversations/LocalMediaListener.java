package com.twilio.conversations;

/**
 * LocalMediaListener interface defines a set of callbacks for events related to a
 * {@link LocalMedia}.
 *
 */
public interface LocalMediaListener {

	/**
	 * This method notifies the listener when a {@link LocalVideoTrack} has been added
	 * to the {@link LocalMedia}
	 *
	 * @param localMedia The local media associated with this track.
	 * @param videoTrack The local video track that was added to the conversation.
	 */
	void onLocalVideoTrackAdded(LocalMedia localMedia, LocalVideoTrack videoTrack);

	/**
	 * This method notifies the listener when a {@link LocalVideoTrack} has been removed
	 * from the {@link LocalMedia}
	 *
	 * @param localMedia The local media associated with this track.
	 * @param videoTrack The local video track that was removed from the conversation.
	 */
	void onLocalVideoTrackRemoved(LocalMedia localMedia, LocalVideoTrack videoTrack);

	/**
	 * This method notifies the listener when an error occurred when
	 * attempting to add or remove a {@link LocalVideoTrack}
	 * @param localMedia The {@link LocalMedia} associated with the {@link LocalVideoTrack}
	 * @param track The {@link LocalVideoTrack} that was requested to be added or removed to the {@link LocalMedia}
	 * @param exception Provides the error that occurred while attempting to add or remove this {@link LocalVideoTrack}.
	 *                  Adding or removing a local video track can result in TOO_MANY_TRACKS, TRACK_OPERATION_IN_PROGRESS,
	 *                  INVALID_VIDEO_CAPTURER, or INVALID_VIDEO_TRACK_STATE.
	 */
	void onLocalVideoTrackError(LocalMedia localMedia, LocalVideoTrack track, TwilioConversationsException exception);

}
