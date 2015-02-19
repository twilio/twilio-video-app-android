/*
 * libjingle
 * Copyright 2012, Google Inc.
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

#ifndef TALK_APP_WEBRTC_TEST_TWILIOVIDEOTRACKRENDERER_H_
#define TALK_APP_WEBRTC_TEST_TWILIOVIDEOTRACKRENDERER_H_

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif

#include "talk/app/webrtc/mediastreaminterface.h"
#include "talk/base/sigslot.h"
#include "talk/media/base/videoframe.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif

#include "twiliosdk.h"

namespace twiliosdk {

class TwilioVideoTrackRenderer : public webrtc::VideoRendererInterface {
public:
    TwilioVideoTrackRenderer(const std::string& id, webrtc::VideoTrackInterface* videoTrack);

    ~TwilioVideoTrackRenderer();

    virtual void SetSize(int width, int height);
    virtual void RenderFrame(const cricket::VideoFrame* frame);

    void start();
    void stop();

    bool started() const;

    const std::string& id() const;

    int Release();
    int AddRef();

    sigslot::signal5<const std::string&, const uint32, const uint32, uint8_t**, TwilioSdkSourceFormat*> SignalVideoStarted;
    sigslot::signal1<const std::string&> SignalDataAvailable;

private:

    void fill_buffer(const cricket::VideoFrame* frame);
    void fill_fourcc(TwilioSdkSourceFormat type);

    int ref_count_;
    std::string id_;
    talk_base::scoped_refptr<webrtc::VideoTrackInterface> video_track_;

    uint8_t* buffer_; //will be provided from outdoors, so no need on safe saving
    uint32 buffer_size_;

    uint32 fourcc_type_; //will be provided from outdoors

    uint32 width_;
    uint32 height_;
    bool video_start_signaled_;
    bool video_started_;
};

}  // namespace twiliosdk 

#endif  // TALK_APP_WEBRTC_TEST_TWILIOVIDEOTRACKRENDERER_H_
