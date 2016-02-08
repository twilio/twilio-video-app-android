package com.twilio.conversations;

import com.twilio.conversations.impl.LocalMediaImpl;


/**
 * A factory for creating an instance of {@link LocalMedia}
 *
 */
public class LocalMediaFactory {

	/**
	 * Creates a new instance of the {@link LocalMedia}
	 * 
	 * @return instance of local media
	 */
	public static LocalMedia createLocalMedia(LocalMediaListener localMediaListener) {
		return new LocalMediaImpl(localMediaListener);
	}
}
