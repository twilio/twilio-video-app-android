//
//  TSCSessionObserver.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCSessionObserver.h"
#include "TSCoreError.h"
#include "TSCEvent.h"
#include "TSCParticipant.h"
#include "TSCMediaStreamInfo.h"
#include "TSCThreadMonitor.h"
#include "TSCLogger.h"
#include "TSCThreadManager.h"

namespace twiliosdk {

TSCSessionObserver::TSCSessionObserver()
{
    m_thread.reset(new talk_base::Thread());
    m_thread->Start(new TSCThreadMonitor("TSCSessionObserver", this));
}

TSCSessionObserver::~TSCSessionObserver()
{
    TS_CORE_LOG_DEBUG("TSCSessionObserver::~TSCSessionObserver()");
    TSCThreadManager::destroyThread(m_thread.release());
}

#pragma mark-

void
TSCSessionObserver::onReceiveEvent(const TSCEventObjectRef& event)
{
    TSCEventObject* eventObject = event.get();
    if(eventObject != nullptr)
       eventObject->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCSessionObserver::onReceiveEventPriv, this, eventObject));
}

void
TSCSessionObserver::onReceiveEventPriv(TSCEventObject* event)
{
    TSCEventObjectRef eventHolder = event;
    if(event != nullptr)
       event->Release();
    onDidReceiveEvent(eventHolder);
}
    
void
TSCSessionObserver::onDidReceiveEvent(const TSCEventObjectRef& event)
{
}

#pragma mark-
    
void
TSCSessionObserver::onStateChange(TSCSessionState state)
{
     m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCSessionObserver::onStateDidChange, this, state));
}

void
TSCSessionObserver::onStateDidChange(TSCSessionState state)
{
    
}
    
#pragma mark-

void
TSCSessionObserver::onStartComplete(const TSCErrorObjectRef& error)
{
    TSCErrorObject* errorObject = error.get();
    if(errorObject != nullptr)
       errorObject->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCSessionObserver::onStartCompletePriv, this, errorObject));
}

void
TSCSessionObserver::onStartCompletePriv(TSCErrorObject* error)
{
    TSCErrorObjectRef errorHolder = error;
    if(error != nullptr)
       error->Release();
    onStartDidComplete(errorHolder);
}

void
TSCSessionObserver::onStartDidComplete(const TSCErrorObjectRef& error)
{
}
    
#pragma mark-
    
void
TSCSessionObserver::onStopComplete(const TSCErrorObjectRef& error)
{
    TSCErrorObject* errorObject = error.get();
    if(errorObject != nullptr)
        errorObject->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCSessionObserver::onStopCompletePriv, this, errorObject));
}
    
void
TSCSessionObserver::onStopCompletePriv(TSCErrorObject* error)
{
    TSCErrorObjectRef errorHolder = error;
    if(error != nullptr)
       error->Release();
    onStopDidComplete(errorHolder);
}

void
TSCSessionObserver::onStopDidComplete(const TSCErrorObjectRef& error)
{
}
    
#pragma mark-
    
void
TSCSessionObserver::onParticipantConnect(const TSCParticipantObjectRef& participant,
                                         const TSCErrorObjectRef& error)
{
    TSCErrorObject* errorObject = error.get();
    if(errorObject != nullptr)
       errorObject->AddRef();

    TSCParticipantObject* participantObject = participant.get();
    if(participantObject != nullptr)
       participantObject->AddRef();
    
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCSessionObserver::onParticipantConnectPriv,
                                                           this, participantObject, errorObject));
}

void
TSCSessionObserver::onParticipantConnectPriv(TSCParticipantObject* participant,
                                             TSCErrorObject* error)
{
    TSCParticipantObjectRef participantHolder = participant;
    if(participant != nullptr)
       participant->Release();
    
    TSCErrorObjectRef errorHolder = error;
    if(error != nullptr)
       error->Release();

    onParticipantDidConnect(participantHolder, errorHolder);
}

void
TSCSessionObserver::onParticipantDidConnect(const TSCParticipantObjectRef& participant,
                                            const TSCErrorObjectRef& error)
{
}

#pragma mark-
    
void
TSCSessionObserver::onParticipantDisconect(const TSCParticipantObjectRef& participant,
                                           TSCDisconnectReason reason)
{
    TSCParticipantObject* participantObject = participant.get();
    if(participantObject != nullptr)
       participantObject->AddRef();
    
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCSessionObserver::onParticipantDisconectPriv,
                                                           this, participantObject, reason));
}

void
TSCSessionObserver::onParticipantDisconectPriv(TSCParticipantObject* participant,
                                               TSCDisconnectReason reason)
{
    TSCParticipantObjectRef participantHolder = participant;
    if(participant != nullptr)
       participant->Release();
    onParticipantDidDisconect(participantHolder, reason);
}

void
TSCSessionObserver::onParticipantDidDisconect(const TSCParticipantObjectRef& participant,
                                              TSCDisconnectReason reason)
{
}

#pragma mark-

void
TSCSessionObserver::onMediaStreamAdd(TSCMediaStreamInfoObject* stream)
{
    stream->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCSessionObserver::onMediaStreamAddPriv,
                                                           this, stream));
}

void
TSCSessionObserver::onMediaStreamAddPriv(TSCMediaStreamInfoObject* stream)
{
    TSCMediaStreamInfoObjectRef streamHolder = stream;
    stream->Release();
    onMediaStreamDidAdd(streamHolder.get());
}

void
TSCSessionObserver::onMediaStreamDidAdd(TSCMediaStreamInfoObject* stream)
{
}
    
#pragma mark-
    
void
TSCSessionObserver::onMediaStreamRemove(TSCMediaStreamInfoObject* stream)
{
    stream->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCSessionObserver::onMediaStreamRemovePriv,
                                                           this, stream));
}

void
TSCSessionObserver::onMediaStreamRemovePriv(TSCMediaStreamInfoObject* stream)
{
    TSCMediaStreamInfoObjectRef streamHolder = stream;
    stream->Release();
    onMediaStreamDidRemove(streamHolder.get());
}

void
TSCSessionObserver::onMediaStreamDidRemove(TSCMediaStreamInfoObject* stream)
{
}
    
} // namespace twiliosdk
