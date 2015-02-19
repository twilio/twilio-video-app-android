//
//  TSCVideoCaptureController.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/27/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCVideoCaptureController.h"
#include "TSCVideoCaptureControllerImpl.h"

namespace twiliosdk {

TSCVideoCaptureController::TSCVideoCaptureController()
{
}

TSCVideoCaptureController::~TSCVideoCaptureController()
{
}

#pragma mark-
    
void
TSCVideoCaptureController::setImpl(IVideoCaptureControllerInterface* impl)
{
    talk_base::CritScope cs(&m_lock);
    m_impl = impl;
    if(m_observer.get() != nullptr && m_impl.get() != nullptr)
       m_impl->setObserver(m_observer.get());
}

#pragma mark-
    
void
TSCVideoCaptureController::setObserver(IVideoCaptureControllerObserverInterface* observer)
{
    talk_base::CritScope cs(&m_lock);
    m_observer = observer;
    if(m_impl.get() != nullptr)
       m_impl->setObserver(observer);
}

#pragma mark-
    
bool
TSCVideoCaptureController::isPaused() const
{
    talk_base::CritScope cs(&m_lock);
    if(m_impl.get() != nullptr)
       return m_impl->isPaused();
    return false;
}
    
void
TSCVideoCaptureController::setPaused(bool paused)
{
    talk_base::CritScope cs(&m_lock);
    if(m_impl.get() != nullptr)
       m_impl->setPaused(paused);
}
    
#pragma mark-
    
void
TSCVideoCaptureController::setVideoCaptureDevice(const std::string& deviceId)
{
    talk_base::CritScope cs(&m_lock);
    if(m_impl.get() != nullptr)
       m_impl->setVideoCaptureDevice(deviceId);
}
    
const std::string
TSCVideoCaptureController::getVideoCaptureDevice() const
{
    talk_base::CritScope cs(&m_lock);
    if(m_impl.get() != nullptr)
        return m_impl->getVideoCaptureDevice();
    return std::string("");
}

#pragma mark-
    
bool
TSCVideoCaptureController::isValid() const
{
    if(m_impl.get() != nullptr)
       return m_impl->isValid();
    return false;
}
    
}