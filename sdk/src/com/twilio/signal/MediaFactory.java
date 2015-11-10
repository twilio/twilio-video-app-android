package com.twilio.signal;

import com.twilio.signal.impl.LocalMediaImpl;

public class MediaFactory {
	
	public static LocalMedia createLocalMedia() {
		return new LocalMediaImpl();
	}
}
