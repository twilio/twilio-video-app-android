//
//  TSCAudioInputControllerObserver.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/29/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCVideoCaptureControllerObserver.h"
#include "TSCThreadMonitor.h"
#include "TSCLogger.h"

namespace twiliosdk {

TSCVideoCaptureControllerObserver::TSCVideoCaptureControllerObserver()
{
    m_thread.reset(new talk_base::Thread());
    m_thread->Start(new TSCThreadMonitor("TSCVideoCaptureControllerObserver", this));
}

TSCVideoCaptureControllerObserver::~TSCVideoCaptureControllerObserver()
{
    TS_CORE_LOG_DEBUG("TSCVideoCaptureControllerObserver::~TSCVideoCaptureControllerObserver()");
    m_thread->Stop();
}

#pragma mark-

void
TSCVideoCaptureControllerObserver::onStateChanged(bool paused)
{
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCVideoCaptureControllerObserver::onStateDidChange, this, paused));
}
    
#pragma mark-

void
TSCVideoCaptureControllerObserver::onStateDidChange(bool paused)
{
}
    
} // namespace twiliosdk
