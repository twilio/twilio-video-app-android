//
//  TSCCameraCapturer.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/28/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_CAMERA_CAPTURER_H
#define TSC_CAMERA_CAPTURER_H

#include "talk/media/base/videocapturer.h"
#include "webrtc/modules/video_capture/include/video_capture.h"

#include "TSCoreSDKTypes.h"
#include "TSCMediaDeviceInfo.h"
#include "ITSCVideoCaptureDataConsumer.h"

namespace twiliosdk {

class TSCCameraCapturer : public cricket::VideoCapturer,
                          public webrtc::VideoCaptureDataCallback
{
public:
    TSCCameraCapturer(ITSCVideoCaptureDataConsumer* owner, const std::string& deviceId);
    virtual ~TSCCameraCapturer();
    
    bool GetBestCaptureFormat(const  cricket::VideoFormat& desired,
                              cricket::VideoFormat* bestFormat);
    
    virtual cricket::CaptureState Start(const cricket::VideoFormat& format);
    virtual void Stop();
    virtual bool IsRunning();
    virtual bool IsScreencast() const;
protected:
    virtual bool GetPreferredFourccs(std::vector<uint32>* fourccs);
private:
    TSCCameraCapturer();
    // Callback when a frame is captured by camera.
    virtual void OnIncomingCapturedFrame(const int32_t id,
                                         webrtc::I420VideoFrame& sample);

    virtual void OnCaptureDelayChanged(const int32_t id,
                                       const int32_t delay);

    talk_base::scoped_refptr<webrtc::VideoCaptureModule> m_module;
    std::vector<uint8_t> m_capture_buffer;
    ITSCVideoCaptureDataConsumer*  m_owner;
};
    
} // namespace twiliosdk

#endif // TSC_CAMERA_CAPTURER_H
