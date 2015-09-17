package com.twilio.signal.impl;

import android.content.Context;

import com.twilio.signal.impl.VideoSurface;
import com.twilio.signal.impl.logging.Logger;

public class VideoSurfaceFactory {

	static final Logger logger = Logger.getLogger(VideoSurfaceFactory.class);

	public static VideoSurface createVideoSurface(Context context,
		VideoSurface.Observer observer) {
		long nativeObserver = nativeCreateVideoSurfaceObserver(observer);
		if(nativeObserver == 0) {
			logger.i("Video surface observer returned null");
			return null;
		} else {
			logger.i("Video surface observer not null");
		}
		long nativeVideoSurface = nativeCreateVideoSurface(nativeObserver);
		if(nativeVideoSurface == 0) {
			logger.i("Video surface returned null");
			return null;
		} else {
			logger.i("Video surface not null");
		}

		return new VideoSurface(context, nativeVideoSurface, nativeObserver);
	}

	private static native long nativeCreateVideoSurfaceObserver(
		VideoSurface.Observer observer);

	private static native long nativeCreateVideoSurface(
      		long nativeObserver);

}

