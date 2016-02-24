package com.twilio.conversations;

/**
 * A listener that provides errors related to the {@link CameraCapturer}
 *
 */
public interface CapturerErrorListener {
    /**
     * Reports runtime errors that can occur in the {@link CameraCapturer}
     */
    void onError(CapturerException e);
}
