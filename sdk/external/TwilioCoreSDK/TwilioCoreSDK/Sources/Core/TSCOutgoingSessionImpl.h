//
//  TSCOutgoingSessionImpl.h
//  Twilio Signal Core SDK
//
//  Created by Serhiy Semenyuk on 01/23/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_OUTGOING_SESSION_IMPL_H
#define TSC_OUTGOING_SESSION_IMPL_H

#include "TSCSessionImpl.h"

namespace twiliosdk {
    
class TSCOutgoingSessionImpl : public TSCSessionImpl
{
public:
    TSCOutgoingSessionImpl(int accountId,
               const TSCOptions& options,
               const TSCSessionObserverObjectRef& observer);
    
    void start();

    // call session callback
    virtual void on_call_state(pjsua_call_id call_id, pjsip_event *e);
};
    
}  // namespace twiliosdk

#endif  // TSC_OUTGOING_SESSION_IMPL_H
