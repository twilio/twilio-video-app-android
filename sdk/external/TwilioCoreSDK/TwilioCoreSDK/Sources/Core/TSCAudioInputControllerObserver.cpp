//
//  TSCAudioInputControllerObserver.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/29/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCAudioInputControllerObserver.h"
#include "TSCThreadMonitor.h"
#include "TSCLogger.h"

namespace twiliosdk {

TSCAudioInputControllerObserver::TSCAudioInputControllerObserver()
{
    m_thread.reset(new talk_base::Thread());
    m_thread->Start(new TSCThreadMonitor("TSCAudioInputControllerObserver", this));
}

TSCAudioInputControllerObserver::~TSCAudioInputControllerObserver()
{
    TS_CORE_LOG_DEBUG("TSCAudioInputControllerObserver::~TSCAudioInputControllerObserver()");
    m_thread->Stop();
}

#pragma mark-

void
TSCAudioInputControllerObserver::onStateChanged(bool muted)
{
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCAudioInputControllerObserver::onStateDidChange, this, muted));
}
    
#pragma mark-

void
TSCAudioInputControllerObserver::onStateDidChange(bool muted)
{
}
    
} // namespace twiliosdk
