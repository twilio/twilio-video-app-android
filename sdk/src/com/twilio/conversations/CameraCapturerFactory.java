package com.twilio.conversations;

import android.view.ViewGroup;
import android.content.Context;

import com.twilio.conversations.CameraCapturer.CameraSource;
import com.twilio.conversations.impl.CameraCapturerImpl;

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
			CapturerErrorListener listener) {
		return CameraCapturerImpl.create(context, source, previewContainer, listener);
	}

}
