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

final class VideoCapturerListenerAdapter implements VideoCapturer.Listener {
    private final org.webrtc.VideoCapturer.CapturerObserver webRtcCapturerObserver;

    public VideoCapturerListenerAdapter(
            org.webrtc.VideoCapturer.CapturerObserver webRtcCapturerObserver) {
        this.webRtcCapturerObserver = webRtcCapturerObserver;
    }

    @Override
    public void onCapturerStarted(boolean success) {
        webRtcCapturerObserver.onCapturerStarted(success);
    }

    @Override
    public void onFrameCaptured(@NonNull VideoFrame videoFrame) {
        /*
         * Currently only Twilio capturers create VideoFrames that wrap org.webrtc.VideoFrame. All
         * other customer capturers use onByteBufferFrameCaptured.
         */
        if (videoFrame.webRtcVideoFrame != null) {
            webRtcCapturerObserver.onFrameCaptured(videoFrame.webRtcVideoFrame);
        } else {
            webRtcCapturerObserver.onByteBufferFrameCaptured(
                    videoFrame.imageBuffer,
                    videoFrame.dimensions.width,
                    videoFrame.dimensions.height,
                    videoFrame.orientation.getValue(),
                    videoFrame.timestamp);
        }
    }
}
