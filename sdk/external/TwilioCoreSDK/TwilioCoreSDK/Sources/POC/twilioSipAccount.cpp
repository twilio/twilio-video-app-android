#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic ignored "-Wc++11-compat-deprecated-writable-strings"
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif
#include "talk/base/thread.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif


#include "twilioLogger.h"
#include "twilioSipAccount.h"
#include "twilioSipCall.h"
#include "twilioSipEndpoint.h"
#include "twilioUtils.h"
#include "string.h"
#include <sstream>

#include <pjsua-lib/pjsua_internal.h>

namespace twiliosdk {

TwilioSipAccount::TwilioSipAccount(bool useTLS) : id_(PJSUA_INVALID_ID) {
    pj_list_init(&extra_headers_);
    if (useTLS){
        sip_transport_type_ = TWILIO_SIP_TRANSPORT_TYPE_TLS;
    } else {
        sip_transport_type_ = TWILIO_SIP_TRANSPORT_TYPE_TCP;
    }
}

TwilioSipAccount::~TwilioSipAccount() {
    if (pjsua_acc_is_valid(id_) != 0) {
        pjsua_acc_set_user_data(id_, NULL);
        pjsua_acc_del(id_);
    }
}

TwilioSipAccount* TwilioSipAccount::get(pjsua_acc_id id) {
    TwilioSipAccount* acc = (TwilioSipAccount*)pjsua_acc_get_user_data(id);
    if (!acc) {
        LOG_ERROR_STREAM << "Unable to find TwilioSipAccount instance for id "
            << id << std::endl;
    }
    return acc;
}

int TwilioSipAccount::getId() const {
    return id_;
}

bool TwilioSipAccount::init(TwilioSdkInitParams& params) {
    acc_sid_ = params.accSid;
    domain_ = params.domain;
    std::string alias = buildUri(params.alias);
    std::string registar = "sip:" + domain_;
    std::ostringstream port;

    port << portOption();
    //registar = "sip:" + params.registrar + ":" + port.str() + transportOption();
    registar = "sip:" + domain_ + ":" + port.str() + transportOption();
    std::string proxy = "<sip:" + params.registrar + ":" + REGISTAR_PORT_STR + ";transport=tcp;hide>";

    // create extra headers to pass with each sip message
    addPjHeader(TwilioSipEndpoint::getPool(), &extra_headers_, std::string(TWILIO_USERNAME_SIP_HEADER), params.user);
    addPjHeader(TwilioSipEndpoint::getPool(), &extra_headers_, std::string(TWILIO_PASSWORD_SIP_HEADER), params.password);
    addPjHeader(TwilioSipEndpoint::getPool(), &extra_headers_, std::string(TWILIO_TOKEN_SIP_HEADER), params.capabilityToken);
    addPjHeader(TwilioSipEndpoint::getPool(), &extra_headers_, std::string(TWILIO_CLIENT_SIP_HEADER), "{}");
    addPjHeader(TwilioSipEndpoint::getPool(), &extra_headers_, std::string(TWILIO_ACCOUNTSID_SIP_HEADER), acc_sid_);
    addPjHeader(TwilioSipEndpoint::getPool(), &extra_headers_, std::string(TWILIO_CLIENTVERSION_SIP_HEADER), std::string(TWILIO_CLIENTVERSION_NUMBER));
    
    pj_status_t status;
        try {
            pjsua_acc_config config;
            pjsua_acc_config_default(&config);
            config.user_data = (void*)this;

            config.id = str2Pj(alias);
            LOG_INFO_STREAM << registar << std::endl;
            config.reg_uri = str2Pj(registar);
            config.proxy[config.proxy_cnt++] = str2Pj(proxy);

            copyPjHeader(TwilioSipEndpoint::getPool(), &extra_headers_, &config.reg_hdr_list);

            status = pjsua_acc_add(&config, true, &id_);
            if (status != PJ_SUCCESS) {
                throw;
            }
        } catch (...) {
            LOG_ERROR_STREAM << "SIP Account creation error: " << status << std::endl;
            return false;
        }
        return true;
}

void TwilioSipAccount::options(TwilioSdkInitParams& params, const std::string& to) {
    pj_status_t status;
    try {
        std::string targetStr("<sip:" + params.registrar + ":" + REGISTAR_PORT_STR + ";transport=tcp>");
        pj_str_t target = str2Pj(targetStr);
        std::string fromStr(buildUri(params.alias));
        pj_str_t from_url = str2Pj(fromStr);
        std::string toStr(buildUri(to));
        pj_str_t to_url = str2Pj(toStr);
        pjsip_tx_data *tdata;
        status = pjsip_endpt_create_request(pjsua_get_pjsip_endpt(),
                pjsip_get_options_method(),
                &target, &from_url, &to_url, NULL, NULL, -1, NULL, &tdata);
        if (status != PJ_SUCCESS) {
            throw;
        }

        pjsua_msg_data msg_data;
        pjsua_msg_data_init(&msg_data);

        copyPjHeader(TwilioSipEndpoint::getPool(),
                getExtraHeaders(),
                &msg_data.hdr_list);

        pjsua_process_msg_data(tdata, &msg_data);

        status = pjsip_endpt_send_request_stateless(pjsua_get_pjsip_endpt(), tdata, NULL, NULL);
        if (status != PJ_SUCCESS) {
            pjsip_tx_data_dec_ref(tdata);
            throw;
        }
    }
    catch (...) {
        LOG_ERROR_STREAM << "Unable to send request " << status << std::endl;
    }
}

void TwilioSipAccount::subscribe(TwilioSipAccountObserverInterface* listener) {
    SignalIncomingCall.connect(
            listener, &TwilioSipAccountObserverInterface::onSipAccountIncomingCall);
    SignalStateChanged.connect(
            listener, &TwilioSipAccountObserverInterface::onSipAccountStateChanged);
    SignalRemoteTrickleSupported.connect(
            listener, &TwilioSipAccountObserverInterface::onSipAccountRemoteTrickleSupported);
}

std::string TwilioSipAccount::buildUri(const std::string& name) {
    return "\"" + name + "\" <sip:" + name + "@" + acc_sid_ + "." + domain_ + transportOption() + ">";
}

std::string TwilioSipAccount::transportOption() {
    if (sip_transport_type_ == TWILIO_SIP_TRANSPORT_TYPE_TLS) {
        return ";transport=tls";
    } else {
        return ";transport=tcp";
    }
}

int TwilioSipAccount::portOption() {
    if (sip_transport_type_ == TWILIO_SIP_TRANSPORT_TYPE_TLS) {
        return TWILIO_DEFAULT_CHUNDER_PORT_TLS;
    } else {
        return TWILIO_DEFAULT_CHUNDER_PORT_TCP;
    }
}

pjsip_hdr* TwilioSipAccount::getExtraHeaders() {
    return &extra_headers_;
}

pj_bool_t TwilioSipAccount::onOptionsResponse(pjsip_rx_data *rdata) {
    if (!rdata->msg_info.cseq || pjsip_method_cmp(&rdata->msg_info.cseq->method,
                pjsip_get_options_method()) != 0) {
        // Skip non-Options in this handler
        return PJ_FALSE;
    }
    TwilioSipAccount* account =
        TwilioSipAccount::get(pjsua_acc_get_default());

    if (!account) {
        return PJ_FALSE;
    }
    bool has_trickle = false;
    const pj_str_t trickle = pj_str("trickle-ice");
    pjsip_supported_hdr *supported = rdata->msg_info.supported;
    if (supported && rdata->msg_info.msg->line.status.code == PJSIP_SC_OK) {
        for(unsigned i = 0; i < supported->count; ++i) {
            if (pj_strcmp(&trickle, &supported->values[i]) == 0) {
                has_trickle = true;
                break;
            }
        }
    }
    account->SignalRemoteTrickleSupported(has_trickle);
    return PJ_TRUE;
}

void TwilioSipAccount::onIncomingCall(pjsua_acc_id acc_id,
        pjsua_call_id call_id,
        pjsip_rx_data *rdata) {
    PJ_UNUSED_ARG(rdata);
    TwilioSipAccount* acc = TwilioSipAccount::get(acc_id);
    TwilioSipCall* call = TwilioSipCall::get(call_id);
    if (!acc) {
        pjsua_call_hangup(call_id, PJSIP_SC_INTERNAL_SERVER_ERROR, NULL, NULL);
        delete call;
        return;
    }
    // assign call to this account
    call->account_ = acc;
    acc->SignalIncomingCall(call_id);
}

void TwilioSipAccount::onRegStarted(pjsua_acc_id acc_id,
        pj_bool_t renew) {
    TwilioSipAccount* acc = TwilioSipAccount::get(acc_id);
    if (!acc) {
        return;
    }
    if (renew) {
        acc->SignalStateChanged(LOGGINGIN);
    }
}

void TwilioSipAccount::onRegState(pjsua_acc_id acc_id,
        pjsua_reg_info *info) {
    TwilioSipAccount* acc = TwilioSipAccount::get(acc_id);
    if (!acc) {
        return;
    }
    LOG_DEBUG_STREAM << "SIP Account status: " << info->cbparam->status
        << " code: " << info->cbparam->code << " reason: " << pj2Str(info->cbparam->reason) << std::endl;
    if (info->cbparam->code < PJSIP_SC_OK) {
        return;
    } else if (info->cbparam->code == PJSIP_SC_OK) {
        acc->SignalStateChanged(INITIALIZED);
    } else if (info->cbparam->code < PJSIP_SC_MULTIPLE_CHOICES) {
        acc->SignalStateChanged(LOGGINGIN);
    } else if (info->cbparam->code == PJSIP_SC_MOVED_TEMPORARILY) {
        std::string reason = pj2Str(info->cbparam->reason);
        const char* full_msg = info->cbparam->rdata->msg_info.msg_buf;

        pjsip_hdr* h = info->cbparam->rdata->msg_info.msg->hdr.next;
        pj_str_t redirect_uri = pj_str("");
        pj_pool_t* pool = pjsua_pool_create("redirectpool", 512, 512);
        while (h != &info->cbparam->rdata->msg_info.msg->hdr) {
            if (h->type == PJSIP_H_CONTACT) {
                pjsip_contact_hdr* contact_hdr = (pjsip_contact_hdr*)h;
                pjsip_sip_uri* uri = (pjsip_sip_uri*)pjsip_uri_get_uri(contact_hdr->uri);
                char uri_str[2048];
                pjsip_uri_print(PJSIP_URI_IN_CONTACT_HDR, uri, uri_str, sizeof(uri_str));
                pj_strdup2(pool, &redirect_uri, uri_str);
                break;
            }
            h = h->next;
        }

        pj_status_t status;
        pjsua_acc_config* acc_cfg = (pjsua_acc_config*) pj_pool_alloc(pool, sizeof(pjsua_acc_config));
        status = pjsua_acc_get_config(acc_id, pool, acc_cfg);

        if (PJ_SUCCESS == status) {
            LOG_INFO_STREAM << "old uri: " << pj2Str(acc_cfg->reg_uri) << std::endl;
            acc_cfg->reg_uri = redirect_uri;
            LOG_INFO_STREAM << "new redirect uri: " << pj2Str(acc_cfg->reg_uri) << std::endl;
            pjsua_acc_modify(acc_id, acc_cfg);
        }
        pj_pool_release(pool);

    } else {
        LOG_ERROR_STREAM << "SIP Account registration error: " << info->cbparam->status
            << " code: " << info->cbparam->code << " reason: " << pj2Str(info->cbparam->reason) << std::endl;
        acc->SignalStateChanged(LOGGINGIN_ERROR);
    }
}

} // namespace twiliosdk
