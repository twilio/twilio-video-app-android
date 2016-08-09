package com.twilio.video;

/**
 * A VideoRenderer is used to receive frames from a {@link VideoTrack}
 *
 */
public interface VideoRenderer {
    interface Observer {
        /**
         * This method notifies the observer when the first frame has arrived.
         */
        void onFirstFrame();

        /**
         * This method notifies the observer when the frame dimensions have changed.
         *
         * @param width frame width
         * @param height frame height
         * @param rotation frame rotation
         */
        void onFrameDimensionsChanged(int width, int height, int rotation);
    }

    /**
     * Provides the YUV frame in I420 format
     *
     * @param frame I420 YUV frame
     */
    void renderFrame(I420Frame frame);
}

