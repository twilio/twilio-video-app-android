//
//  TSCSessionMediaControllers.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/27/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_SESSION_MEDIA_CONTROLLER_H
#define TSC_SESSION_MEDIA_CONTROLLER_H

#include "TSCoreSDKTypes.h"

namespace twiliosdk {

class IAudioInputControllerObserverInterface;
class IAudioInputControllerInterface: public talk_base::RefCountInterface
{
public:
    virtual ~IAudioInputControllerInterface(){};
    
    virtual void setObserver(IAudioInputControllerObserverInterface* observer) = 0;
    
    virtual bool isMuted() const = 0;
    virtual void setMuted(bool muted) = 0;
    
    virtual bool isValid() const = 0;
};

class IAudioInputControllerObserverInterface: public talk_base::RefCountInterface
{
public:
    virtual ~IAudioInputControllerObserverInterface(){};

    virtual void onStateChanged(bool muted) = 0;
    
protected:
    virtual void onStateDidChange(bool muted) = 0;
};

#pragma mark-

class IVideoCaptureControllerObserverInterface;
class IVideoCaptureControllerInterface: public talk_base::RefCountInterface
{
public:
    virtual ~IVideoCaptureControllerInterface(){};
    
    virtual void setObserver(IVideoCaptureControllerObserverInterface* observer) = 0;
    
    virtual bool isPaused() const = 0;
    virtual void setPaused(bool paused) = 0;
    virtual void setVideoCaptureDevice(const std::string& deviceId) = 0;
    virtual const std::string getVideoCaptureDevice() const = 0;
    
    virtual bool isValid() const = 0;
};

class IVideoCaptureControllerObserverInterface: public talk_base::RefCountInterface
{
public:
    virtual ~IVideoCaptureControllerObserverInterface(){};
    
    virtual void onStateChanged(bool paused) = 0;
    
protected:
    virtual void onStateDidChange(bool paused) = 0;
};
    
}

#endif // TSC_SESSION_MEDIA_CONTROLLER_H
