//
//  TSCCameraCapturer.cpp
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/28/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "webrtc/common_video/libyuv/include/webrtc_libyuv.h"
#include "webrtc/modules/video_capture/include/video_capture_factory.h"

#include "TSCCameraCapturer.h"
#include "TSCLogger.h"

namespace twiliosdk {
    
struct TSCVideoCaptureCapability : public webrtc::VideoCaptureCapability
{
    explicit TSCVideoCaptureCapability(const cricket::VideoFormat& format)
    {
        width = format.width;
        height = format.height;
        maxFPS = cricket::VideoFormat::IntervalToFps(format.interval);
        expectedCaptureDelay = 0;
        rawType = webrtc::kVideoI420;
        codecType = webrtc::kVideoCodecUnknown;
        interlaced = false;
    }
};
    
struct TSCCapturedFrame : public cricket::CapturedFrame
{
    TSCCapturedFrame(const webrtc::I420VideoFrame& frame, void* buffer, int length)
    {
        width = frame.width();
        height = frame.height();
        fourcc = cricket::FOURCC_I420;
        pixel_width = 1;
        pixel_height = 1;
        // Convert units from VideoFrame RenderTimeMs to CapturedFrame (nanoseconds).
        elapsed_time = frame.render_time_ms() * talk_base::kNumNanosecsPerMillisec;
        time_stamp = elapsed_time;
        data_size = length;
        data = buffer;
    }
};
    
#pragma mark-
    
TSCCameraCapturer::TSCCameraCapturer(ITSCVideoCaptureDataConsumer* owner, const std::string& deviceId)
{
    m_owner = owner;
    SetId(deviceId);
}

TSCCameraCapturer::~TSCCameraCapturer()
{
    m_owner = nullptr;
}
    
#pragma mark-

cricket::CaptureState
TSCCameraCapturer::Start(const cricket::VideoFormat& format)
{
    talk_base::scoped_ptr<cricket::VideoFormat> capture_format(new cricket::VideoFormat(format));
    SetCaptureFormat(capture_format.get());
    if (IsRunning()) {
        return cricket::CS_RUNNING;
    }
    
    if (m_module.get() == nullptr) {
        m_module = webrtc::VideoCaptureFactory::Create(0, GetId().c_str());
    }
    if (m_module.get() == nullptr) {
        TS_CORE_LOG_ERROR("Failed to start camera device %s", GetId().c_str());
        return cricket::CS_NO_DEVICE;
    }
    TSCVideoCaptureCapability capability(*capture_format);
    m_module->RegisterCaptureDataCallback(*this);
    if (m_module->StartCapture(capability) != 0) {
        TS_CORE_LOG_ERROR("Failed to start camera capturing on %s", GetId().c_str());
        return cricket::CS_FAILED;
    }
    TS_CORE_LOG_DEBUG("Started camera capturing on %s", GetId().c_str());
    SetCaptureState(cricket::CS_RUNNING);
    return cricket::CS_RUNNING;
}

void
TSCCameraCapturer::Stop()
{
    if (IsRunning()) {
        m_module->StopCapture();
        talk_base::Thread::Current()->Clear(this);
        m_module->DeRegisterCaptureDataCallback();
    }
    m_module = nullptr;
    if (cricket::CS_STOPPED != capture_state()) {
        SetCaptureState(cricket::CS_STOPPED);
    }
    TS_CORE_LOG_DEBUG("Stopped camera capturing on %s", GetId().c_str());
}

bool
TSCCameraCapturer::IsRunning()
{
    return (m_owner != nullptr) && (m_module.get() != nullptr) && (m_module->CaptureStarted());
}

bool
TSCCameraCapturer::IsScreencast() const
{
    return false;
}

#pragma mark-
    
bool
TSCCameraCapturer::GetBestCaptureFormat(const  cricket::VideoFormat& desired,
                                       cricket::VideoFormat* bestFormat)
{
    if (!bestFormat) {
        return false;
    }
    if (!VideoCapturer::GetBestCaptureFormat(desired, bestFormat)) {
        bestFormat->width = desired.width;
        bestFormat->height = desired.height;
        bestFormat->fourcc = cricket::FOURCC_I420;
        bestFormat->interval = desired.interval;
    }
    return true;
}

bool
TSCCameraCapturer::GetPreferredFourccs(std::vector<uint32>* fourccs)
{
    if (!fourccs) {
        return false;
    }
    fourccs->clear();
    fourccs->push_back(cricket::FOURCC_I420);
    fourccs->push_back(cricket::FOURCC_MJPG);
    return true;
}

void
TSCCameraCapturer::OnIncomingCapturedFrame(const int32_t id, webrtc::I420VideoFrame& sample)
{
    if (!IsRunning()) {
        return;
    }
    const int length = webrtc::CalcBufferSize(webrtc::kI420,
                                              sample.width(), sample.height());
    m_capture_buffer.resize(length);
    webrtc::ExtractBuffer(sample, length, &m_capture_buffer[0]);
    TSCCapturedFrame frame(sample, &m_capture_buffer[0], length);
    m_owner->onVideoCaptureDataFrame(frame);
}

void
TSCCameraCapturer::OnCaptureDelayChanged(const int32_t id, const int32_t delay)
{
}

} //namespace twiliosdk