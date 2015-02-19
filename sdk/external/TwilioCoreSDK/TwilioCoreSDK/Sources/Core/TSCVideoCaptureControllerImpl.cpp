//
//  TSCVideoCaptureControllerImpl.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/27/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCVideoCaptureControllerImpl.h"
#include "TSCVideoCapturer.h"

namespace twiliosdk {

TSCVideoCaptureControllerImpl::TSCVideoCaptureControllerImpl(webrtc::VideoTrackInterface* videoTrack,
                                                             TSCVideoCapturer* videoCapturer)
{
    m_video_track = videoTrack;
    m_video_capturer = videoCapturer;
    m_paused = isPausedPriv();
}

TSCVideoCaptureControllerImpl::~TSCVideoCaptureControllerImpl()
{
    m_video_capturer = nullptr;
    m_video_track = nullptr;
    m_observer = nullptr;
}

#pragma mark-

void
TSCVideoCaptureControllerImpl::setObserver(IVideoCaptureControllerObserverInterface* observer)
{
    m_observer = observer;
}

#pragma mark-
    
bool
TSCVideoCaptureControllerImpl::isPaused() const
{
    return m_paused;
}

bool
TSCVideoCaptureControllerImpl::isPausedPriv() const
{
    if(m_video_track.get() != nullptr && m_video_capturer != nullptr)
        return !m_video_capturer->IsRunning();
    return false;
}
 
#pragma mark-
    
void
TSCVideoCaptureControllerImpl::setPaused(bool paused)
{
    if(m_video_track.get() != nullptr && m_video_capturer != nullptr)
    {
        if(paused != m_paused)
        {
            m_video_track->set_enabled(!paused);
            m_video_capturer->Pause(paused);
            setState(paused);
        }
    }
}
    
#pragma mark-
    
void
TSCVideoCaptureControllerImpl::setVideoCaptureDevice(const std::string& deviceId)
{
    if (m_video_capturer != nullptr) {
        m_video_capturer->setCapturingDevice(deviceId);
    }
}
    
const std::string
TSCVideoCaptureControllerImpl::getVideoCaptureDevice() const
{
    if(m_video_capturer != nullptr)
        return m_video_capturer->getCapturingDevice();
    return std::string("");
}
    
#pragma mark-
    
bool
TSCVideoCaptureControllerImpl::isValid() const
{
    return (m_video_track.get() != nullptr);
}

#pragma mark-
    
void
TSCVideoCaptureControllerImpl::setState(bool paused)
{
    if(m_paused != paused)
    {
        m_paused = paused;
        if(m_observer.get() != nullptr)
            m_observer->onStateChanged(m_paused);
    }
}
    
}