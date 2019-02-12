/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import android.support.annotation.NonNull;
import java.util.List;

/** Generic video capturing interface. */
public interface VideoCapturer {
    /** Returns list of all supported video formats this capturer supports. */
    @NonNull
    List<VideoFormat> getSupportedFormats();

    /**
     * Indicates whether the capturer is a screen cast.
     *
     * @return true if the video capturer is a screencast and false if not.
     */
    boolean isScreencast();

    /**
     * Starts capturing frames at the specified format. Frames should be provided to the given
     * listener upon availability.
     *
     * <p><b>Note</b>: This method is not meant to be invoked directly.
     *
     * @param captureFormat the format in which to capture frames.
     * @param capturerListener consumer of available frames.
     */
    void startCapture(@NonNull VideoFormat captureFormat, @NonNull Listener capturerListener);

    /**
     * Stops all frames being captured.
     *
     * <p><b>Note</b>: This method is not meant to be invoked directly.
     */
    void stopCapture();

    /**
     * Interface that allows an implementation of {@link VideoCapturer} to forward events to the
     * video capturer pipeline.
     */
    interface Listener {
        /**
         * Notify whether capturer started.
         *
         * @param success true if capturer successfully started or false if there was a failure.
         */
        void onCapturerStarted(boolean success);

        /** Provides available video frame. */
        void onFrameCaptured(@NonNull VideoFrame videoFrame);
    }
}
