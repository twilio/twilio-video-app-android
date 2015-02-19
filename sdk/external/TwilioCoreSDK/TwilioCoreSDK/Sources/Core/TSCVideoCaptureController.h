//
//  TSCVideoCaptureController.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/27/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_VIDEO_CAPTURE_CONTROLLER_H
#define TSC_VIDEO_CAPTURE_CONTROLLER_H

#include "TSCoreSDKTypes.h"
#include "TSCSessionMediaControllers.h"

namespace twiliosdk {

class TSCVideoCaptureControllerImpl;
class TSCVideoCaptureController: public IVideoCaptureControllerInterface
{
public:
    TSCVideoCaptureController();
    virtual ~TSCVideoCaptureController();
    
    void setImpl(IVideoCaptureControllerInterface* impl);
    
    virtual void setObserver(IVideoCaptureControllerObserverInterface* observer);
    
    virtual bool isPaused() const;
    virtual void setPaused(bool paused);
    virtual void setVideoCaptureDevice(const std::string& deviceId);
    virtual const std::string getVideoCaptureDevice() const;
    
    virtual bool isValid() const;
    
private:
    TSCVideoCaptureController(const TSCVideoCaptureController&);
    TSCVideoCaptureController& operator=(TSCVideoCaptureController&);
    
    talk_base::scoped_refptr<IVideoCaptureControllerInterface> m_impl;
    talk_base::scoped_refptr<IVideoCaptureControllerObserverInterface> m_observer;
    
    mutable talk_base::CriticalSection m_lock;
};
    
}

#endif // TSC_VIDEO_CAPTURE_CONTROLLER_H
