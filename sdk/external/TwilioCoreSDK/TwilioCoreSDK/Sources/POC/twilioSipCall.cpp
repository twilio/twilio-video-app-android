#ifdef DARWIN
#pragma GCC diagnostic ignored "-Wc++11-compat-deprecated-writable-strings"
#endif // DARWIN

#include "twilioLogger.h"
#include "twilioSipAccount.h"
#include "twilioSipEndpoint.h"
#include "twilioSipCall.h"
#include "twilioUtils.h"

#include <pjsua-lib/pjsua_internal.h>

namespace twiliosdk {

int TwilioSipCall::current_call_id_ = PJSUA_INVALID_ID;

TwilioSipCall::TwilioSipCall(TwilioSipAccount* account, int call_id) :
    call_id_(call_id),
    account_(account),
    answered_initially_(false) {
    if (call_id_ != PJSUA_INVALID_ID) {
        current_call_id_ = call_id;
        pjsua_call_set_user_data(call_id_, this);
    }
    call_info_ = new pjsua_call_info();
}

TwilioSipCall::~TwilioSipCall() {
    if (call_id_ != PJSUA_INVALID_ID &&
        pjsua_get_state() < PJSUA_STATE_CLOSING) {
        if (isActive()) {
            hangup();
        }
        pjsua_call_set_user_data(call_id_, nullptr);
    }
    if (nullptr != call_info_) {
        delete call_info_;
        call_info_ = nullptr;
    }
}

TwilioSipCall* TwilioSipCall::get(int id) {
    TwilioSipCall* call = (TwilioSipCall*)pjsua_call_get_user_data(id);
    if (call) {
        current_call_id_ = id;
        call->call_id_ = id;
    } else {
        call = new TwilioSipCall(nullptr, id);
        LOG_INFO_STREAM << "Creating TwilioSipCall instance in advance for id " << id << std::endl;
    }
    return call;
}

void TwilioSipCall::subscribe(TwilioSipCallObserverInterface* listener) {
    SignalStateChanged.connect(listener, &TwilioSipCallObserverInterface::onSipCallStateChanged);
    SignalInfoReceived.connect(listener, &TwilioSipCallObserverInterface::onSipInfoMethodReceived);
    SignalPrackReceived.connect(listener, &TwilioSipCallObserverInterface::onSipPrackMethodReceived);
}

bool TwilioSipCall::call(TwilioSdkCallParams& params, std::string& offer) {
    pj_status_t status(PJ_SUCCESS);
    setLocalOffer(offer);
    std::string uri = account_->buildUri(params.remoteUser);
    try {
        pj_str_t destination_uri = str2Pj(uri);
        pjsua_msg_data msg_data;
        pjsua_msg_data_init(&msg_data);
        copyPjHeader(TwilioSipEndpoint::getPool(),
                     account_->getExtraHeaders(),
                     &msg_data.hdr_list);
        pjsua_call_setting opt;
        pjsua_call_setting_default(&opt);

        status = pjsua_call_make_call(account_->getId(), &destination_uri, &opt,
                                                  this, &msg_data, &call_id_);
        if (PJ_SUCCESS != status) {
            LOG_ERROR_STREAM << "SIP call error: " << status << std::endl;
            throw;
        }
    } catch(...) {
        SignalStateChanged(TERMINATED);
        return false;
    }
    return true;
}

void TwilioSipCall::reInvite(std::string& offer) {
    if (!isActive()) {
        return;
    }
    setLocalOffer(offer);
    pj_status_t status(PJ_SUCCESS);
    try {
        pjsua_msg_data msg_data;
        pjsua_msg_data_init(&msg_data);
        copyPjHeader(TwilioSipEndpoint::getPool(),
                     account_->getExtraHeaders(),
                     &msg_data.hdr_list);
        status = pjsua_call_reinvite2(call_id_, nullptr, &msg_data);
        if (PJ_SUCCESS != status) {
            throw;
        }
    } catch (...) {
        LOG_ERROR_STREAM << "SIP reinvite error: " << status << std::endl;
    }
}

void TwilioSipCall::info(std::string& data) {
    if (data.empty()) {
        return;
    }
    if (!isActive()) {
        return;
    }
    pj_status_t status(PJ_SUCCESS);
    try {
        pj_str_t info = pj_str("INFO");

        pjsua_msg_data msg_data;
        pjsua_msg_data_init(&msg_data);

        msg_data.content_type = pj_str("application/sdp");
        msg_data.msg_body = str2Pj(data);
        if (account_) {
            copyPjHeader(TwilioSipEndpoint::getPool(),
                         account_->getExtraHeaders(),
                         &msg_data.hdr_list);
        }

        addPjHeader(TwilioSipEndpoint::getPool(), &msg_data.hdr_list,
                    "Info-Package", "trickle-ice");
        addPjHeader(TwilioSipEndpoint::getPool(), &msg_data.hdr_list,
                    "Content-Disposition", "Info-Package");

        status = pjsua_call_send_request(call_id_, &info, &msg_data);
        if (status != PJ_SUCCESS) {
            throw;
        }
    } catch (...) {
        LOG_ERROR_STREAM << "Unable to send request " << status << std::endl;
    }
}

void TwilioSipCall::answer(const int code, std::string& answer) {
    setLocalOffer(answer);
    pj_status_t status(PJ_SUCCESS);
    try {
        pj_status_t status;
        pjsua_msg_data msg_data;
        pjsua_msg_data_init(&msg_data);
        if (!answered_initially_ && account_) {
            copyPjHeader(TwilioSipEndpoint::getPool(),
                         account_->getExtraHeaders(),
                         &msg_data.hdr_list);
        }
        msg_data.msg_body = str2Pj(answer);
        pjsua_call_setting opt;
        pjsua_call_setting_default(&opt);
        pjsua_media_channel_update(call_id_, NULL, NULL);
        status = pjsua_call_answer2(call_id_, &opt, pjsip_status_code(code), NULL, &msg_data);
        if (PJ_SUCCESS != status) {
            throw;
        }
        answered_initially_ = true;
    } catch(...) {
        LOG_ERROR_STREAM << "SIP answer error: " << status << std::endl;
    }
}

void TwilioSipCall::reject() {
    if (call_id_ == PJSUA_INVALID_ID) {
        return;
    }
    pj_status_t status(PJ_SUCCESS);
    try {
        pj_status_t status;
        pjsua_msg_data msg_data;
        pjsua_msg_data_init(&msg_data);
        if (!answered_initially_ && account_) {
            copyPjHeader(TwilioSipEndpoint::getPool(),
                         account_->getExtraHeaders(),
                         &msg_data.hdr_list);
        }
        status = pjsua_call_answer(call_id_, PJSIP_SC_BUSY_HERE, NULL, &msg_data);
        if (PJ_SUCCESS != status) {
            throw;
        }
        answered_initially_ = true;
    } catch(...) {
        LOG_ERROR_STREAM << "SIP reject error: " << status << std::endl;
    }
}

void TwilioSipCall::hangup() {
    if (call_id_ == PJSUA_INVALID_ID) {
        return;
    }
    pj_status_t status(PJ_SUCCESS);
    TwilioSipEndpoint::register_thread();
    try {
        pjsua_msg_data msg_data;
        pjsua_msg_data_init(&msg_data);
        if (account_) {
            copyPjHeader(TwilioSipEndpoint::getPool(),
                         account_->getExtraHeaders(),
                         &msg_data.hdr_list);
        }
        status = pjsua_call_hangup(call_id_, pjsip_status_code(0), NULL, &msg_data);
        if (PJ_SUCCESS != status) {
            throw;
        }
    } catch(...) {
        LOG_ERROR_STREAM << "SIP hangup error: " << status << std::endl;
    }
}

bool TwilioSipCall::isActive() const {
    if (call_id_ == PJSUA_INVALID_ID) {
        return false;
    }
    return (pjsua_call_is_active(call_id_) != 0);
}

pjsua_call_info TwilioSipCall::getDetails() const {
    pj_status_t result = pjsua_call_get_info(call_id_, call_info_);
    if (PJ_SUCCESS != result) {
        LOG_ERROR("Failed to get Call details");
    }

    return *call_info_;
}

int TwilioSipCall::getId() const {
    return call_id_;
}

std::string TwilioSipCall::remoteUser() const {
    std::string remote_uri = pj2Str(getDetails().remote_info);
    // uri can be in form "display" <sip:name@domain>, so parse name
    std::size_t from = remote_uri.find("<sip:");
    std::size_t to = remote_uri.find("@");
    if (from != std::string::npos &&
            to != std::string::npos) {
        remote_uri = remote_uri.substr(from + 5, to - from - 5);
    }
    return remote_uri;
}

std::string TwilioSipCall::remoteOffer() const {
    return remote_offer_;
}

void TwilioSipCall::setLocalOffer(std::string& offer) {
    local_offer_ = offer;
}

void TwilioSipCall::onCallState(pjsua_call_id call_id,
                                       pjsip_event *e) {
    PJ_UNUSED_ARG(e);
    TwilioSipCall *call = TwilioSipCall::get(call_id);
    if (!call) {
        return;
    }
    pjsua_call_info call_info = call->getDetails();

    LOG_DEBUG_STREAM << "SIP Call id: " << call_info.id
            << " status: " << pj2Str(call_info.state_text)
            << " reason: " << call_info.last_status << std::endl;

    switch (call_info.state) {
        case PJSIP_INV_STATE_DISCONNECTED:
            call_id = PJSUA_INVALID_ID;
            // parse extra disconnection reasons
            switch (call_info.last_status) {
                case PJSIP_SC_TEMPORARILY_UNAVAILABLE:
                case PJSIP_SC_NOT_FOUND:
                case PJSIP_SC_REQUEST_TIMEOUT:
                    call->SignalStateChanged(USER_NOT_AVAILABLE);
                    break;
                case PJSIP_SC_BUSY_HERE:
                    call->SignalStateChanged(REJECTED);
                    break;
                default:
                    call->SignalStateChanged(TERMINATED);
                    break;
            }
            break;
         case PJSIP_INV_STATE_EARLY:
             switch (call_info.last_status) {
                 case PJSIP_SC_RINGING:
                     call->SignalStateChanged(RINGING);
                     break;
                 case PJSIP_SC_PROGRESS:
                     call->SignalStateChanged(CONNECTING);
                     break;
                 default:
                     break;
             }
             break;
         case PJSIP_INV_STATE_CONFIRMED:
             switch (call_info.last_status) {
                 case PJSIP_SC_OK:
                     call->SignalStateChanged(CONNECTED);
                     break;
                 default:
                     break;
             }
             break;
         default:
             break;
    }
}

void TwilioSipCall::onCallSdpCreated(pjsua_call_id call_id,
                                     pjmedia_sdp_session *sdp,
                                     pj_pool_t *pool,
                                     const pjmedia_sdp_session *rem_sdp) {
    TwilioSipCall *call = TwilioSipCall::get(call_id);
    if (rem_sdp) {
        // parse & save remote offer if available
        char buf[PJSIP_MAX_PKT_LEN];
        int len;
        len = pjmedia_sdp_print(rem_sdp, buf, sizeof(buf));
        const std::string offer = (len > -1 ? std::string(buf, len): "");
        if (offer != call->remote_offer_) {
            call->remote_offer_ = offer;
            // trigger call status update
            TwilioSipCall::onCallState(call_id, NULL);
        }
    }
    if (sdp && !call->local_offer_.empty()) {
        pjmedia_sdp_session* tmp_sdp;
        pjmedia_sdp_parse(pool, const_cast<char*>(call->local_offer_.c_str()),
                          call->local_offer_.size(), &tmp_sdp);
        copySdp(pool, tmp_sdp, sdp);
        // Clear the offer to be send only once
        call->local_offer_.clear();
    }
}

void TwilioSipCall::onCallSdpUpdate(pjsua_call_id call_id,
                            const pjmedia_sdp_session *offer,
                            void *reserved,
                            pjsip_status_code *code,
                            pjsua_call_setting *opt) {
    // need this to trigger an updated remote sdp grabbing
    PJ_UNUSED_ARG(offer);
    PJ_UNUSED_ARG(reserved);
    PJ_UNUSED_ARG(code);
    PJ_UNUSED_ARG(opt);
    pjsua_media_channel_update(call_id, NULL, NULL);
}

pj_bool_t TwilioSipCall::onInfoRequest(pjsip_rx_data *rdata) {
    return onCallInfoRequest(PJSUA_INVALID_ID, rdata);
}

void TwilioSipCall::onCallRequest(pjsua_call_id call_id,
                                  pjsip_transaction *tsx,
                                  pjsip_event *e) {
    PJ_UNUSED_ARG(tsx);
    if (e->body.tsx_state.type != PJSIP_EVENT_RX_MSG) {
        // Check incoming messages only
        return;
    }
    if (onCallInfoRequest(call_id, e->body.rx_msg.rdata)) {
        return;
    }
    if (onCallPrackRequest(call_id, e->body.rx_msg.rdata)) {
        return;
    }
}

bool TwilioSipCall::onCallInfoRequest(pjsua_call_id call_id,
                                      pjsip_rx_data *rdata) {
    static pjsip_method info_method = {
            PJSIP_OTHER_METHOD,
            {"INFO", 4}
    };
    if (pjsip_method_cmp(&rdata->msg_info.msg->line.req.method,
                         &info_method) != 0) {
        // Skip non-Info in this handler
        return false;
    }
    const pj_str_t type_application = pj_str("application");
    const pj_str_t type_sdp = pj_str("sdp");
    pjsip_msg_body *body = rdata->msg_info.msg->body;
    bool is_sdp = body && body->len &&
            pj_stricmp(&body->content_type.type, &type_application) == 0 &&
            pj_stricmp(&body->content_type.subtype, &type_sdp) == 0;
    if (is_sdp) {

        TwilioSipCall *call = NULL;
        if (call_id != PJSUA_INVALID_ID) {
            call = TwilioSipCall::get(call_id);
            // Answer 200 OK via Call dialog
            pjsua_call *call_data;
            pjsip_dialog *dlg = NULL;
            acquire_call("TwilioSipCall::onCallRequest", call_id, &call_data, &dlg);

            pjsip_dlg_respond(dlg, rdata,
                              PJSIP_SC_OK,
                              NULL,
                              call->account_->getExtraHeaders(),
                              NULL);
        } else {
            call = TwilioSipCall::get(current_call_id_);
            // Answer 200 OK via Endpoint
            pjsip_endpt_respond_stateless(pjsua_get_pjsip_endpt(),
                                          rdata,
                                          PJSIP_SC_OK,
                                          NULL,
                                          call->account_->getExtraHeaders(),
                                          NULL);
        }
        call->SignalInfoReceived(std::string((char*)body->data, body->len));
    }
    return true;
}

bool TwilioSipCall::onCallPrackRequest(pjsua_call_id call_id,
                                      pjsip_rx_data *rdata) {
    static pjsip_method info_method = {
            PJSIP_OTHER_METHOD,
            {"PRACK", 5}
    };
    if (pjsip_method_cmp(&rdata->msg_info.msg->line.req.method,
                         &info_method) != 0) {
        // Skip non-Prack in this handler
        return false;
    }
    TwilioSipCall *call = TwilioSipCall::get(call_id);
    call->SignalPrackReceived();
    return true;
}

}  // namespace twiliosdk
