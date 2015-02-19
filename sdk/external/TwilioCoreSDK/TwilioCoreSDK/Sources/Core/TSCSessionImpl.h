//
//  TSCSession.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_SESSION_IMPL_H
#define TSC_SESSION_IMPL_H

// TODO: remove pjsip dependency from TSCPJSUA interface
#include <pjsua-lib/pjsua.h>
#include <pjsua-lib/pjsua_internal.h>
#include <pj/types.h>

#include "TSCoreSDKTypes.h"
#include "TSCParticipantConnection.h"
#include "TSCParticipantConnectionObserver.h"

#include "TSCSession.h"
#include "TSCVideoSurface.h"

namespace twiliosdk {
    
class TSCSessionImpl : public TSCParticipantConnectionObserverObject
{
public:
    TSCSessionImpl(int accountId,
               const TSCOptions& options,
               const TSCSessionObserverObjectRef& observer);
    
    virtual ~TSCSessionImpl();
    
    uint64 getId() const;
    
    void setSessionObserver(const TSCSessionObserverObjectRef& observer);

    void setParticipants(const std::vector<TSCParticipant>& participants);
    const std::vector<TSCParticipant> getParticipants() const;

    void setVideoSurface(const TSCVideoSurfaceObjectRef& videoSurface);
    
    virtual void start();
    virtual void stop();
    virtual void reject();
    virtual void ignore();
    virtual void ringing();

    // call session callbacks
    virtual void on_call_state(pjsua_call_id call_id, pjsip_event *e);
    virtual void on_call_sdp_created(pjsua_call_id call_id,
                             pjmedia_sdp_session *sdp,
                             pj_pool_t *pool,
                             const pjmedia_sdp_session *rem_sdp);
    virtual void on_call_rx_offer(pjsua_call_id call_id,
                          const pjmedia_sdp_session *offer,
                          void *reserved,
                          pjsip_status_code *code,
                          pjsua_call_setting *opt);
    virtual void on_call_tsx_state(pjsua_call_id call_id,
                           pjsip_transaction *tsx,
                           pjsip_event *e);
    
    // media input controllers
    IAudioInputControllerInterface* getAudioInputController() const;
    IVideoCaptureControllerInterface* getVideoCaptureController() const;
    
    virtual void onDidIceGatheringComplete(const TSCParticipantConnectionRef& connection, const TSCErrorObjectRef& error);
    
    virtual void onDidSetSessionLocalDescription(const TSCParticipantConnectionRef& connection, const TSCErrorObjectRef& error);
    virtual void onDidSetSessionRemoteDescription(const TSCParticipantConnectionRef& connection, const TSCErrorObjectRef& error);
    virtual void onDidConnectPeerConnection(const TSCParticipantConnectionRef& connection, const TSCErrorObjectRef& error);
    virtual void onDidDisconnectPeerConnection(const TSCParticipantConnectionRef& connection, TSCDisconnectReason reason);
    
    virtual void onDidAddStream(const TSCParticipantConnectionRef& connection, const MediaStreamInterfaceRef& stream, TSCMediaStreamOrigin origin);
    virtual void onDidRemoveStream(const TSCParticipantConnectionRef& connection, const MediaStreamInterfaceRef& stream, TSCMediaStreamOrigin origin);
    
    virtual void onDidLinkAudioInputController(IAudioInputControllerInterface* controller);
    virtual void onDidUnlinkAudioInputController(IAudioInputControllerInterface* controller);
    
    virtual void onDidLinkVideoCaptureController(IVideoCaptureControllerInterface* controller);
    virtual void onDidUnlinkVideoCaptureController(IVideoCaptureControllerInterface* controller);

protected:
    void processParticipantConnect(const TSCParticipantConnectionRef& participant, const TSCErrorObjectRef& error);
    void changeState(const TSCSessionState state);

protected:
    
    TSCSessionState m_state;
    int m_account_id;
    TSCOptions m_options;
    TSCSessionObserverObjectRef m_observer;
   
    std::vector<TSCParticipantConnectionRef> m_participant_connections;
    TSCVideoSurfaceObjectRef m_video_surface;
    
    TSCAudioInputControllerRef m_audio_input_controller;
    TSCVideoCaptureControllerRef m_video_capture_controller;
};
    
}  // namespace twiliosdk

#endif  // TSC_SESSION_IMPL_H
