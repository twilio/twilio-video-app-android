//
//  TSCEndpointObserver.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 12/30/14.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#include "TSCEndpointObserver.h"
#include "TSCoreError.h"
#include "TSCIncomingSession.h"
#include "TSCLogger.h"
#include "TSCThreadMonitor.h"

namespace twiliosdk {

TSCEndpointObserver::TSCEndpointObserver()
{
    m_thread.reset(new talk_base::Thread());
    m_thread->Start(new TSCThreadMonitor("TSCEndpointObserver", this));
}

TSCEndpointObserver::~TSCEndpointObserver()
{
    TS_CORE_LOG_DEBUG("TSCEndpointObserver::~TSCEndpointObserver()");
    m_thread->Stop();
}

#pragma mark-

void
TSCEndpointObserver::onRegistrationComplete(const TSCErrorObjectRef& error)
{
    TSCErrorObject* object = error.get();
    if(object != nullptr)
       object->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCEndpointObserver::onRegistrationCompletePriv,
                                                           this, object));
}

void
TSCEndpointObserver::onRegistrationCompletePriv(TSCErrorObject* error)
{
    TSCErrorObjectRef errorHandler = error;
    if(error != nullptr)
       error->Release();
    onRegistrationDidComplete(errorHandler.get());
}

void
TSCEndpointObserver::onRegistrationDidComplete(TSCErrorObject* error)
{
}
    
#pragma mark-
    
void
TSCEndpointObserver::onUnregistrationComplete(const TSCErrorObjectRef& error)
{
    TSCErrorObject* object = error.get();
    if(object != nullptr)
        object->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCEndpointObserver::onUnregistrationCompletePriv,
                                                           this, object));
}

void
TSCEndpointObserver::onUnregistrationCompletePriv(TSCErrorObject* error)
{
    TSCErrorObjectRef errorHandler = error;
    if(error != nullptr)
        error->Release();
    onUnregistrationDidComplete(errorHandler.get());
}
    
void
TSCEndpointObserver::onUnregistrationDidComplete(TSCErrorObject* error)
{
}
    
#pragma mark-
    
void
TSCEndpointObserver::onStateChange(TSCEndpointState state)
{
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCEndpointObserver::onStateDidChange, this, state));
}

void
TSCEndpointObserver::onStateDidChange(TSCEndpointState state)
{
}

#pragma mark-

    
void
TSCEndpointObserver::onReceiveIncomingCall(const TSCIncomingSessionObjectRef& session)
{
    TSCIncomingSessionObject* object = session.get();
    object->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCEndpointObserver::onReceiveIncomingCallPriv, this, object));
}
    
void
TSCEndpointObserver::onReceiveIncomingCallPriv(TSCIncomingSessionObject* object)
{
    TSCIncomingSessionObjectRef sessionHolder = object;
    object->Release();
    onIncomingCallDidReceive(sessionHolder.get());
}
    
void
TSCEndpointObserver::onIncomingCallDidReceive(TSCIncomingSession* session)
{
}

    
} // namespace twiliosdk
