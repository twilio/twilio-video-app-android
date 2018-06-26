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

import java.util.List;

/** Generic video capturing interface. */
public interface VideoCapturer {
  /** Returns list of all supported video formats this capturer supports. */
  List<VideoFormat> getSupportedFormats();

  /**
   * Indicates whether the capturer is a screen cast.
   *
   * @return true if the video capturer is a screencast and false if not.
   */
  boolean isScreencast();

  /**
   * Start capturing frames.
   *
   * @param captureFormat format to start capturing in.
   * @param capturerListener consumes frames upon availability.
   */
  void startCapture(VideoFormat captureFormat, Listener capturerListener);

  /** Stop capturing. This method must block until capturer has stopped. */
  void stopCapture();

  /**
   * Interface that allows an implementation of {@link VideoCapturer} to forward events to the video
   * capturer pipeline.
   */
  interface Listener {
    /**
     * Notify whether capturer started.
     *
     * @param success true if capturer successfully started or false if there was a failure.
     */
    void onCapturerStarted(boolean success);

    /** Provides available video frame. */
    void onFrameCaptured(VideoFrame videoFrame);
  }
}
