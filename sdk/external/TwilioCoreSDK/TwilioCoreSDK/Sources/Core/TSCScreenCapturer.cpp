//
//  TSCScreenCapturer.cpp
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/28/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "webrtc/common_video/libyuv/include/webrtc_libyuv.h"

#include "TSCScreenCapturer.h"
#include "TSCScreenCaptureProvider.h"
#include "TSCLogger.h"

namespace twiliosdk {

struct TSCScreenCapturedFrame : public cricket::CapturedFrame
{
    TSCScreenCapturedFrame(const TSCScreenCaptureProviderFrame& frame)
    {
        width = frame.m_width;
        height = frame.m_height;
        fourcc = cricket::FOURCC_ARGB;
        pixel_width = 1;
        pixel_height = 1;
        elapsed_time = 1 * talk_base::kNumNanosecsPerMillisec;
        time_stamp = elapsed_time;
        data_size = frame.m_size;
        data = frame.m_buffer;
    }
};
    
#pragma mark-
    
template <class T>
class TSCScreenCaptureDataCallback: public ITSCScreenCaptureDataCallback
{
public:
    
    TSCScreenCaptureDataCallback(T* delegate)
    {
        m_delegate = delegate;
    }
    
    void onCapturedFrame(const TSCScreenCaptureProviderFrame& frame)
    {
        m_delegate->onCapturedFrame(frame);
    }
private:
    
    T* m_delegate;
};

#pragma mark-
    
TSCScreenCapturer::TSCScreenCapturer(ITSCVideoCaptureDataConsumer* owner, const TSCScreenCaptureProviderRef& provider) :
    kTSCScreenFrameRate(10)
{
    m_owner = owner;
    m_provider = provider;
}

TSCScreenCapturer::~TSCScreenCapturer()
{
    m_owner = nullptr;
}
    
#pragma mark-
    
cricket::CaptureState
TSCScreenCapturer::Start(const cricket::VideoFormat& format)
{
    if (capture_state() == cricket::CS_RUNNING) {
        return cricket::CS_RUNNING;
    }
    if (m_provider.get() == nullptr) {
        return cricket::CS_NO_DEVICE;
    }
    talk_base::scoped_refptr<TSCScreenCaptureDataCallback<TSCScreenCapturer>>
    callback(new talk_base::RefCountedObject<
             TSCScreenCaptureDataCallback<TSCScreenCapturer>>(this));
    m_provider->start(kTSCScreenFrameRate, callback);
    SetCaptureState(cricket::CS_RUNNING);
    return cricket::CS_RUNNING;
}

void
TSCScreenCapturer::Stop()
{
    m_provider->stop();
    if (cricket::CS_STOPPED != capture_state()) {
        SetCaptureState(cricket::CS_STOPPED);
    }
}

bool
TSCScreenCapturer::IsRunning()
{
    return (m_owner != nullptr) && capture_state() == cricket::CS_RUNNING;
}

bool
TSCScreenCapturer::IsScreencast() const
{
    return true;
}

#pragma mark-

bool
TSCScreenCapturer::GetBestCaptureFormat(const  cricket::VideoFormat& desired,
                                       cricket::VideoFormat* bestFormat)
{
    if (!bestFormat) {
        return false;
    }
    if (!VideoCapturer::GetBestCaptureFormat(desired, bestFormat)) {
        bestFormat->width = desired.width;
        bestFormat->height = desired.height;
        bestFormat->fourcc = cricket::FOURCC_ARGB;
        bestFormat->interval = desired.interval;
    }
    return true;
}

bool
TSCScreenCapturer::GetPreferredFourccs(std::vector<uint32>* fourccs)
{
    if (!fourccs) {
        return false;
    }
    fourccs->clear();
    fourccs->push_back(cricket::FOURCC_ARGB);
    return true;
}
    
void
TSCScreenCapturer::onCapturedFrame(const TSCScreenCaptureProviderFrame& frame)
{
    TSCScreenCapturedFrame sample(frame);
    m_owner->onVideoCaptureDataFrame(sample);
}

} //namespace twiliosdk