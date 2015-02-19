//
//  TSCParticipantConnection.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 2/10/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_PARTICIPANT_CONNECTION_OBSERVER_H
#define TSC_PARTICIPANT_CONNECTION_OBSERVER_H

#include "TSCoreSDKTypes.h"

namespace twiliosdk {
    
class TSCParticipantConnectionObserver : public talk_base::RefCountInterface
{
public:
    TSCParticipantConnectionObserver() {};
    virtual ~TSCParticipantConnectionObserver() {};
    
    virtual void onDidIceGatheringComplete(const TSCParticipantConnectionRef& connection, const TSCErrorObjectRef& error) = 0;
    
    virtual void onDidSetSessionLocalDescription(const TSCParticipantConnectionRef& connection, const TSCErrorObjectRef& error) = 0;
    virtual void onDidSetSessionRemoteDescription(const TSCParticipantConnectionRef& connection, const TSCErrorObjectRef& error) = 0;
    virtual void onDidConnectPeerConnection(const TSCParticipantConnectionRef& connection, const TSCErrorObjectRef& error) = 0;
    virtual void onDidDisconnectPeerConnection(const TSCParticipantConnectionRef& connection, TSCDisconnectReason reason) = 0;
    
    virtual void onDidAddStream(const TSCParticipantConnectionRef& connection, const MediaStreamInterfaceRef& stream, TSCMediaStreamOrigin origin) = 0;
    virtual void onDidRemoveStream(const TSCParticipantConnectionRef& connection, const MediaStreamInterfaceRef& stream, TSCMediaStreamOrigin origin) = 0;
    
    virtual void onDidLinkAudioInputController(IAudioInputControllerInterface* controller) = 0;
    virtual void onDidUnlinkAudioInputController(IAudioInputControllerInterface* controller) = 0;
    
    virtual void onDidLinkVideoCaptureController(IVideoCaptureControllerInterface* controller) = 0;
    virtual void onDidUnlinkVideoCaptureController(IVideoCaptureControllerInterface* controller) = 0;
};
    
}  // namespace twiliosdk

#endif // TSC_PARTICIPANT_CONNECTION_OBSERVER_H
