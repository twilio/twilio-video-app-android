//
//  TSCIncomingSession.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCIncomingSession.h"
#include "TSCIncomingSessionImpl.h"

namespace twiliosdk {
    
TSCIncomingSession::TSCIncomingSession(int accountId,
                                       const TSCOptions& options,
                                       uint64 callId):
    TSCSession(new TSCIncomingSessionImpl(accountId, options, callId))
{
}
    
TSCIncomingSession::~TSCIncomingSession()
{
}
    
void
TSCIncomingSession::reject()
{
    m_impl->reject();
}
    
void
TSCIncomingSession::ignore()
{
    m_impl->ignore();
}
    
void
TSCIncomingSession::ringing()
{
    m_impl->ringing();
}
    
void
TSCIncomingSession::setSessionObserver(const TSCSessionObserverObjectRef& observer)
{
    m_impl->setSessionObserver(observer);
}

} // namespace twiliosdk
