//
//  TSCVideoCapturer.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/28/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_VIDEO_CAPTURER_H
#define TSC_VIDEO_CAPTURER_H

#include "talk/media/base/videocapturer.h"
#include "ITSCVideoCaptureDataConsumer.h"
#include "TSCoreSDKTypes.h"
#include "TSCMediaDeviceInfo.h"

namespace twiliosdk {

class TSCVideoCapturer : public cricket::VideoCapturer,
                         public ITSCVideoCaptureDataConsumer
{
public:
    TSCVideoCapturer(const std::string& deviceId);
    virtual ~TSCVideoCapturer();
    
    bool GetBestCaptureFormat(const  cricket::VideoFormat& desired,
                              cricket::VideoFormat* bestFormat);
    
    virtual cricket::CaptureState Start(const cricket::VideoFormat& format);
    virtual void Stop();
    virtual bool IsRunning();
    virtual bool IsScreencast() const;
    
    void setCapturingDevice(const std::string& deviceInfo);
    const std::string getCapturingDevice();
    
    //ITSCVideoCaptureDataConsumer
    virtual void onVideoCaptureDataFrame(const cricket::CapturedFrame& frame);
protected:
    virtual bool GetPreferredFourccs(std::vector<uint32>* fourccs);
private:
    TSCVideoCapturer();

    talk_base::scoped_ptr<cricket::VideoCapturer> m_capturer;
    std::string m_device_id;
    talk_base::CriticalSection m_lock;
};
    
} // namespace twiliosdk

#endif // TSC_VIDEO_CAPTURER_H
