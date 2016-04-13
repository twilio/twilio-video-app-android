package com.twilio.conversations;

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
     * @return CameraCapturer
     */
    public static CameraCapturer createCameraCapturer (
            Context context,
            CameraSource source,
            CapturerErrorListener listener) {
        return CameraCapturerImpl.create(context, source, listener);
    }
}
