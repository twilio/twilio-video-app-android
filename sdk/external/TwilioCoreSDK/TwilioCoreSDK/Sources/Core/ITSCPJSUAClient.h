//
//  ITSCPJSUAClient.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 12/26/14.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#ifndef TSC_TSCPJSUA_CLIENT_H
#define TSC_TSCPJSUA_CLIENT_H

#include <pjsua-lib/pjsua.h>
#include <pjsua-lib/pjsua_internal.h>

namespace twiliosdk {

class ITSCPJSUAClient
{
public:
    virtual ~ITSCPJSUAClient(){};
    
    virtual pj_bool_t on_rx_request(pjsip_rx_data *rdata) = 0;
    virtual pj_bool_t on_rx_response(pjsip_rx_data *rdata) = 0;
    
    virtual void on_incoming_call(pjsua_acc_id acc_id, pjsua_call_id call_id, pjsip_rx_data *rdata) = 0;
    virtual void on_reg_started(pjsua_acc_id acc_id, pj_bool_t renew) = 0;
    virtual void on_reg_state2(pjsua_acc_id acc_id, pjsua_reg_info *info) = 0;
    virtual void on_call_state(uint64 session_id, pjsua_call_id call_id, pjsip_event *e) = 0;
    virtual void on_call_sdp_created(uint64 session_id,
                                     pjsua_call_id call_id,
                                     pjmedia_sdp_session *sdp,
                                     pj_pool_t *pool,
                                     const pjmedia_sdp_session *rem_sdp) = 0;
    virtual void on_call_rx_offer(uint64 session_id,
                                  pjsua_call_id call_id,
                                  const pjmedia_sdp_session *offer,
                                  void *reserved,
                                  pjsip_status_code *code,
                                  pjsua_call_setting *opt) = 0;
    virtual void on_call_tsx_state(uint64 session_id,
                                   pjsua_call_id call_id,
                                   pjsip_transaction *tsx,
                                   pjsip_event *e) = 0;
};
    
}  // namespace twiliosdk

#endif  // TSC_TSCPJSUA_CLIENT_H
