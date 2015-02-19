//
//  TSCParticipantConnection.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 2/10/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_PARTICIPANT_CONNECTION_H
#define TSC_PARTICIPANT_CONNECTION_H

#include "TSCoreSDKTypes.h"
#include "TSCParticipant.h"
#include "TSCSIPCall.h"
#include "TSCPeerConnectionObserver.h"

namespace twiliosdk {

class TSCParticipantConnection : public TSCPeerConnectionObserverObject
{
public:
    TSCParticipantConnection(const TSCParticipant& participant, const TSCOptions& options, const TSCParticipantConnectionObserverRef& observer);
    virtual ~TSCParticipantConnection();
    
    const TSCParticipant getParticipant() const;
    TSCSIPCall::TSCSIPCallState getSignalingState();
    uint64 getCallId();
    
    bool isValid() const;
    
    virtual void start(TSCSIPCallContext* context) = 0;
    
    void stop();
    
    void reject();
    void ignore();
    void ringing();
    
    virtual void on_call_sdp_created(pjsua_call_id call_id,
                             pjmedia_sdp_session *sdp,
                             pj_pool_t *pool,
                             const pjmedia_sdp_session *rem_sdp);
    
    virtual void on_call_state(pjsua_call_id call_id, pjsip_event *e);
protected:
    
    // peer connection observer
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
    TSCParticipantConnection();
    TSCParticipantConnection(const TSCParticipantConnection&);
    TSCParticipantConnection& operator=(TSCParticipantConnection&);

protected:
    TSCPeerConnectionObjectRef m_peer_connection;
    TSCSIPCallObjectRef m_sip_call;
    TSCParticipant m_participant;
    TSCOptions m_options;
    TSCParticipantConnectionObserverRef m_observer;
    std::string m_remote_offer;
};

}  // namespace twiliosdk

#endif // TSC_PARTICIPANT_CONNECTION_H
