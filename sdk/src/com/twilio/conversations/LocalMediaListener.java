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

}
