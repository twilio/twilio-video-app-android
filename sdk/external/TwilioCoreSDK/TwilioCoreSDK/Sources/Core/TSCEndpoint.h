//
//  TSCEndpoint.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 12/26/14.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#ifndef TSC_ENDPOINT_H
#define TSC_ENDPOINT_H

#include "TSCoreSDKTypes.h"
#include "ITSCPJSUAClient.h"

namespace twiliosdk {

class TSCEndpoint: public ITSCPJSUAClient, public talk_base::RefCountInterface
{
public:
    
    TSCEndpoint(TSCPJSUAObjectRef pjsip, const TSCOptions& options, TSCEndpointObserverObjectRef observer);
    virtual ~TSCEndpoint();
    
    void registerEndpoint();
    void unregisterEndpoint();
    
    pjsua_acc_id getAccountId() const;
    
    TSCOutgoingSessionObjectRef createSession(const TSCOptions& options, const TSCSessionObserverObjectRef& observer);
    void accept(TSCIncomingSessionObjectRef session, const TSCSessionObserverObjectRef& observer);
    void reject(TSCIncomingSessionObjectRef session);
    void ignore(TSCIncomingSessionObjectRef session);
    
    TSCSession* getSessionForId(uint64 sessionId) const;
    
    // ITSCPJSUAClient
    pj_bool_t on_rx_request(pjsip_rx_data *rdata);
    pj_bool_t on_rx_response(pjsip_rx_data *rdata);

    void on_incoming_call(pjsua_acc_id acc_id, pjsua_call_id call_id, pjsip_rx_data *rdata);
    void on_reg_started(pjsua_acc_id acc_id, pj_bool_t renew);
    void on_reg_state2(pjsua_acc_id acc_id, pjsua_reg_info *info);
    
    void on_call_state(uint64 session_id, pjsua_call_id call_id, pjsip_event *e);
    void on_call_sdp_created(uint64 session_id,
                             pjsua_call_id call_id,
                             pjmedia_sdp_session *sdp,
                             pj_pool_t *pool,
                             const pjmedia_sdp_session *rem_sdp);
    void on_call_rx_offer(uint64 session_id,
                          pjsua_call_id call_id,
                          const pjmedia_sdp_session *offer,
                          void *reserved,
                          pjsip_status_code *code,
                          pjsua_call_setting *opt);
    void on_call_tsx_state(uint64 session_id,
                           pjsua_call_id call_id,
                           pjsip_transaction *tsx,
                           pjsip_event *e);

    void onSessionDidStart(TSCSession* session);
    void onSessionDidStop(TSCSession* session);

private:
    
    void endpointDidRegister(const TSCErrorObjectRef& error = nullptr);
    void endpointDidUnregister(const TSCErrorObjectRef& error = nullptr);
    
    void changeState(const TSCEndpointState state);

private:
    
    TSCEndpointState m_state;
    TSCOptions m_options;
    TSCPJSUAObjectRef m_PJSIPRef;
    TSCEndpointObserverObjectRef m_observer;
    TSCSIPAccountObjectRef m_sip_account;
    
    TSCSessionLifeCycleObserverObjectRef m_session_lifecycle_observer;
    TSCSessionList m_active_sessions;
};
    
}  // namespace twiliosdk

#endif  // TSC_ENDPOINT_H
