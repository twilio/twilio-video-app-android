package com.twilio.video;

import android.hardware.Camera;

/**
 * Interface for updating {@link android.hardware.Camera.Parameters} on a {@link CameraCapturer}.
 */
public interface CameraParameterUpdater {
    /**
     * Invoked when camera parameters are available for modification.
     *
     * @param cameraParameters the current parameters for a {@link android.hardware.Camera}.
     */
    void applyCameraParameterUpdates(Camera.Parameters cameraParameters);
}
