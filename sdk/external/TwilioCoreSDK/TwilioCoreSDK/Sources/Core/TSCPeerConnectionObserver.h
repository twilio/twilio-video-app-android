//
//  TSCPeerConnectionObserver.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/12/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_PEER_CONNECTION_OBSERVER_H
#define TSC_PEER_CONNECTION_OBSERVER_H

#include "TSCoreSDKTypes.h"

#include "talk/base/asyncinvoker.h"
#include "talk/base/thread.h"
#include "talk/app/webrtc/mediastreaminterface.h"

namespace twiliosdk {

typedef talk_base::scoped_refptr<webrtc::MediaStreamInterface> MediaStreamInterfaceRef;
    
class TSCPeerConnectionObserver
{
public:
    TSCPeerConnectionObserver();
    virtual ~TSCPeerConnectionObserver();
    
    void onIceGatheringComplete(const TSCErrorObjectRef& error = nullptr);
    void onSetSessionLocalDescription(const TSCErrorObjectRef& error = nullptr);
    void onSetSessionRemoteDescription(const TSCErrorObjectRef& error = nullptr);
    void onPeerConnectionConnected(const TSCErrorObjectRef& error = nullptr);
    void onPeerConnectionDisconnected(TSCDisconnectReason reason);
    
    void onAddStream(webrtc::MediaStreamInterface* stream, TSCMediaStreamOrigin origin);
    void onRemoveStream(webrtc::MediaStreamInterface* stream, TSCMediaStreamOrigin origin);
    
    void onLinkAudioInputController(IAudioInputControllerInterface* controller);
    void onUnlinkAudioInputController(IAudioInputControllerInterface* controller);
    
    void onLinkVideoCaptureController(IVideoCaptureControllerInterface* controller);
    void onUnlinkVideoCaptureController(IVideoCaptureControllerInterface* controller);
    
protected:
    virtual void onDidIceGatheringComplete(const TSCErrorObjectRef& error);

    virtual void onDidSetSessionLocalDescription(const TSCErrorObjectRef& error);
    virtual void onDidSetSessionRemoteDescription(const TSCErrorObjectRef& error);
    virtual void onDidConnectPeerConnection(const TSCErrorObjectRef& error);
    virtual void onDidDisconnectPeerConnection(TSCDisconnectReason reason);
    
    virtual void onDidAddStream(const MediaStreamInterfaceRef& stream, TSCMediaStreamOrigin origin);
    virtual void onDidRemoveStream(const MediaStreamInterfaceRef& stream, TSCMediaStreamOrigin origin);

    virtual void onDidLinkAudioInputController(IAudioInputControllerInterface* controller);
    virtual void onDidUnlinkAudioInputController(IAudioInputControllerInterface* controller);
    
    virtual void onDidLinkVideoCaptureController(IVideoCaptureControllerInterface* controller);
    virtual void onDidUnlinkVideoCaptureController(IVideoCaptureControllerInterface* controller);

private:
    void onIceGatheringCompletePriv(TSCErrorObject* error);
    
    void onSetSessionLocalDescriptionPriv(TSCErrorObject* error);
    void onSetSessionRemoteDescriptionPriv(TSCErrorObject* error);
    void onPeerConnectionConnectedPriv(TSCErrorObject* error);
    void onPeerConnectionDisconnectedPriv(TSCDisconnectReason reason);
    
private:
    talk_base::AsyncInvoker m_invoker;
    talk_base::scoped_ptr<talk_base::Thread> m_thread;
};
    
}  // namespace twiliosdk

#endif  // TSC_PEER_CONNECTION_OBSERVER_H
