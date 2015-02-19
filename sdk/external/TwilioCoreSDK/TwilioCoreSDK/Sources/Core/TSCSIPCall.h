//
//  TSCSIPCall.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_SIP_CALL_H
#define TSC_SIP_CALL_H

#include <pjsua-lib/pjsua.h>

#include "TSCoreSDKTypes.h"
#include "TSCoreConstants.h"

namespace twiliosdk {

class TSCSIPCallContext;
    
class TSCSIPCall
{
public:
    
    typedef enum _TSCSIPCallState {
        kTSCSIPCallStateInitial = 0,
        kTSCSIPCallStateConnecting,
        kTSCSIPCallStateConnected,
        kTSCSIPCallStateUserNotAvailable,
        kTSCSIPCallStateRejected,
        kTSCSIPCallStateIgnored,
        kTSCSIPCallStateFailed,
        kTSCSIPCallStateTerminated
    } TSCSIPCallState;
    
    TSCSIPCall(const std::string& participant,
               const TSCOptions& options,
               TSCSIPCallContext* callContext,
               const int callId = kTSCInvalidId);
    virtual ~TSCSIPCall();
    
    bool isValid() const;
    int getId() const;
    
    void call();
    void answer(const pjsip_status_code code);
    void reject();
    void ignore();
    void hangup();
    
    TSCSIPCallState getCallState() const;
    std::string getRemoteUser() const;
    std::string getRemoteOffer();

    void onLocalSDPCreated(pjsua_call_id call_id,
                           pjmedia_sdp_session *sdp,
                           pj_pool_t *pool,
                           const std::string& offer);
    void onRemoteSDPCreated(pjsua_call_id call_id,
                            const pjmedia_sdp_session *sdp,
                            pj_pool_t *pool,
                            std::string& offer);
private:
    TSCSIPCall();
    TSCSIPCall(const TSCSIPCall&);
    TSCSIPCall& operator=(TSCSIPCall&);
    
    mutable talk_base::CriticalSection m_lock;
    
    class TImpl;
    talk_base::scoped_ptr<TImpl> m_impl;
};
    
}  // namespace twiliosdk

#endif  // TSC_SIP_CALL_H
