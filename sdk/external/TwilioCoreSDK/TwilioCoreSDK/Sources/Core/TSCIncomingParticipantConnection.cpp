//
//  TSCIncomingParticipantConnection.cpp
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 2/10/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCIncomingParticipantConnection.h"
#include "TSCLogger.h"
#include "TSCoreError.h"

#include "TSCParticipantConnectionObserver.h"
#include "TSCSIPCallContext.h"
#include "TSCIncomingPeerConnection.h"

namespace twiliosdk {
    
TSCIncomingParticipantConnection::TSCIncomingParticipantConnection(uint64 callId,
                                                                   TSCSIPCallContext* context,
                                                                   const TSCOptions& options,
                                                                   const TSCParticipantConnectionObserverRef& observer) :
    TSCParticipantConnection(TSCParticipant(""), options, observer)
{
    m_sip_call = new TSCSIPCallObject(std::string(""), m_options, context, callId);
    // parse participant from the remote user
    m_participant = TSCParticipant(m_sip_call->getRemoteUser());
}
    
void
TSCIncomingParticipantConnection::start(TSCSIPCallContext* context)
{
    m_peer_connection = new TSCIncomingPeerConnectionObject(m_options, this);
    if (isValid()) {
        m_peer_connection->open();
        std::string offer = m_sip_call->getRemoteOffer();
        m_peer_connection->setRemoteDescription(offer);
    }
}
    
void
TSCIncomingParticipantConnection::onDidIceGatheringComplete(const TSCErrorObjectRef& error)
{
    TSCParticipantConnection::onDidIceGatheringComplete(error);
    if (error.get() == nullptr && isValid()) {
        m_sip_call->answer(PJSIP_SC_OK);
    } else {
        TS_CORE_LOG_INFO("Ice gathering failed due to: %s", error->getMessage().c_str());
    }
}
    
void
TSCIncomingParticipantConnection::onDidSetSessionRemoteDescription(const TSCErrorObjectRef& error)
{
    TSCParticipantConnection::onDidSetSessionRemoteDescription(error);
    if (error.get() == nullptr && isValid()) {
        TS_CORE_LOG_INFO("Remote offer set succesfully");
        m_peer_connection->createLocalSessionDescription();
    }
}
    
} //namespace twiliosdk


