//
//  TSCSIPCall.cpp
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include <pjsua-lib/pjsua.h>
#include <pjsua-lib/pjsua_internal.h>

#include <algorithm>

#include "TSCSIPCall.h"
#include "TSCSIPCallContext.h"
#include "TSCoreConstants.h"
#include "TSCLogger.h"
#include "TSCPJSUA.h"
#include "TSCSIPUtils.h"

namespace twiliosdk {
    
class TSCSIPCall::TImpl
{
public:
    
    TImpl(const std::string& participant,
          const TSCOptions& options,
          TSCSIPCallContext* callContext,
          const int callId)
    {
        m_options = options;
        m_participant = participant;
        m_call_id = callId;
        m_account_id = callContext->m_account_id;

        m_context.reset(callContext);
        if (isValid()) {
            pjsua_call_set_user_data(m_call_id, m_context.get());
        }
    }
    
    ~TImpl()
    {
        if (isValid() && pjsua_get_state() < PJSUA_STATE_CLOSING) {
            if (isActive()) {
                hangup();
            }
            pjsua_call_set_user_data(m_call_id, nullptr);
        }
    }
    
    bool isValid() const {return m_call_id != PJSUA_INVALID_ID;}
    pjsua_call_id getCallId() const {return m_call_id;}
    
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
    
    TImpl();

    bool isActive() const;
    
    void fillSIPHeaders(pjsip_hdr* headers);
    
    TSCOptions m_options;
    std::string m_participant;
    pjsua_acc_id m_account_id;
    pjsua_call_id m_call_id;
    talk_base::scoped_ptr<TSCSIPCallContext> m_context;
};
    
bool
TSCSIPCall::TImpl::isActive() const
{
    return isValid() && (pjsua_call_is_active(m_call_id) != 0);
}
    
void
TSCSIPCall::TImpl::fillSIPHeaders(pjsip_hdr* headers)
{
    pjsip_hdr sip_headers;
    pj_list_init(&sip_headers);
    TSCSIPUtils::addPjHeader(TSCSIPUtils::getPool(), &sip_headers, std::string(kTSCSIPHeaderUsername), m_options[kTSCUserNameKey]);
    TSCSIPUtils::addPjHeader(TSCSIPUtils::getPool(), &sip_headers, std::string(kTSCSIPHeaderPassword), m_options[kTSCPasswordKey]);
    TSCSIPUtils::addPjHeader(TSCSIPUtils::getPool(), &sip_headers, std::string(kTSCSIPHeaderToken), m_options[kTSCTokenKey]);
    TSCSIPUtils::addPjHeader(TSCSIPUtils::getPool(), &sip_headers, std::string(kTSCSIPHeaderAccountSid), m_options[kTSCAccountSidKey]);
    TSCSIPUtils::addPjHeader(TSCSIPUtils::getPool(), &sip_headers, std::string(kTSCSIPHeaderClientVersion), m_options[KTSCSIPClientVersionKey]);
    if (m_options.count(KTSCSIPUserAgentKey) > 0) {
        TSCSIPUtils::addPjHeader(TSCSIPUtils::getPool(), &sip_headers, std::string(kTSCSIPHeaderUserAgent), m_options[KTSCSIPUserAgentKey]);
    }
    
    TSCSIPUtils::copyPjHeader(TSCSIPUtils::getPool(), &sip_headers, headers);
}
    
void
TSCSIPCall::TImpl::call()
{
    TSCSIPUtils::registerThread();
    
    pj_status_t status(PJ_SUCCESS);
    std::string accound_sid = m_options[kTSCAccountSidKey];
    std::transform(accound_sid.begin(), accound_sid.end(), accound_sid.begin(), ::tolower);
    std::string remote_uri = "\"" + m_participant +
        "\" <sip:" + m_participant + "@" + accound_sid + "." + m_options[kTSCDomainKey] + ">";
    pj_str_t destination_uri = TSCSIPUtils::str2Pj(remote_uri);
    pjsua_msg_data msg_data;
    pjsua_msg_data_init(&msg_data);
    
    pjsua_call_setting opt;
    pjsua_call_setting_default(&opt);
    
    fillSIPHeaders(&msg_data.hdr_list);
    
    status = pjsua_call_make_call(m_account_id, &destination_uri, &opt,
                                  m_context.get(), &msg_data, &m_call_id);
    if (PJ_SUCCESS != status) {
        TS_CORE_LOG_ERROR("SIP call error: %d", status);
        hangup();
    }
}

void
TSCSIPCall::TImpl::answer(const pjsip_status_code code)
{
    TSCSIPUtils::registerThread();
    
    pj_status_t status(PJ_SUCCESS);
    pjsua_msg_data msg_data;
    pjsua_msg_data_init(&msg_data);
    
    fillSIPHeaders(&msg_data.hdr_list);
    
    //? msg_data.msg_body = str2Pj(answer);
    pjsua_call_setting opt;
    pjsua_call_setting_default(&opt);
    pjsua_media_channel_update(m_call_id, NULL, NULL);
    status = pjsua_call_answer2(m_call_id, &opt, code, NULL, &msg_data);
    if (PJ_SUCCESS != status) {
        TS_CORE_LOG_ERROR("SIP answer error: %d", status);
    }
}

void
TSCSIPCall::TImpl::reject()
{
    if (!isValid()) {
        return;
    }
    TSCSIPUtils::registerThread();
    pj_status_t status(PJ_SUCCESS);
    pjsua_msg_data msg_data;
    pjsua_msg_data_init(&msg_data);
    
    fillSIPHeaders(&msg_data.hdr_list);
    
    status = pjsua_call_answer(m_call_id, PJSIP_SC_DECLINE, NULL, &msg_data);
    if (PJ_SUCCESS != status) {
        TS_CORE_LOG_ERROR("SIP reject error: %d", status);
    }
}
    
void
TSCSIPCall::TImpl::ignore()
{
    if (!isValid()) {
        return;
    }
    TSCSIPUtils::registerThread();
    pj_status_t status(PJ_SUCCESS);
    pjsua_msg_data msg_data;
    pjsua_msg_data_init(&msg_data);
    
    fillSIPHeaders(&msg_data.hdr_list);
    
    status = pjsua_call_answer(m_call_id, PJSIP_SC_BUSY_HERE, NULL, &msg_data);
    if (PJ_SUCCESS != status) {
        TS_CORE_LOG_ERROR("SIP reject error: %d", status);
    }
}

void
TSCSIPCall::TImpl::hangup()
{
    if (!isValid()) {
        return;
    }
    TSCSIPUtils::registerThread();
    pj_status_t status(PJ_SUCCESS);
    pjsua_msg_data msg_data;
    pjsua_msg_data_init(&msg_data);
    
    fillSIPHeaders(&msg_data.hdr_list);
    
    status = pjsua_call_hangup(m_call_id, pjsip_status_code(0), NULL, &msg_data);
    if (PJ_SUCCESS != status) {
        TS_CORE_LOG_ERROR("SIP hangup error: %d", status);
    }
}
    
TSCSIPCall::TSCSIPCallState
TSCSIPCall::TImpl::getCallState() const
{
    pjsua_call_info call_info;
    pjsua_call_get_info(m_call_id, &call_info);
    TS_CORE_LOG_INFO("SIP call %d state %d due to reason %d",
                     m_call_id, call_info.state, call_info.last_status);
    
    TSCSIPCallState state(kTSCSIPCallStateInitial);
    switch (call_info.state) {
        case PJSIP_INV_STATE_CALLING:
        case PJSIP_INV_STATE_INCOMING:
        case PJSIP_INV_STATE_EARLY:
        case PJSIP_INV_STATE_CONNECTING:
            state = kTSCSIPCallStateConnecting;
            break;
        case PJSIP_INV_STATE_CONFIRMED:
            state = kTSCSIPCallStateConnected;
            break;
        case PJSIP_INV_STATE_DISCONNECTED: {
            switch (call_info.last_status) {
                case PJSIP_SC_TEMPORARILY_UNAVAILABLE:
                case PJSIP_SC_NOT_FOUND:
                case PJSIP_SC_REQUEST_TIMEOUT:
                case 477: // failed sending INVITE to other peer
                    state = kTSCSIPCallStateUserNotAvailable;
                    break;
                case PJSIP_SC_DECLINE:
                    state = kTSCSIPCallStateRejected;
                    break;
                case PJSIP_SC_BUSY_HERE:
                    state = kTSCSIPCallStateIgnored;
                    break;
                case PJSIP_SC_OK:
                    state = kTSCSIPCallStateTerminated;
                    break;
                default:
                    state = kTSCSIPCallStateFailed;
                    break;
            }
            break;
        }
        default:
            break;
    }
    return state;
}
    
std::string
TSCSIPCall::TImpl::getRemoteUser() const
{
    pjsua_call_info call_info;
    pjsua_call_get_info(m_call_id, &call_info);
    std::string remote_uri = TSCSIPUtils::pj2Str(call_info.remote_info);
    // uri can be in form "display" <sip:name@domain>, so parse name
    const std::string prefix("<sip:");
    std::size_t from = remote_uri.find(prefix);
    std::size_t to = remote_uri.find("@");
    if (from != std::string::npos &&
        to != std::string::npos) {
        remote_uri = remote_uri.substr(from + prefix.size(), to - from - prefix.size());
    }
    return remote_uri;
}
    

    
std::string
TSCSIPCall::TImpl::getRemoteOffer()
{
    
    pjsua_call call = pjsua_get_var()->calls[m_call_id];
    // For finished negotiation use active
    if (call.inv && call.inv->neg) {
        const pjmedia_sdp_session *offer = NULL;
        if (pjmedia_sdp_neg_get_state(call.inv->neg) == PJMEDIA_SDP_NEG_STATE_DONE) {
            pjmedia_sdp_neg_get_active_remote(call.inv->neg, &offer);
        } else {
            pjmedia_sdp_neg_get_neg_remote(call.inv->neg, &offer);
        }
        char buffer[PJSIP_MAX_PKT_LEN];
        int len = pjmedia_sdp_print(offer, buffer, sizeof(buffer));
        if (len > -1) {
            return std::string(buffer, len);
        }
    }
    return std::string("");
}

#pragma mark-

void
TSCSIPCall::TImpl::onLocalSDPCreated(pjsua_call_id call_id,
                                     pjmedia_sdp_session *sdp,
                                     pj_pool_t *pool,
                                     const std::string& offer)
{
    if (!isValid()) {
        m_call_id = call_id;
    }
    pjmedia_sdp_session* tmp_sdp;
    pjmedia_sdp_parse(pool, const_cast<char*>(offer.c_str()),
                      offer.size(), &tmp_sdp);
    TSCSIPUtils::copySdp(pool, tmp_sdp, sdp);
}

void
TSCSIPCall::TImpl::onRemoteSDPCreated(pjsua_call_id call_id,
                                      const pjmedia_sdp_session *sdp,
                                      pj_pool_t *pool,
                                      std::string& offer)
{
    if (!isValid()) {
        m_call_id = call_id;
    }
    char buf[PJSIP_MAX_PKT_LEN];
    int len;
    len = pjmedia_sdp_print(sdp, buf, sizeof(buf));
    offer = (len > -1) ? std::string(buf, len) : std::string("");
}
    
#pragma mark-
    
TSCSIPCall::TSCSIPCall(const std::string& participant,
                       const TSCOptions& options,
                       TSCSIPCallContext* callContext,
                       const int callId)
{
    m_impl.reset(new TImpl(participant, options, callContext, callId));
}

TSCSIPCall::~TSCSIPCall()
{
    talk_base::CritScope cs(&m_lock);
}

#pragma mark-

bool
TSCSIPCall::isValid() const
{
    talk_base::CritScope cs(&m_lock);
    return m_impl->isValid();
}

int
TSCSIPCall::getId() const
{
    talk_base::CritScope cs(&m_lock);
    return m_impl->getCallId();
}

#pragma mark-

void
TSCSIPCall::call()
{
    talk_base::CritScope cs(&m_lock);
    m_impl->call();
}
    
void
TSCSIPCall::answer(const pjsip_status_code code)
{
    talk_base::CritScope cs(&m_lock);
    m_impl->answer(code);
}
    
void
TSCSIPCall::reject()
{
    talk_base::CritScope cs(&m_lock);
    m_impl->reject();
}
    
void
TSCSIPCall::ignore()
{
    talk_base::CritScope cs(&m_lock);
    m_impl->ignore();
}
    
void
TSCSIPCall::hangup()
{
    talk_base::CritScope cs(&m_lock);
    m_impl->hangup();
}
    
#pragma mark-

TSCSIPCall::TSCSIPCallState
TSCSIPCall::getCallState() const
{
    talk_base::CritScope cs(&m_lock);
    return m_impl->getCallState();
}
    
std::string
TSCSIPCall::getRemoteUser() const
{
    talk_base::CritScope cs(&m_lock);
    return m_impl->getRemoteUser();
}
    
std::string
TSCSIPCall::getRemoteOffer()
{
    talk_base::CritScope cs(&m_lock);
    return m_impl->getRemoteOffer();
}

#pragma mark-
    
void
TSCSIPCall::onLocalSDPCreated(pjsua_call_id call_id,
                              pjmedia_sdp_session *sdp,
                              pj_pool_t *pool,
                              const std::string& offer)
{
    talk_base::CritScope cs(&m_lock);
    m_impl->onLocalSDPCreated(call_id, sdp, pool, offer);
}

void
TSCSIPCall::onRemoteSDPCreated(pjsua_call_id call_id,
                               const pjmedia_sdp_session *sdp,
                               pj_pool_t *pool,
                               std::string& offer)
{
    talk_base::CritScope cs(&m_lock);
    m_impl->onRemoteSDPCreated(call_id, sdp, pool, offer);
}

} // namespace twiliosdk

