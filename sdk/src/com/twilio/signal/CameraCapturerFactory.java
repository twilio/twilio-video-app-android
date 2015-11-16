package com.twilio.signal;

import android.widget.FrameLayout;
import android.content.Context;

import com.twilio.signal.CameraCapturer.CameraSource;
import com.twilio.signal.impl.CameraCapturerImpl;

public class CameraCapturerFactory {
	
	/**
	 * Creates instance of CameraCapturer
	 * 
	 * @param source
	 * @param previewContainerView
	 * @return CameraCapturer
	 */
	public static CameraCapturer createCameraCapturer (
			Context context,
			CameraSource source,
			FrameLayout previewContainerView,
			CameraErrorListener listener) {
		return CameraCapturerImpl.create(context, source, previewContainerView, listener);
	}

}
