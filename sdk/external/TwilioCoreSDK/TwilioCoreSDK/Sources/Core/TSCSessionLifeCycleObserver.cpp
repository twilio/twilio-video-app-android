//
//  TSCSessionLifeCycleObserver.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/19/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCSessionLifeCycleObserver.h"
#include "TSCEndpoint.h"
#include "TSCSession.h"
#include "TSCThreadMonitor.h"
#include "TSCThreadManager.h"
#include "TSCLogger.h"

namespace twiliosdk {

TSCSessionLifeCycleObserver::TSCSessionLifeCycleObserver()
{
    m_thread.reset(new talk_base::Thread());
    m_thread->Start(new TSCThreadMonitor("TSCSessionLifeCycleObserver", this));
}

TSCSessionLifeCycleObserver::~TSCSessionLifeCycleObserver()
{
    TS_CORE_LOG_DEBUG("TSCSessionLifeCycleObserver::~TSCSessionLifeCycleObserver()");
    TSCThreadManager::destroyThread(m_thread.release());
}

#pragma mark-
    
void
TSCSessionLifeCycleObserver::setDelegate(TSCEndpoint* delegate)
{
    m_delegate = delegate;
}

#pragma mark-
  
void
TSCSessionLifeCycleObserver::onSessionStarted(TSCSession* session)
{
    session->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(),
                                talk_base::Bind(&TSCSessionLifeCycleObserver::onSessionStartedPriv,
                                                this, session));
}

void
TSCSessionLifeCycleObserver::onSessionStartedPriv(TSCSession* session)
{
    talk_base::scoped_refptr<TSCSession> sessionHolder = session;
    session->Release();
    onSessionDidStart(sessionHolder);
}

void
TSCSessionLifeCycleObserver::onSessionDidStart(TSCSession* session)
{
    if(m_delegate)
       m_delegate->onSessionDidStart(session);
}
    
#pragma mark-

void
TSCSessionLifeCycleObserver::onSessionStoped(TSCSession* session)
{
    session->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(),
                                talk_base::Bind(&TSCSessionLifeCycleObserver::onSessionStoppedPriv,
                                                this, session));
}

void
TSCSessionLifeCycleObserver::onSessionStoppedPriv(TSCSession* session)
{
    talk_base::scoped_refptr<TSCSession> sessionHolder = session;
    session->Release();
    onSessionDidStop(sessionHolder);
}
    
void
TSCSessionLifeCycleObserver::onSessionDidStop(TSCSession* session)
{
    if(m_delegate)
       m_delegate->onSessionDidStop(session);
}
    
} // namespace twiliosdk
