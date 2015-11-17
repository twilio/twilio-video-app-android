package com.twilio.signal;

import android.view.ViewGroup;
import android.content.Context;

import com.twilio.signal.CameraCapturer.CameraSource;
import com.twilio.signal.impl.CameraCapturerImpl;

public class CameraCapturerFactory {
	
	/**
	 * Creates instance of CameraCapturer
	 * 
	 * @param source
	 * @param previewContainer
	 * @return CameraCapturer
	 */
	public static CameraCapturer createCameraCapturer (
			Context context,
			CameraSource source,
			ViewGroup previewContainer,
			CameraErrorListener listener) {
		return CameraCapturerImpl.create(context, source, previewContainer, listener);
	}

}
