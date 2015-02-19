//
//  TSCOutgoingParticipantConnection.cpp
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 2/10/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCOutgoingParticipantConnection.h"
#include "TSCLogger.h"
#include "TSCoreError.h"

#include "TSCParticipantConnectionObserver.h"
#include "TSCSIPCallContext.h"
#include "TSCOutgoingPeerConnection.h"

namespace twiliosdk {

TSCOutgoingParticipantConnection::TSCOutgoingParticipantConnection(const TSCParticipant& participant,
                                                                   const TSCOptions& options,
                                                                   const TSCParticipantConnectionObserverRef& observer) :
    TSCParticipantConnection(participant, options, observer)
{
}
    
void
TSCOutgoingParticipantConnection::start(TSCSIPCallContext* context)
{
    m_peer_connection = new TSCOutgoingPeerConnectionObject(m_options, this);

    m_sip_call = new TSCSIPCallObject(m_participant.getAddress(),
                                      m_options,
                                      context);
    if (isValid()) {
        m_peer_connection->open();
        m_peer_connection->createLocalSessionDescription();
    }
}
    
void
TSCOutgoingParticipantConnection::onDidIceGatheringComplete(const TSCErrorObjectRef& error)
{
    TSCParticipantConnection::onDidIceGatheringComplete(error);
    if (error.get() == nullptr && isValid()) {
        m_sip_call->call();
    } else {
        TS_CORE_LOG_INFO("Ice gathering failed due to: %s", error->getMessage().c_str());
    }
}
    
void
TSCOutgoingParticipantConnection::on_call_state(pjsua_call_id call_id, pjsip_event *e)
{
    TSCSIPCall::TSCSIPCallState state = getSignalingState();
    switch (state) {
        case TSCSIPCall::kTSCSIPCallStateConnected: {
            const std::string offer = m_remote_offer;
            m_peer_connection->setRemoteDescription(offer);
            break;
        }
        default:
            break;
    }
}
    
} //namespace twiliosdk


