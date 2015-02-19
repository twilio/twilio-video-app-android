//
//  TSCOutgoingSession.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCOutgoingSession.h"
#include "TSCOutgoingSessionImpl.h"

namespace twiliosdk {

TSCOutgoingSession::TSCOutgoingSession(int accountId,
                                       const TSCOptions& options,
                                       const TSCSessionObserverObjectRef& observer):
    TSCSession(new TSCOutgoingSessionImpl(accountId, options, observer))
{
}

TSCOutgoingSession::~TSCOutgoingSession()
{
}
    
} // namespace twiliosdk
