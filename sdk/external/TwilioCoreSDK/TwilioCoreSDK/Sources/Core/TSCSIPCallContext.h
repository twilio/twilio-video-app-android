//
//  TSCSIPCallContext.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/19/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_SIP_CALL_CONTEXT_H
#define TSC_SIP_CALL_CONTEXT_H

namespace twiliosdk {
    
class TSCSIPCallContext
{
public:
    TSCSIPCallContext(int accountId, uint64 sessionId) :
        m_account_id(accountId), m_session_id(sessionId) {}
    int m_account_id;
    uint64 m_session_id;
private:
    TSCSIPCallContext();
};
    
}  // namespace twiliosdk

#endif // TSC_SIP_CALL_CONTEXT_H
