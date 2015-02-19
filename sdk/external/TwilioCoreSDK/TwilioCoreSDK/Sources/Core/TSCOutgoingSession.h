//
//  TSCOutgoingSession.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_OUTGOING_SESSION_H
#define TSC_OUTGOING_SESSION_H

#include "TSCSession.h"

namespace twiliosdk {
    
class TSCOutgoingSession : public TSCSession
{
public:
    TSCOutgoingSession(int accountId, const TSCOptions& options, const TSCSessionObserverObjectRef& observer);
    virtual ~TSCOutgoingSession();
    
private:
    TSCOutgoingSession();
    TSCOutgoingSession(const TSCOutgoingSession&);
    TSCOutgoingSession& operator=(TSCOutgoingSession&);
};
    
}  // namespace twiliosdk

#endif  // TSC_OUTGOING_SESSION_H
