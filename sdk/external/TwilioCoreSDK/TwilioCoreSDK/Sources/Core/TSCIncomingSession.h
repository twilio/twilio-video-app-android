//
//  TSCIncomingSession.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_INCOMING_SESSION_H
#define TSC_INCOMING_SESSION_H

#include "TSCSession.h"

namespace twiliosdk {
    
class TSCIncomingSession : public TSCSession
{
public:
    TSCIncomingSession(int accountId, const TSCOptions& options, uint64 callId);
    virtual ~TSCIncomingSession();

    void reject();
    void ignore();
    void ringing();
    
    void setSessionObserver(const TSCSessionObserverObjectRef& observer);
private:
    TSCIncomingSession();
    TSCIncomingSession(const TSCIncomingSession&);
    TSCIncomingSession& operator=(TSCIncomingSession&);
};
    
}  // namespace twiliosdk

#endif  // TSC_INCOMING_SESSION_H
