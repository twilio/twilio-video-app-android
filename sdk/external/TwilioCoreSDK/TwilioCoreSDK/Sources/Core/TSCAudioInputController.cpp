//
//  TSCAudioInputController.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/27/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCAudioInputController.h"
#include "TSCAudioInputControllerImpl.h"

namespace twiliosdk {

TSCAudioInputController::TSCAudioInputController()
{
}

TSCAudioInputController::~TSCAudioInputController()
{
}

#pragma mark-
    
void
TSCAudioInputController::setImpl(IAudioInputControllerInterface* impl)
{
    talk_base::CritScope cs(&m_lock);
    m_impl = impl;
    if(m_observer.get() != nullptr && m_impl.get() != nullptr)
       m_impl->setObserver(m_observer.get());
}

#pragma mark-

void
TSCAudioInputController::setObserver(IAudioInputControllerObserverInterface* observer)
{
    talk_base::CritScope cs(&m_lock);
    m_observer = observer;
    if(m_impl.get() != nullptr)
       m_impl->setObserver(observer);
}
    
#pragma mark-
    
bool
TSCAudioInputController::isMuted() const
{
    talk_base::CritScope cs(&m_lock);
    if(m_impl.get() != nullptr)
       return m_impl->isMuted();
    return false;
}
    
void
TSCAudioInputController::setMuted(bool muted)
{
    talk_base::CritScope cs(&m_lock);
    if(m_impl.get() != nullptr)
       m_impl->setMuted(muted);
}

#pragma mark-
    
bool
TSCAudioInputController::isValid() const
{
    talk_base::CritScope cs(&m_lock);
    if(m_impl.get() != nullptr)
       return m_impl->isValid();
    return false;
}
    
}