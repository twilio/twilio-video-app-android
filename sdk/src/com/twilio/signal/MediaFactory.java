package com.twilio.signal;

import com.twilio.signal.impl.LocalMediaImpl;

public class MediaFactory {
	
	/**
	 * Create new instance of local media
	 * 
	 * @return instance of local media
	 */
	public static LocalMedia createLocalMedia() {
		return new LocalMediaImpl();
	}
}
