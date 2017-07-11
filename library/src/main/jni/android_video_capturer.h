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

#ifndef VIDEO_ANDROID_ANDROID_VIDEO_CAPTURER_H_
#define VIDEO_ANDROID_ANDROID_VIDEO_CAPTURER_H_

#include <string>
#include <vector>

#include "webrtc/base/thread_checker.h"
#include "webrtc/common_video/include/video_frame_buffer.h"
#include "webrtc/media/base/videocapturer.h"
#include "webrtc/base/refcount.h"

/*
 * The androidvideocapturer was removed in WebRTC 55. The delegate and capturer defined below are
 * ports of the implementations found in WebRTC 54. Original source can be found at
 * https://code.hq.twilio.com/client/twilio-webrtc/blob/twilio-webrtc-54/webrtc/api/androidvideocapturer.h
 */
class AndroidVideoCapturer;
class AndroidVideoCapturerDelegate : public rtc::RefCountInterface {
public:
    virtual ~AndroidVideoCapturerDelegate() {}
    /*
     * Start capturing. The implementation of the delegate must call
     * AndroidVideoCapturer::OnCapturerStarted with the result of this request.
     */
    virtual void Start(const cricket::VideoFormat& capture_format,
                       AndroidVideoCapturer* capturer) = 0;

    /*
     * Stops capturing.
     * The delegate may not call into AndroidVideoCapturer after this call.
     */
    virtual void Stop() = 0;

    virtual std::vector<cricket::VideoFormat> GetSupportedFormats() = 0;
    virtual bool IsScreencast() = 0;
};

/*
 * Android implementation of cricket::VideoCapturer for use with WebRtc PeerConnection.
 */
class AndroidVideoCapturer : public cricket::VideoCapturer {
public:
    explicit AndroidVideoCapturer(
            const rtc::scoped_refptr<AndroidVideoCapturerDelegate>& delegate);
    virtual ~AndroidVideoCapturer();

    // Called from JNI when the capturer has been started.
    void OnCapturerStarted(bool success);

    // Called from JNI to request a new video format.
    void OnOutputFormatRequest(int width, int height, int fps);

    AndroidVideoCapturerDelegate* delegate() { return delegate_.get(); }

    // cricket::VideoCapturer implementation.
    bool GetBestCaptureFormat(const cricket::VideoFormat& desired,
                              cricket::VideoFormat* best_format) override;

    // Expose these protected methods as public, to be used by the VideoCapturerDelegate.
    using cricket::VideoCapturer::AdaptFrame;
    using cricket::VideoCapturer::OnFrame;

private:
    /*
     * cricket::VideoCapturer implementation.
     * Video frames will be delivered using
     * cricket::VideoCapturer::SignalFrameCaptured on the thread that calls Start.
     */
    cricket::CaptureState Start(const cricket::VideoFormat& capture_format) override;
    void Stop() override;
    bool IsRunning() override;
    bool IsScreencast() const override { return delegate_->IsScreencast(); }
    bool GetPreferredFourccs(std::vector<uint32_t>* fourccs) override;

    bool running_;
    rtc::scoped_refptr<AndroidVideoCapturerDelegate> delegate_;

    rtc::ThreadChecker thread_checker_;
};

#endif // VIDEO_ANDROID_ANDROID_VIDEO_CAPTURER_H_
