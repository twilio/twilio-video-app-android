//
//  TSCPeerConnectionObserver.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/12/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCPeerConnectionObserver.h"
#include "TSCoreError.h"
#include "TSCEvent.h"
#include "TSCLogger.h"
#include "TSCThreadMonitor.h"
#include "TSCThreadManager.h"

namespace twiliosdk {
    
TSCPeerConnectionObserver::TSCPeerConnectionObserver()
{
    m_thread.reset(new talk_base::Thread());
    m_thread->Start(new TSCThreadMonitor("TSCPeerConnectionObserver", this));
}

TSCPeerConnectionObserver::~TSCPeerConnectionObserver()
{
    TS_CORE_LOG_DEBUG("TSCPeerConnectionObserver::~TSCPeerConnectionObserver()");
    TSCThreadManager::destroyThread(m_thread.release());
}

#pragma mark-

void
TSCPeerConnectionObserver::onIceGatheringComplete(const TSCErrorObjectRef& error)
{
    TSCErrorObject* errorObject = error.get();
    if(errorObject != nullptr)
       errorObject->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCPeerConnectionObserver::onIceGatheringCompletePriv,
                                                           this, errorObject));
}

#pragma mark-

void
TSCPeerConnectionObserver::onIceGatheringCompletePriv(TSCErrorObject* error)
{
    TSCErrorObjectRef errorHolder = error;
    onDidIceGatheringComplete(errorHolder);
}

#pragma mark-
    
void
TSCPeerConnectionObserver::onDidIceGatheringComplete(const TSCErrorObjectRef& error)
{
}

#pragma mark-

void
TSCPeerConnectionObserver::onSetSessionLocalDescription(const TSCErrorObjectRef& error)
{
    TSCErrorObject* errorObject = error.get();
    if(errorObject != nullptr)
        errorObject->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCPeerConnectionObserver::onSetSessionLocalDescriptionPriv, this, errorObject));
}
    
void
TSCPeerConnectionObserver::onSetSessionLocalDescriptionPriv(TSCErrorObject* error)
{
    TSCErrorObjectRef errorHolder = error;
    onDidSetSessionLocalDescription(errorHolder);
}

void
TSCPeerConnectionObserver::onDidSetSessionLocalDescription(const TSCErrorObjectRef& error)
{
}
    
#pragma mark-
    
void
TSCPeerConnectionObserver::onSetSessionRemoteDescription(const TSCErrorObjectRef& error)
{
    TSCErrorObject* errorObject = error.get();
    if(errorObject != nullptr)
        errorObject->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCPeerConnectionObserver::onSetSessionRemoteDescriptionPriv, this, errorObject));
}

void
TSCPeerConnectionObserver::onSetSessionRemoteDescriptionPriv(TSCErrorObject* error)
{
    TSCErrorObjectRef errorHolder = error;
    onDidSetSessionRemoteDescription(errorHolder);
}

void
TSCPeerConnectionObserver::onDidSetSessionRemoteDescription(const TSCErrorObjectRef& error)
{
}

#pragma mark-
    
void
TSCPeerConnectionObserver::onPeerConnectionConnected(const TSCErrorObjectRef& error)
{
    TSCErrorObject* errorObject = error.get();
    if(errorObject != nullptr)
        errorObject->AddRef();
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCPeerConnectionObserver::onPeerConnectionConnectedPriv,
                                                           this, errorObject));
}

void
TSCPeerConnectionObserver::onPeerConnectionConnectedPriv(TSCErrorObject* error)
{
    TSCErrorObjectRef errorHolder = error;
    onDidConnectPeerConnection(errorHolder);
}
    
void
TSCPeerConnectionObserver::onDidConnectPeerConnection(const TSCErrorObjectRef& error)
{
}

#pragma mark-
    
void
TSCPeerConnectionObserver::onPeerConnectionDisconnected(TSCDisconnectReason reason)
{
    m_invoker.AsyncInvoke<void>(m_thread.get(), talk_base::Bind(&TSCPeerConnectionObserver::onPeerConnectionDisconnectedPriv,this, reason));
}

void
TSCPeerConnectionObserver::onPeerConnectionDisconnectedPriv(TSCDisconnectReason reason)
{
    onDidDisconnectPeerConnection(reason);
}
    
void
TSCPeerConnectionObserver::onDidDisconnectPeerConnection(TSCDisconnectReason reason)
{
}

#pragma mark-
    
void
TSCPeerConnectionObserver::onAddStream(webrtc::MediaStreamInterface* stream, TSCMediaStreamOrigin origin)
{
    onDidAddStream(stream, origin);
}

void
TSCPeerConnectionObserver::onDidAddStream(const MediaStreamInterfaceRef& stream, TSCMediaStreamOrigin origin)
{
}

#pragma mark-
    
void
TSCPeerConnectionObserver::onRemoveStream(webrtc::MediaStreamInterface* stream, TSCMediaStreamOrigin origin)
{
    onDidRemoveStream(stream, origin);
}

void
TSCPeerConnectionObserver::onDidRemoveStream(const MediaStreamInterfaceRef& stream, TSCMediaStreamOrigin origin)
{
}

#pragma mark-

void
TSCPeerConnectionObserver::onLinkVideoCaptureController(IVideoCaptureControllerInterface* controller)
{
    onDidLinkVideoCaptureController(controller);
}

void
TSCPeerConnectionObserver::onDidLinkVideoCaptureController(IVideoCaptureControllerInterface* controller)
{
}
    
#pragma mark-
    
void
TSCPeerConnectionObserver::onUnlinkVideoCaptureController(IVideoCaptureControllerInterface* controller)
{
    onDidUnlinkVideoCaptureController(controller);
}

void
TSCPeerConnectionObserver::onDidUnlinkVideoCaptureController(IVideoCaptureControllerInterface* controller)
{
}

#pragma mark-
    
void
TSCPeerConnectionObserver::onLinkAudioInputController(IAudioInputControllerInterface* controller)
{
    onDidLinkAudioInputController(controller);
}

void
TSCPeerConnectionObserver::onDidLinkAudioInputController(IAudioInputControllerInterface* controller)
{
}

#pragma mark-
    
void
TSCPeerConnectionObserver::onUnlinkAudioInputController(IAudioInputControllerInterface* controller)
{
    onDidUnlinkAudioInputController(controller);
}

void
TSCPeerConnectionObserver::onDidUnlinkAudioInputController(IAudioInputControllerInterface* controller)
{
}
    
} // namespace twiliosdk
