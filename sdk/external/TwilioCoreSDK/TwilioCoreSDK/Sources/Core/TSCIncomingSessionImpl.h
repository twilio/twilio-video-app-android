//
//  TSCIncomingSessionImpl.h
//  Twilio Signal Core SDK
//
//  Created by Serhiy Semenyuk on 01/24/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_INCOMING_SESSION_IMPL_H
#define TSC_INCOMING_SESSION_IMPL_H

#include "TSCSessionImpl.h"

namespace twiliosdk {
    
class TSCIncomingSessionImpl : public TSCSessionImpl
{
public:
    TSCIncomingSessionImpl(int accountId,
               const TSCOptions& options,
               uint64 callId);
    
    void start();
    void reject();
    void ignore();
    void ringing();
    
    // call session callback
    virtual void on_call_state(pjsua_call_id call_id, pjsip_event *e);
};
    
}  // namespace twiliosdk

#endif  // TSC_INCOMING_SESSION_IMPL_H
