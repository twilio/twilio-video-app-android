//
//  TSCScreenCapturer.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/28/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_SCREEN_CAPTURER_H
#define TSC_SCREEN_CAPTURER_H

#include "talk/media/base/videocapturer.h"

#include "TSCoreSDKTypes.h"
#include "TSCScreenCaptureProvider.h"
#include "TSCMediaDeviceInfo.h"
#include "ITSCVideoCaptureDataConsumer.h"

namespace twiliosdk {
    
class TSCScreenCapturer : public cricket::VideoCapturer
{
public:
    TSCScreenCapturer(ITSCVideoCaptureDataConsumer* owner, const TSCScreenCaptureProviderRef& provider);
    virtual ~TSCScreenCapturer();
    
    bool GetBestCaptureFormat(const  cricket::VideoFormat& desired,
                              cricket::VideoFormat* bestFormat);
    
    virtual cricket::CaptureState Start(const cricket::VideoFormat& format);
    virtual void Stop();
    virtual bool IsRunning();
    virtual bool IsScreencast() const;
    
    virtual void onCapturedFrame(const TSCScreenCaptureProviderFrame& frame);
protected:
    virtual bool GetPreferredFourccs(std::vector<uint32>* fourccs);
private:
    TSCScreenCapturer();

    const int kTSCScreenFrameRate;
    TSCScreenCaptureProviderRef m_provider;
    ITSCVideoCaptureDataConsumer* m_owner;
};
    
} // namespace twiliosdk

#endif // TSC_SCREEN_CAPTURER_H
