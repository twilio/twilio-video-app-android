package com.twilio.signal;

import android.view.ViewGroup;

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
			CameraSource source,
			ViewGroup previewContainerView,
			CameraErrorListener listener) throws CameraException {
		return CameraCapturerImpl.create(source, previewContainerView, listener);
	}

}
