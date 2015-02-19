//
//  TSCPJSUA.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 12/26/14.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#include <pjsua-lib/pjsua.h>
#include <pjsua-lib/pjsua_internal.h>


#include "TSCPJSUA.h"
#include "TSCSIPUtils.h"
#include "TSCEndpoint.h"
#include "TSCLogger.h"
#include "TSCSIPCallContext.h"

namespace twiliosdk {

namespace {
    
    typedef std::map<pjsua_acc_id, const TSCEndpointObjectRef> RegistryType;
    RegistryType&
    GetRegistry()
    {
        static RegistryType		sRegistry;
        return sRegistry;
    }

    void register_endpoint(pjsua_acc_id accountId, const TSCEndpointObjectRef& endpoint)
    {
        GetRegistry().insert(std::make_pair(accountId, endpoint));
    }
    
    void unregister_endpoint(pjsua_acc_id accountId)
    {
        GetRegistry().erase(accountId);
    }
    
    bool has_endpoint(pjsua_acc_id accountId)
    {
        RegistryType::const_iterator pos = GetRegistry().find(accountId);
        return (pos != GetRegistry().end());
    }
    
    const TSCEndpointObjectRef& find_endpoint(pjsua_acc_id accountId)
    {
        RegistryType::const_iterator pos = GetRegistry().find(accountId);
        return pos->second;
    }
    
}

#pragma mark-
    
static void on_log(int level, const char *data, int len)
{
    const std::string msg(data, len);
    switch (level) {
        case 0:
            TS_CORE_LOG_MODULE(kTSCoreLogModulePJSIP, twiliosdk::kTSCoreLogLevelFatal, "%s", msg.c_str()); break;
        case 1:
            TS_CORE_LOG_MODULE(kTSCoreLogModulePJSIP, twiliosdk::kTSCoreLogLevelError, "%s", msg.c_str()); break;
        case 2:
            TS_CORE_LOG_MODULE(kTSCoreLogModulePJSIP, twiliosdk::kTSCoreLogLevelWarning, "%s", msg.c_str()); break;
        case 3:
            TS_CORE_LOG_MODULE(kTSCoreLogModulePJSIP, twiliosdk::kTSCoreLogLevelInfo, "%s", msg.c_str()); break;
        default:
            TS_CORE_LOG_MODULE(kTSCoreLogModulePJSIP, twiliosdk::kTSCoreLogLevelDebug, "%s", msg.c_str()); break;
    }
}

static pj_bool_t on_rx_request(pjsip_rx_data *rdata)
{
    pjsua_acc_id acc_id = pjsua_acc_find_for_incoming(rdata);
    if(has_endpoint(acc_id))
       return find_endpoint(acc_id)->on_rx_request(rdata);
    return false;
}

static pj_bool_t on_rx_response(pjsip_rx_data *rdata)
{
    pjsua_acc_id acc_id = pjsua_acc_find_for_incoming(rdata);
    if(has_endpoint(acc_id))
       return find_endpoint(acc_id)->on_rx_response(rdata);
    return false;
}

#pragma mark-
    
static void on_incoming_call(pjsua_acc_id acc_id, pjsua_call_id call_id, pjsip_rx_data *rdata)
{
    if(has_endpoint(acc_id))
       find_endpoint(acc_id)->on_incoming_call(acc_id, call_id, rdata);
}
  
static void on_reg_started(pjsua_acc_id acc_id, pj_bool_t renew)
{
    if(has_endpoint(acc_id))
       find_endpoint(acc_id)->on_reg_started(acc_id, renew);
}

static void on_reg_state2(pjsua_acc_id acc_id, pjsua_reg_info *info)
{
    if(has_endpoint(acc_id))
       find_endpoint(acc_id)->on_reg_state2(acc_id, info);
}

#pragma mark-

static void on_call_state(pjsua_call_id call_id, pjsip_event *e)
{
    TSCSIPCallContext* context = (TSCSIPCallContext*)pjsua_call_get_user_data(call_id);
    if(context != nullptr)
    {
        if(has_endpoint(context->m_account_id))
           find_endpoint(context->m_account_id)->on_call_state(context->m_session_id, call_id, e);
    }
}

static void on_call_sdp_created(pjsua_call_id call_id,
                                pjmedia_sdp_session *sdp,
                                pj_pool_t *pool,
                                const pjmedia_sdp_session *rem_sdp)
{
    TSCSIPCallContext* context = (TSCSIPCallContext*)pjsua_call_get_user_data(call_id);
    if(context != nullptr)
    {
        if(has_endpoint(context->m_account_id))
           find_endpoint(context->m_account_id)->on_call_sdp_created(context->m_session_id, call_id, sdp, pool, rem_sdp);
    }
}

static void on_call_rx_offer(pjsua_call_id call_id,
                             const pjmedia_sdp_session *offer,
                             void *reserved,
                             pjsip_status_code *code,
                             pjsua_call_setting *opt)
{
    TSCSIPCallContext* context = (TSCSIPCallContext*)pjsua_call_get_user_data(call_id);
    if(context != nullptr)
    {
        if(has_endpoint(context->m_account_id))
           find_endpoint(context->m_account_id)->on_call_rx_offer(context->m_session_id, call_id, offer, reserved, code, opt);
    }
}

static void on_call_tsx_state(pjsua_call_id call_id,
                              pjsip_transaction *tsx,
                              pjsip_event *e)
{
    TSCSIPCallContext* context = (TSCSIPCallContext*)pjsua_call_get_user_data(call_id);
    if(context != nullptr)
    {
        if(has_endpoint(context->m_account_id))
           find_endpoint(context->m_account_id)->on_call_tsx_state(context->m_session_id, call_id, tsx, e);
    }
}

#pragma mark-
    
class TSCPJSUA::TState
{
public:
    
    TState()
    {
        m_initialized = init();
    }
    
    ~TState()
    {
        if (GetRegistry().size() > 0 )
        {
            PJ_LOG(1, (__FILE__, "Error: pjsip stack still has %d endpoints registered!", GetRegistry().size()));
            throw;
        }
        if(m_initialized)
        {
            TSCSIPUtils::registerThread();
            try {
                pjsua_destroy();
                m_initialized = false;
            } catch (...) {
            }
        }
    }
    
    bool isInitialized() const {return m_initialized;}
    
private:
    bool init();
    
    bool m_initialized;
};

bool
TSCPJSUA::TState::init()
{
    bool result = false;
    do
    {
        pj_status_t status;
        status = pjsua_create();
        if (status != PJ_SUCCESS) {
            TS_CORE_LOG_ERROR("Error creating pjsua %d", status);
            break;
        }
        
        pjsua_config ua_cfg;
        pjsua_logging_config log_cfg;
        pjsua_media_config media_cfg;
        
        pjsua_config_default(&ua_cfg);
        pjsua_logging_config_default(&log_cfg);
        pjsua_media_config_default(&media_cfg);
        log_cfg.cb = &on_log;
        
        ua_cfg.cb.on_incoming_call = &on_incoming_call;
        ua_cfg.cb.on_reg_started = &on_reg_started;
        ua_cfg.cb.on_reg_state2 = &on_reg_state2;
        
        ua_cfg.cb.on_call_state = &on_call_state;
        ua_cfg.cb.on_call_sdp_created = &on_call_sdp_created;
        ua_cfg.cb.on_call_rx_offer = &on_call_rx_offer;
        ua_cfg.cb.on_call_tsx_state = &on_call_tsx_state;
        
        status = pjsua_init(&ua_cfg, &log_cfg, &media_cfg);
        if (status != PJ_SUCCESS) {
            TS_CORE_LOG_ERROR("Error initializing pjsua %d", status);
            break;
        }
        status = pjsua_start();
        if (status != PJ_SUCCESS) {
            pjsua_destroy();
            TS_CORE_LOG_ERROR("Error starting pjsua %d", status);
            break;
        }
        
        result = true;
    } while(false);
    return result;
}
    
#pragma mark-
    
TSCPJSUA::TSCPJSUA()
{
    m_state.reset(new TState());
}

TSCPJSUA::~TSCPJSUA()
{
}

#pragma mark-

bool
TSCPJSUA::isInitialized() const
{
    return m_state.get()->isInitialized();
}

#pragma mark-
    
void
TSCPJSUA::registerEndpoint(const TSCEndpointObjectRef& endpoint)
{
    register_endpoint(endpoint->getAccountId(), endpoint);
}

void
TSCPJSUA::unregisterEndpoint(const TSCEndpointObjectRef& endpoint)
{
    unregister_endpoint(endpoint->getAccountId());
}
    
} // namespace twiliosdk
