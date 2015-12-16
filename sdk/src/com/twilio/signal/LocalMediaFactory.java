package com.twilio.signal;

import com.twilio.signal.impl.LocalMediaImpl;

public class LocalMediaFactory {
	
	/**
	 * Create new instance of local media
	 * 
	 * @return instance of local media
	 */
	public static LocalMedia createLocalMedia(LocalMediaListener localMediaListener) {
		return new LocalMediaImpl(localMediaListener);
	}
}
