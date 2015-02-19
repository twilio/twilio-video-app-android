//
//  TSCAudioInputControllerImpl.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/27/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCAudioInputControllerImpl.h"
#include "TSCAudioInputControllerObserver.h"

namespace twiliosdk {

TSCAudioInputControllerImpl::TSCAudioInputControllerImpl(webrtc::AudioTrackInterface* audioTrack)
{
    m_audio_track = audioTrack;
    m_muted = isMutedPriv();
}

TSCAudioInputControllerImpl::~TSCAudioInputControllerImpl()
{
}

#pragma mark-
    
void
TSCAudioInputControllerImpl::setObserver(IAudioInputControllerObserverInterface* observer)
{
    m_observer = observer;
}

#pragma mark-
    
bool
TSCAudioInputControllerImpl::isMuted() const
{
    return m_muted;
}
    
void
TSCAudioInputControllerImpl::setMuted(bool muted)
{
    if(m_audio_track.get() != nullptr)
    {
        if(muted != m_muted)
        {
            m_audio_track->set_enabled(!muted);
            setState(muted);
        }
    }
}
    
#pragma mark-
    
bool
TSCAudioInputControllerImpl::isValid() const
{
    return (m_audio_track.get() != nullptr);
}

#pragma mark-

bool
TSCAudioInputControllerImpl::isMutedPriv() const
{
    if(m_audio_track.get() != nullptr)
        return !m_audio_track->enabled();
    return false;
}
    
#pragma mark-
    
void
TSCAudioInputControllerImpl::setState(bool muted)
{
    if(m_muted != muted)
    {
        m_muted = muted;
        if(m_observer.get() != nullptr)
           m_observer->onStateChanged(m_muted);
    }
}
    
}