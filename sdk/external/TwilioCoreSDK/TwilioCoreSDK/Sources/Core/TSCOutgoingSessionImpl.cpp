//
//  TSCOutgoingSessionImpl.cpp
//  Twilio Signal Core SDK
//
//  Created by Serhiy Semenyuk on 01/23/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCOutgoingSessionImpl.h"
#include "TSCoreError.h"
#include "TSCSessionObserver.h"
#include "TSCOutgoingParticipantConnection.h"
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

TSCOutgoingSessionImpl::TSCOutgoingSessionImpl(int accountId,
                                               const TSCOptions& options,
                                               const TSCSessionObserverObjectRef& observer) :
    TSCSessionImpl(accountId, options, observer)
{
}
    
void
TSCOutgoingSessionImpl::start()
{
    changeState(kTSCSessionStateStarting);
    
    m_participant_connections[0]->start(new TSCSIPCallContext(m_account_id, getId()));
    
    if (!m_participant_connections[0]->isValid()) {
        if(m_observer.get() != nullptr) {
            TSCErrorObjectRef error = new TSCErrorObject(kTSCoreSDKErrorDomain,
                                                         kTSCErrorSessionCreationFailed);
            m_observer->onStartComplete(error);
        }
    }
}
    
#pragma mark-
    
void
TSCOutgoingSessionImpl::on_call_state(pjsua_call_id call_id, pjsip_event *e)
{
    TSCSIPCall::TSCSIPCallState state = m_participant_connections[0]->getSignalingState();
    m_participant_connections[0]->on_call_state(call_id, e);
    switch (state) {
        case TSCSIPCall::kTSCSIPCallStateConnected: {
            TS_CORE_LOG_DEBUG("Participant SIP: connected");
            break;
        }
        case TSCSIPCall::kTSCSIPCallStateUserNotAvailable: {
            TS_CORE_LOG_DEBUG("Participant SIP: user not available");
            processParticipantConnect(m_participant_connections[0],
                                      new TSCErrorObject(kTSCoreSDKErrorDomain, kTSCErrorSessionParticipantNotAvailable));
            break;
        }
        case TSCSIPCall::kTSCSIPCallStateRejected: {
            TS_CORE_LOG_DEBUG("Participant SIP: rejected");
            processParticipantConnect(m_participant_connections[0],
                                      new TSCErrorObject(kTSCoreSDKErrorDomain, kTSCErrorSessionRejected));
            break;
        }
        case TSCSIPCall::kTSCSIPCallStateIgnored: {
            TS_CORE_LOG_DEBUG("Participant SIP: ignored");
            processParticipantConnect(m_participant_connections[0],
                                      new TSCErrorObject(kTSCoreSDKErrorDomain, kTSCErrorSessionIgnored));
            break;
        }
        case TSCSIPCall::kTSCSIPCallStateFailed: {
            TS_CORE_LOG_DEBUG("Participant SIP: failed");
            processParticipantConnect(m_participant_connections[0],
                                      new TSCErrorObject(kTSCoreSDKErrorDomain, kTSCErrorSessionFailed));
            break;
        }
        case TSCSIPCall::kTSCSIPCallStateTerminated: {
            TS_CORE_LOG_DEBUG("Participant SIP: terminated");
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
