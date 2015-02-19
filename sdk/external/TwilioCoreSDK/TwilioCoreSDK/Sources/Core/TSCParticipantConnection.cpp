//
//  TSCParticipantConnection.cpp
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 2/10/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCParticipantConnection.h"
#include "TSCLogger.h"
#include "TSCoreError.h"

#include "TSCParticipantConnectionObserver.h"
#include "TSCPeerConnection.h"
#include "TSCPeerConnectionObserver.h"
#include "TSCSIPCallContext.h"

namespace twiliosdk {
    
TSCParticipantConnection::TSCParticipantConnection(const TSCParticipant& participant,
                                                   const TSCOptions& options,
                                                   const TSCParticipantConnectionObserverRef& observer) :
    m_participant(participant)
{
    m_options = options;
    m_observer = observer;
}

TSCParticipantConnection::~TSCParticipantConnection()
{
    
}
    
const TSCParticipant
TSCParticipantConnection::getParticipant() const
{
    return m_participant;
}
    
TSCSIPCall::TSCSIPCallState
TSCParticipantConnection::getSignalingState()
{
    TSCSIPCall::TSCSIPCallState state(TSCSIPCall::kTSCSIPCallStateInitial);
    if (m_sip_call.get() != nullptr) {
        state = m_sip_call->getCallState();
    }
    return state;
}

uint64
TSCParticipantConnection::getCallId()
{
    if (m_sip_call.get() != nullptr) {
        return m_sip_call->getId();
    }
    return kTSCInvalidId;
}
    
bool
TSCParticipantConnection::isValid() const
{
    return m_peer_connection.get() != nullptr && m_sip_call.get() != nullptr;
}

#pragma mark-
    
void
TSCParticipantConnection::stop()
{
    m_sip_call = nullptr;
    if(m_peer_connection.get() != nullptr) {
        TS_CORE_LOG_DEBUG("Disconnect participant %s", m_participant.getAddress().c_str());
        m_peer_connection->close();
        m_peer_connection = nullptr;
    }
}
    
void
TSCParticipantConnection::reject()
{
    if (m_sip_call.get() != nullptr) {
        m_sip_call->reject();
    }
}

void
TSCParticipantConnection::ignore()
{
    if (m_sip_call.get() != nullptr) {
        m_sip_call->ignore();
    }
}

void
TSCParticipantConnection::ringing()
{
    if (m_sip_call.get() != nullptr) {
        m_sip_call->answer(PJSIP_SC_RINGING);
    }
}
    
#pragma mark-
    
void
TSCParticipantConnection::on_call_sdp_created(pjsua_call_id call_id,
                                              pjmedia_sdp_session *sdp,
                                              pj_pool_t *pool,
                                              const pjmedia_sdp_session *rem_sdp)
{
    if (m_sip_call.get() != nullptr && sdp != nullptr) {
        std::string local_offer = m_peer_connection->getLocalDescription();
        m_sip_call->onLocalSDPCreated(call_id, sdp, pool, local_offer);
    }
    
    if (m_sip_call.get() != nullptr && rem_sdp != nullptr) {
        m_sip_call->onRemoteSDPCreated(call_id, rem_sdp, pool, m_remote_offer);
    }
}
    
void
TSCParticipantConnection::on_call_state(pjsua_call_id call_id, pjsip_event *e)
{
    
}
    
#pragma mark-
    
void
TSCParticipantConnection::onDidIceGatheringComplete(const TSCErrorObjectRef& error)
{
    if (m_observer.get() != nullptr) {
        m_observer->onDidIceGatheringComplete(this, error);
    }
}

void
TSCParticipantConnection::onDidSetSessionLocalDescription(const TSCErrorObjectRef& error)
{
    if (m_observer.get() != nullptr) {
        m_observer->onDidSetSessionLocalDescription(this, error);
    }
}

void
TSCParticipantConnection::onDidSetSessionRemoteDescription(const TSCErrorObjectRef& error)
{
    if (m_observer.get() != nullptr) {
        m_observer->onDidSetSessionRemoteDescription(this, error);
    }
}

void
TSCParticipantConnection::onDidConnectPeerConnection(const TSCErrorObjectRef& error)
{
    if (m_observer.get() != nullptr) {
        m_observer->onDidConnectPeerConnection(this, error);
    }
}

void
TSCParticipantConnection::onDidDisconnectPeerConnection(TSCDisconnectReason reason)
{
    if (m_observer.get() != nullptr) {
        m_observer->onDidDisconnectPeerConnection(this, reason);
    }
}

#pragma mark-

void
TSCParticipantConnection::onDidAddStream(const MediaStreamInterfaceRef& stream, TSCMediaStreamOrigin origin)
{
    if (m_observer.get() != nullptr) {
        m_observer->onDidAddStream(this, stream, origin);
    }
}

void
TSCParticipantConnection::onDidRemoveStream(const MediaStreamInterfaceRef& stream, TSCMediaStreamOrigin origin)
{
    if (m_observer.get() != nullptr) {
        m_observer->onDidRemoveStream(this, stream, origin);
    }
}
    
#pragma mark-
    
void
TSCParticipantConnection::onDidLinkAudioInputController(IAudioInputControllerInterface* controller)
{
    if (m_observer.get() != nullptr) {
        m_observer->onDidLinkAudioInputController(controller);
    }
}

void
TSCParticipantConnection::onDidUnlinkAudioInputController(IAudioInputControllerInterface* controller)
{
    if (m_observer.get() != nullptr) {
        m_observer->onDidUnlinkAudioInputController(controller);
    }
}

void
TSCParticipantConnection::onDidLinkVideoCaptureController(IVideoCaptureControllerInterface* controller)

{
    if (m_observer.get() != nullptr) {
        m_observer->onDidLinkVideoCaptureController(controller);
    }
}

void
TSCParticipantConnection::onDidUnlinkVideoCaptureController(IVideoCaptureControllerInterface* controller)
{
    if (m_observer.get() != nullptr) {
        m_observer->onDidUnlinkVideoCaptureController(controller);
    }
}
    
} //namespace twiliosdk


