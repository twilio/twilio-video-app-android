package com.twilio.video;

import java.util.List;

/**
 * Generic video capturing interface.
 */
public interface VideoCapturer {
    /**
     * Returns list of all supported video formats this capturer supports.
     */
    List<VideoFormat> getSupportedFormats();

    /**
     * Start capturing frames.
     *
     * @param captureFormat format to start capturing in
     * @param capturerListener consumes frames upon availability
     */
    void startCapture(VideoFormat captureFormat,
                      Listener capturerListener);

    /**
     * Stop capturing. This method must block until capturer has stopped.
     */
    void stopCapture();

    interface Listener {
        /**
         * Notify whether capturer started
         *
         * @param success true if capturer successfully started or false if there was a failure
         */
        void onCapturerStarted(boolean success);

        /**
         * Provides available video frame
         */
        void onFrameCaptured(VideoFrame videoFrame);
    }
}
