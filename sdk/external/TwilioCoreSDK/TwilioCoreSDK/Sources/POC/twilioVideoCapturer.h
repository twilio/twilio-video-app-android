/*
 * libjingle
 * Copyright 2004 Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef TALK_MEDIA_BASE_TWILIOVIDEOCAPTURER_H_
#define TALK_MEDIA_BASE_TWILIOVIDEOCAPTURER_H_

#include <string.h>

#include <vector>

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif

#include "talk/base/timeutils.h"
#include "talk/media/base/videocapturer.h"
#include "talk/media/base/videocommon.h"
#include "talk/media/base/videoframe.h"
#include "webrtc/modules/video_capture/include/video_capture.h"
#include "webrtc/modules/utility/interface/process_thread.h"
#include "webrtc/system_wrappers/interface/tick_util.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif

namespace twiliosdk {

// Twilio video capturer that allows the test to manually pump in frames.
class TwilioVideoCapturer : public cricket::VideoCapturer,
                            public webrtc::VideoCaptureDataCallback,
                            public webrtc::VideoCaptureFeedBack {
public:
    TwilioVideoCapturer();
    virtual ~TwilioVideoCapturer();

    void ResetSupportedFormats(
            const std::vector<cricket::VideoFormat>& formats);

    virtual cricket::CaptureState Start(const cricket::VideoFormat& format);
    virtual void Stop();
    virtual bool IsRunning();
    virtual bool IsScreencast() const;
    void SetScreencast(bool is_screencast);
    void setCaptureFeedbackInterval(int64_t timeInMsec);
    bool GetPreferredFourccs(std::vector<uint32>* fourccs);

    sigslot::signal3<const uint32_t, const uint32_t, const uint32_t> SignalCaptureFeedbackAvailable;

private:

    // Initialize capturing module. Will need to change for device selection in the future
    bool Init();

    // Callback when a frame is captured by camera.
    virtual void OnIncomingCapturedFrame(const int32_t id,
                                         webrtc::I420VideoFrame& frame);

    virtual void OnCaptureDelayChanged(const int32_t id,
                                       const int32_t delay);

    // Callback that reports frame capture feedback
    virtual void OnCaptureFrameRate(const int32_t id,
                                    const uint32_t frameRate);
    virtual void OnNoPictureAlarm(const int32_t id,
                                  const webrtc::VideoCaptureAlarm alarm);

    //SignalVideoFrame Responder 
    void OnFrameAdapted(cricket::VideoCapturer*, const cricket::VideoFrame* video_frame);

private:
#ifdef WIN32
    static bool isComInitialized_;
#endif
    bool isScreencast_;
    talk_base::scoped_refptr<webrtc::VideoCaptureModule> module_;
    std::vector<uint8_t> capture_buffer_;
    webrtc::ProcessThread* process_module_;
    webrtc::TickTime last_feedback_report_time_;

    uint32_t last_captured_frame_rate_;
    int64_t capture_feedback_interval_;


};
}

#endif
