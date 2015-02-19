//
//  TSCVideoCaptureControllerObserver.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/29/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_VIDEO_CAPTURE_CONTROLLER_OBSERVER_H
#define TSC_VIDEO_CAPTURE_CONTROLLER_OBSERVER_H

#include "TSCoreSDKTypes.h"
#include "TSCSessionMediaControllers.h"

#include "talk/base/asyncinvoker.h"
#include "talk/base/thread.h"

namespace twiliosdk {

class TSCVideoCaptureControllerObserver: public IVideoCaptureControllerObserverInterface
{
public:
    TSCVideoCaptureControllerObserver();
    virtual ~TSCVideoCaptureControllerObserver();
    
    void onStateChanged(bool paused);
    
protected:
    virtual void onStateDidChange(bool paused);
    
private:
    talk_base::AsyncInvoker m_invoker;
    talk_base::scoped_ptr<talk_base::Thread> m_thread;
};
    
}  // namespace twiliosdk

#endif  // TSC_VIDEO_CAPTURE_CONTROLLER_OBSERVER_H
