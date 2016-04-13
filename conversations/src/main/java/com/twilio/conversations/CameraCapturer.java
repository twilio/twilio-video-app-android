package com.twilio.conversations;

import android.view.ViewGroup;

/**
 * A camera capturer retrieves frames from a
 * camera source on the device which can be previewed onto
 * a view.
 *
 */
public interface CameraCapturer {
    /**
     * Camera source types
     */
    enum CameraSource {
        CAMERA_SOURCE_FRONT_CAMERA,
        CAMERA_SOURCE_BACK_CAMERA
    };

    /**
     * Starts previewing the camera within the provided ViewGroup
     * @param previewContainer View where camera will be previewed
     */
    void startPreview(ViewGroup previewContainer);

    /**
     * Starts previewing the camera.
     */
    void startPreview();

    /**
     * Stops previewing the camera.
     */
    void stopPreview();

    /**
     * Returns whether the camera capturer is previewing the camera
     */
    boolean isPreviewing();

    /**
     * Switches the camera to the next available camera source.
     */
    boolean switchCamera();
}