//
//  TSCOutgoingParticipantConnection.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 2/10/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_OUTGOING_PARTICIPANT_CONNECTION_H
#define TSC_OUTGOING_PARTICIPANT_CONNECTION_H

#include "TSCoreSDKTypes.h"
#include "TSCParticipantConnection.h"

namespace twiliosdk {

class TSCOutgoingParticipantConnection : public TSCParticipantConnection
{
public:
    TSCOutgoingParticipantConnection(const TSCParticipant& participant,
                                     const TSCOptions& options,
                                     const TSCParticipantConnectionObserverRef& observer);
    
    virtual void start(TSCSIPCallContext* context);
    
    virtual void on_call_state(pjsua_call_id call_id, pjsip_event *e);
protected:
    // peer connection observer
    virtual void onDidIceGatheringComplete(const TSCErrorObjectRef& error);
};
    
}  // namespace twiliosdk

#endif // TSC_OUTGOING_PARTICIPANT_CONNECTION_H
