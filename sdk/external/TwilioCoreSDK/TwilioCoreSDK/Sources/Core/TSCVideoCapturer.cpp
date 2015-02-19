//
//  TSCVideoCapturer.cpp
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/28/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "webrtc/common_video/libyuv/include/webrtc_libyuv.h"
#include "webrtc/modules/video_capture/include/video_capture_factory.h"

#include "TSCVideoCapturer.h"
#include "TSCLogger.h"
#include "TSCDeviceManager.h"
#include "TSCCameraCapturer.h"
#include "TSCScreenCapturer.h"

namespace twiliosdk {
    
    
#pragma mark-
    
TSCVideoCapturer::TSCVideoCapturer(const std::string& deviceId)
{
    setCapturingDevice(deviceId);
}

TSCVideoCapturer::~TSCVideoCapturer()
{
}
    
#pragma mark-
    
void
TSCVideoCapturer::setCapturingDevice(const std::string& deviceId)
{
    if (m_device_id == deviceId) {
        return;
    }
    const bool need_restart = IsRunning();
    talk_base::scoped_ptr<cricket::VideoFormat> capture_format(
        GetCaptureFormat() ? new cricket::VideoFormat(*GetCaptureFormat()) : NULL);

    if (need_restart) {
        Stop();
    }
    
    m_device_id = deviceId;
    TSCDeviceManagerObjectRef device_manager = new TSCDeviceManagerObject;
    // grab the device type
    std::vector<TSCVideoCaptureDeviceInfo> devices = device_manager->getVideoCaptureDevices();
    TSCVideoCaptureDeviceType type = kTSCVideoCaptureNone;
    for (auto &device: devices) {
        if (device.getDeviceId() == m_device_id) {
            type = device.getDeviceType();
            break;
        }
    }
    if (type == kTSCVideoCaptureScreen) {
        m_capturer.reset(new TSCScreenCapturer(this, device_manager->getScreenCaptureProvider()));
    } else {
        m_capturer.reset(new TSCCameraCapturer(this, m_device_id));
    }
    if (need_restart) {
        Start(*capture_format);
    }
}
    
const std::string
TSCVideoCapturer::getCapturingDevice()
{
    return m_device_id;
}

cricket::CaptureState
TSCVideoCapturer::Start(const cricket::VideoFormat& format)
{
    talk_base::CritScope cs(&m_lock);
    talk_base::scoped_ptr<cricket::VideoFormat> capture_format(new cricket::VideoFormat(format));
    SetCaptureFormat(capture_format.get());
    if (m_capturer.get() != nullptr) {
        return m_capturer->Start(format);
    } else {
        return cricket::CS_NO_DEVICE;
    }
}

void
TSCVideoCapturer::Stop()
{
    talk_base::CritScope cs(&m_lock);
    if (m_capturer.get() != nullptr) {
        return m_capturer->Stop();
    }
    SetCaptureFormat(NULL);
    if (cricket::CS_STOPPED != capture_state()) {
        SetCaptureState(cricket::CS_STOPPED);
    }
    TS_CORE_LOG_DEBUG("Stopped video capturing on %s", m_device_id.c_str());
}

bool
TSCVideoCapturer::IsRunning()
{
    if (m_capturer.get() != nullptr) {
        return m_capturer->IsRunning();
    } else {
        return false;
    }
}

bool
TSCVideoCapturer::IsScreencast() const
{
    if (m_capturer.get() != nullptr) {
        return m_capturer->IsScreencast();
    } else {
        return false;
    }
}

#pragma mark-
    
bool
TSCVideoCapturer::GetBestCaptureFormat(const  cricket::VideoFormat& desired,
                                       cricket::VideoFormat* bestFormat)
{
    if (m_capturer.get() != nullptr) {
        return m_capturer->GetBestCaptureFormat(desired, bestFormat);
    } else {
        return false;
    }
}

bool
TSCVideoCapturer::GetPreferredFourccs(std::vector<uint32>* fourccs)
{
    if (!fourccs)
        return false;
    fourccs->push_back(cricket::FOURCC_I420);
    return true;
/*
    if (m_capturer.get() != nullptr) {
        return m_capturer->GetPreferredFourccs(fourccs);
    } else {
        return false;
    }
*/
}
    
void
TSCVideoCapturer::onVideoCaptureDataFrame(const cricket::CapturedFrame& frame)
{
    SignalFrameCaptured(this, &frame);
}

} //namespace twiliosdk