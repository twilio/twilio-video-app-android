//
//  TSCIncomingSessionImpl.cpp
//  Twilio Signal Core SDK
//
//  Created by Serhiy Semenyuk on 01/24/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCIncomingSessionImpl.h"
#include "TSCoreError.h"
#include "TSCSessionObserver.h"
#include "TSCIncomingParticipantConnection.h"
#include "TSCLogger.h"

// TODO: remove pjsip dependency from TSCPJSUA interface
#include <pj/types.h>
#include <pjsua-lib/pjsua.h>
#include "TSCPJSUA.h"
#include "TSCSIPCall.h"
#include "TSCSIPCallContext.h"
#include "TSCVideoSurface.h"
#include "TSCMediaTrackInfo.h"

namespace twiliosdk {

TSCIncomingSessionImpl::TSCIncomingSessionImpl(int accountId,
                                               const TSCOptions& options,
                                               uint64 callId) :
    TSCSessionImpl(accountId, options, nullptr)
{
    m_participant_connections.push_back(new TSCIncomingParticipantConnection(
                    callId, new TSCSIPCallContext(m_account_id, getId()), options, this));
}
    
void
TSCIncomingSessionImpl::start()
{
    changeState(kTSCSessionStateStarting);
    
    m_participant_connections[0]->start(nullptr);
    
    if (!m_participant_connections[0]->isValid()) {
        if(m_observer.get() != nullptr) {
            TSCErrorObjectRef error = new TSCErrorObject(kTSCoreSDKErrorDomain,
                                                         kTSCErrorSessionCreationFailed);
            m_observer->onStartComplete(error);
        }
    }
}
    
void
TSCIncomingSessionImpl::reject()
{
    m_participant_connections[0]->reject();
}
    
void
TSCIncomingSessionImpl::ignore()
{
    m_participant_connections[0]->ignore();
}
    
void
TSCIncomingSessionImpl::ringing()
{
    m_participant_connections[0]->ringing();
}

#pragma mark-
    
void
TSCIncomingSessionImpl::on_call_state(pjsua_call_id call_id, pjsip_event *e)
{
    TSCSIPCall::TSCSIPCallState state = m_participant_connections[0]->getSignalingState();
    m_participant_connections[0]->on_call_state(call_id, e);
    switch (state) {
        case TSCSIPCall::kTSCSIPCallStateConnected: {
            TS_CORE_LOG_DEBUG("Session SIP: connected");
            break;
        }
        case TSCSIPCall::kTSCSIPCallStateFailed: {
            TS_CORE_LOG_DEBUG("Session SIP: failed");
            processParticipantConnect(m_participant_connections[0],
                                      new TSCErrorObject(kTSCoreSDKErrorDomain, kTSCErrorSessionFailed));
            break;
        }
        case TSCSIPCall::kTSCSIPCallStateTerminated: {
            TS_CORE_LOG_DEBUG("Session SIP: terminated");
            m_participant_connections[0]->stop();
            if(m_observer.get() != nullptr) {
                m_observer->onParticipantDisconect(new TSCParticipantObject(m_participant_connections[0]->getParticipant()),
                                                   kTSCDisconnectReasonParticipantTerminated);
            }
            break;
        }
        default:
            break;
    }
}

} // namespace twiliosdk
