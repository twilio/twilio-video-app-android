package com.twilio.signal;

import android.view.ViewGroup;

import com.twilio.signal.CameraCapturer.CameraSource;
import com.twilio.signal.impl.CameraCapturerImpl;

public class CameraCapturerFactory {
	
	public static CameraCapturer createCameraCapturer(
			CameraSource source, ViewGroup previewContainerView) {
		return new CameraCapturerImpl(source, previewContainerView);
	}

}
