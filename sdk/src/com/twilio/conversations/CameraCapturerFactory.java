package com.twilio.conversations;

import android.app.Activity;
import android.view.ViewGroup;
import android.content.Context;

import com.twilio.conversations.CameraCapturer.CameraSource;
import com.twilio.conversations.impl.CameraCapturerImpl;

/**
 * A factory for creating an instance of {@link CameraCapturer}
 *
 */
public class CameraCapturerFactory {

	/**
	 * Creates an instance of CameraCapturer
	 * 
	 * @param source the camera source
	 * @param previewContainer the view where the preview will be shown
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
