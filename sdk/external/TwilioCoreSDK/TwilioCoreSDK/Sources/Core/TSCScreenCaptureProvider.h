//
//  TSCScreenCaptureProvider.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 2/4/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_SCREEN_CAPTURE_PROVIDER_H
#define TSC_SCREEN_CAPTURE_PROVIDER_H

#include "talk/base/asyncinvoker.h"
#include "talk/base/thread.h"

#include "TSCoreSDKTypes.h"

namespace twiliosdk {
    
    
struct TSCScreenCaptureProviderFrame
{
    int m_width;
    int m_height;
    uint8_t* m_buffer;
    int m_size;
};
    
class ITSCScreenCaptureDataCallback : public talk_base::RefCountInterface
{
public:
    virtual void onCapturedFrame(const TSCScreenCaptureProviderFrame& frame) = 0;
protected:
    virtual ~ITSCScreenCaptureDataCallback() {}
};
    
typedef talk_base::scoped_refptr<ITSCScreenCaptureDataCallback> ITSCScreenCaptureDataCallbackRef;

class TSCScreenCaptureProvider : public talk_base::RefCountInterface
{
public:
    TSCScreenCaptureProvider();
    virtual ~TSCScreenCaptureProvider();
    
    void start(const int frameRate, ITSCScreenCaptureDataCallback* recipient);
    void stop();
    std::string getId() const;
protected:
    
    virtual void onStart(const int frameRate, ITSCScreenCaptureDataCallback* recipient);
    virtual void onStop();
    
private:
    void startPriv(const int frameRate, ITSCScreenCaptureDataCallback* recipient);
    
    talk_base::AsyncInvoker m_invoker;
    talk_base::Thread m_thread;
    std::string m_id;
};
    
} // namespace twiliosdk

#endif // TSC_SCREEN_CAPTURE_PROVIDER_H
