//
//  TSCEndpoint.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 12/26/14.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#include "TSCEndpoint.h"
#include "TSCPJSUA.h"
#include "TSCSIPAccount.h"
#include "TSCEndpointObserver.h"
#include "TSCoreError.h"
#include "TSCLogger.h"
#include "TSCSession.h"
#include "TSCSessionObserver.h"
#include "TSCSessionLifeCycleObserver.h"
#include "TSCIncomingSession.h"
#include "TSCOutgoingSession.h"
#include "TSCSIPUtils.h"

namespace twiliosdk {

TSCEndpoint::TSCEndpoint(TSCPJSUAObjectRef pjsip, const TSCOptions& options, TSCEndpointObserverObjectRef observer)
{
    m_state = kTSCEndpointStateInitialized;
    m_PJSIPRef = pjsip;
    m_options = options;
    m_observer = observer;

    m_session_lifecycle_observer = new TSCSessionLifeCycleObserverObject();
    m_session_lifecycle_observer->setDelegate(this);
}

TSCEndpoint::~TSCEndpoint()
{
    m_observer = nullptr;
    m_session_lifecycle_observer = nullptr;
}

#pragma mark-

void
TSCEndpoint::registerEndpoint()
{
    m_sip_account = new TSCSIPAccountObject(m_options);
    if(m_sip_account->isValid()) {
        m_PJSIPRef->registerEndpoint(TSCEndpointObjectRef(this));
        m_sip_account->registerAccount();
        changeState(kTSCEndpointStateRegistering);
    } else {
        TSCErrorObjectRef error = new TSCErrorObject(kTSCoreSDKErrorDomain,
                                                     kTSCErrorInvalidSIPAccount);
        endpointDidRegister(error);
    }
}

void
TSCEndpoint::endpointDidRegister(const TSCErrorObjectRef& error)
{
    TSCEndpointState old_state = m_state;
    
    if(error.get() != nullptr) {
        changeState(kTSCEndpointStateRegistrationFailed);
        m_PJSIPRef->unregisterEndpoint(TSCEndpointObjectRef(this));
    } else {
        changeState(kTSCEndpointStateRegistered);
    }
    if (old_state != m_state) {
        if(m_observer.get()) {
            m_observer->onRegistrationComplete(error);
        }
    }
}

#pragma mark-
    
void
TSCEndpoint::unregisterEndpoint()
{
    changeState(kTSCEndpointStateUnregistering);
    if(m_sip_account->isValid()) {
        m_sip_account->unregisterAccount();
    } else {
        TSCErrorObjectRef error = new TSCErrorObject(kTSCoreSDKErrorDomain,
                                                     kTSCErrorInvalidSIPAccount);
        endpointDidUnregister(error);
    }
}

void
TSCEndpoint::endpointDidUnregister(const TSCErrorObjectRef& error)
{
    TSCEndpointState old_state = m_state;
    if (error.get() == nullptr) {
        changeState(kTSCEndpointStateUnregistered);
    } else {
        changeState(kTSCEndpointStateUnregisterationFailed);
    }
    // force to unregister endpoint
    m_PJSIPRef->unregisterEndpoint(TSCEndpointObjectRef(dynamic_cast<TSCEndpointObject*>(this)));
    if (old_state != m_state) {
        if(m_observer.get()) {
           m_observer->onUnregistrationComplete(error);
        }
    }
    changeState(kTSCEndpointStateInitialized);
}
    
#pragma mark-
    
void
TSCEndpoint::changeState(const TSCEndpointState state)
{
    m_state = state;
    if(m_observer.get()) {
        m_observer->onStateChange(state);
    }
}
    
#pragma mark-
    
pjsua_acc_id
TSCEndpoint::getAccountId() const
{
    if(m_sip_account.get() && m_sip_account->isValid()) {
       return m_sip_account->getId();
    }
    return PJSUA_INVALID_ID;
}

#pragma mark-
    
TSCOutgoingSessionObjectRef
TSCEndpoint::createSession(const TSCOptions& options, const TSCSessionObserverObjectRef& observer)
{
    TSCOutgoingSessionObjectRef result = new TSCOutgoingSessionObject(getAccountId(), options, observer);
    if(result.get() != nullptr)
       result->setSessionLifeCycleObserver(m_session_lifecycle_observer);
    return result;
}
    
void
TSCEndpoint::accept(TSCIncomingSessionObjectRef session, const TSCSessionObserverObjectRef& observer)
{
    session->setSessionLifeCycleObserver(m_session_lifecycle_observer);
    session->setSessionObserver(observer);
    session->start();
}
    
void
TSCEndpoint::reject(TSCIncomingSessionObjectRef session)
{
    session->reject();
}
    
void
TSCEndpoint::ignore(TSCIncomingSessionObjectRef session)
{
    session->ignore();
}


#pragma mark-
    
void
TSCEndpoint::onSessionDidStart(TSCSession* session)
{
    TSCSessionListIterator iter = std::find(m_active_sessions.begin(), m_active_sessions.end(), session);
    if(iter == m_active_sessions.end())
    {
        session->AddRef();
        m_active_sessions.push_back(session);
    }
}
    
void
TSCEndpoint::onSessionDidStop(TSCSession* session)
{
    TSCSessionListIterator iter = std::find(m_active_sessions.begin(), m_active_sessions.end(), session);
    if(iter != m_active_sessions.end())
    {
        m_active_sessions.erase(iter);
        session->Release();
    }
}

TSCSession*
TSCEndpoint::getSessionForId(uint64 sessionId) const
{
    TSCSession* result = nullptr;
    for(TSCSessionListConstIterator iter = m_active_sessions.begin(); iter != m_active_sessions.end(); iter++)
    {
        if((*iter)->getId() == sessionId)
        {
            result = *iter;
            break;
        }
    }
    return result;
}
    
#pragma mark-
    
pj_bool_t
TSCEndpoint::on_rx_request(pjsip_rx_data *rdata)
{
    return false;
}

pj_bool_t
TSCEndpoint::on_rx_response(pjsip_rx_data *rdata)
{
    return false;
}

#pragma mark-
    
void
TSCEndpoint::on_incoming_call(pjsua_acc_id acc_id, pjsua_call_id call_id, pjsip_rx_data *rdata)
{
    TS_CORE_LOG_DEBUG("Incoming call for account %d", acc_id);
    TSCIncomingSessionObjectRef session = new TSCIncomingSessionObject(getAccountId(), m_options, call_id);
    if (session.get() != nullptr) {
        if (m_observer.get() != nullptr) {
            m_observer->onReceiveIncomingCall(session);
            session->ringing();
        }
    }
}
    
void
TSCEndpoint::on_reg_started(pjsua_acc_id acc_id, pj_bool_t renew)
{
}
    
void
TSCEndpoint::on_reg_state2(pjsua_acc_id acc_id, pjsua_reg_info *info)
{
    TS_CORE_LOG_DEBUG("Registration state for account %d changed to %d", acc_id, info->cbparam->code);
    if (info->cbparam->code < PJSIP_SC_OK) {
        // processing
        return;
    } else if (info->cbparam->code == PJSIP_SC_OK) {
        if(info->cbparam->expiration < 1) {
           endpointDidUnregister();
        } else {
           endpointDidRegister();
        }
    } else if (info->cbparam->code < PJSIP_SC_MULTIPLE_CHOICES) {
        // registering...
    } else if (info->cbparam->code == PJSIP_SC_MOVED_TEMPORARILY) {
        // redirection
        pjsip_hdr* header = info->cbparam->rdata->msg_info.msg->hdr.next;
        pj_str_t redirect_uri;
        pj_pool_t* pool = pjsua_pool_create("redirectpool", 512, 512);
        // find new registar uri
        while (header != &info->cbparam->rdata->msg_info.msg->hdr) {
            if (header->type == PJSIP_H_CONTACT) {
                pjsip_contact_hdr* contact_hdr = (pjsip_contact_hdr*)header;
                pjsip_sip_uri* uri = (pjsip_sip_uri*)pjsip_uri_get_uri(contact_hdr->uri);
                char uri_str[2048];
                pjsip_uri_print(PJSIP_URI_IN_CONTACT_HDR, uri, uri_str, sizeof(uri_str));
                pj_strdup2(pool, &redirect_uri, uri_str);
                break;
            }
            header = header->next;
        }

        pjsua_acc_config* acc_cfg = (pjsua_acc_config*) pj_pool_alloc(pool, sizeof(pjsua_acc_config));
        pj_status_t status = pjsua_acc_get_config(acc_id, pool, acc_cfg);
        
        if (PJ_SUCCESS == status) {
            // remove proxy if exist
            if (acc_cfg->proxy_cnt > 0) {
                std::string transport = ";transport=" + m_options[kTSCSIPTransportTypeKey] + ";hide";
                std::string proxy_uri = TSCSIPUtils::pj2Str(redirect_uri) + transport;
                acc_cfg->proxy[0] = pj_strdup3(pool, proxy_uri.c_str());
            }
            pjsua_acc_modify(acc_id, acc_cfg);
        }
        pj_pool_release(pool);
        
    } else {
        TSCErrorObjectRef error = new TSCErrorObject(kTSCoreSDKErrorDomain,
                                                     kTSCErrorEndpointRegistration,
                                                     TSCSIPUtils::pj2Str(info->cbparam->reason));
        if (m_state == kTSCEndpointStateUnregistering) {
            endpointDidRegister(error);
        } else {
            endpointDidUnregister(error);
        }
    }
}

#pragma mark-
    
void
TSCEndpoint::on_call_state(uint64 session_id, pjsua_call_id call_id, pjsip_event *e)
{
    TSCSession* session = getSessionForId(session_id);
    if(session != nullptr)
    {
       session->on_call_state(call_id, e);
    }
}
    
void
TSCEndpoint::on_call_sdp_created(uint64 session_id,
                                 pjsua_call_id call_id,
                                 pjmedia_sdp_session *sdp,
                                 pj_pool_t *pool,
                                 const pjmedia_sdp_session *rem_sdp)
{
    TSCSession* session = getSessionForId(session_id);
    if(session != nullptr)
    {
       session->on_call_sdp_created(call_id, sdp, pool, rem_sdp);
    }
}
    
void
TSCEndpoint::on_call_rx_offer(uint64 session_id,
                              pjsua_call_id call_id,
                              const pjmedia_sdp_session *offer,
                              void *reserved,
                              pjsip_status_code *code,
                              pjsua_call_setting *opt)
{
    TSCSession* session = getSessionForId(session_id);
    if(session != nullptr)
    {
       session->on_call_rx_offer(call_id, offer, reserved, code, opt);
    }
}
    
void
TSCEndpoint::on_call_tsx_state(uint64 session_id,
                               pjsua_call_id call_id,
                               pjsip_transaction *tsx,
                               pjsip_event *e)
{
    TSCSession* session = getSessionForId(session_id);
    if(session != nullptr)
    {
       session->on_call_tsx_state(call_id, tsx, e);
    }
}

} // namespace twiliosdk
