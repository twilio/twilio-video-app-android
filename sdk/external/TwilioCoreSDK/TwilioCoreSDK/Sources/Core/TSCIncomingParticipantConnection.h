//
//  TSCIncomingParticipantConnection.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 2/10/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_INCOMING_PARTICIPANT_CONNECTION_H
#define TSC_INCOMING_PARTICIPANT_CONNECTION_H

#include "TSCoreSDKTypes.h"
#include "TSCParticipantConnection.h"

namespace twiliosdk {

class TSCIncomingParticipantConnection : public TSCParticipantConnection
{
public:
    TSCIncomingParticipantConnection(uint64 callId,
                                     TSCSIPCallContext* context,
                                     const TSCOptions& options, const
                                     TSCParticipantConnectionObserverRef& observer);
    
    virtual void start(TSCSIPCallContext* context);
protected:    
    // peer connection observer
    virtual void onDidIceGatheringComplete(const TSCErrorObjectRef& error);
    virtual void onDidSetSessionRemoteDescription(const TSCErrorObjectRef& error);
};
    
}  // namespace twiliosdk

#endif // TSC_INCOMING_PARTICIPANT_CONNECTION_H
