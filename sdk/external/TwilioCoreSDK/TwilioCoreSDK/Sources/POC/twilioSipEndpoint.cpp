#ifdef DARWIN
#pragma GCC diagnostic ignored "-Wc++11-compat-deprecated-writable-strings"
#endif // DARWIN

#ifndef PJ_IS_LITTLE_ENDIAN
#define PJ_IS_LITTLE_ENDIAN 1
#endif

#ifndef PJ_IS_BIG_ENDIAN
#define PJ_IS_BIG_ENDIAN 0
#endif

#include <pjsua-lib/pjsua.h>
#include <pjsua-lib/pjsua_internal.h>

#include "twilioLogger.h"
#include "twilioSipAccount.h"
#include "twilioSipCall.h"
#include "twilioSipEndpoint.h"

namespace twiliosdk {

// Redirect PjSip logging to TwilioLogger
static void Log(int level, const char *data, int len) {
    std::string msg(data, len - 1);
    switch (level) {
        case 0:
            LOG_FATAL(msg); break;
        case 1:
            LOG_ERROR(msg); break;
        case 2:
            LOG_WARN(msg); break;
        case 3:
            LOG_INFO(msg); break;
        default:
            LOG_DEBUG(msg); break;
    }
}

static pjsip_module mod_info_handler =
{
        NULL, NULL, /* prev, next. */
        { "mod-info-handler", 16 }, /* Name. */
        -1, /* Id */
        PJSIP_MOD_PRIORITY_APPLICATION+99, /* Priority */
        NULL, /* load() */
        NULL, /* start() */
        NULL, /* stop() */
        NULL, /* unload() */
        &TwilioSipCall::onInfoRequest, /* on_rx_request() */
        NULL, /* on_rx_response() */
        NULL, /* on_tx_request. */
        NULL, /* on_tx_response() */
        NULL, /* on_tsx_state() */
};

static pjsip_module mod_options_handler =
{
        NULL, NULL, /* prev, next. */
        { "mod-opts-handler", 16 }, /* Name. */
        -1, /* Id */
        PJSIP_MOD_PRIORITY_APPLICATION+99, /* Priority */
        NULL, /* load() */
        NULL, /* start() */
        NULL, /* stop() */
        NULL, /* unload() */
        NULL, /* on_rx_request() */
        &TwilioSipAccount::onOptionsResponse, /* on_rx_response() */
        NULL, /* on_tx_request. */
        NULL, /* on_tx_response() */
        NULL, /* on_tsx_state() */
};
bool TwilioSipEndpoint::init(bool useTLS) {
    try {
        pj_status_t status;
        status = pjsua_create();
        if (status != PJ_SUCCESS) {
            LOG_ERROR_STREAM << "Error creating pjsua " << status << std::endl;
            throw;
        }

        pjsua_config ua_cfg;
        pjsua_logging_config log_cfg;
        pjsua_media_config media_cfg;

        pjsua_config_default(&ua_cfg);
        pjsua_logging_config_default(&log_cfg);
        pjsua_media_config_default(&media_cfg);
        log_cfg.cb = &Log;

        ua_cfg.cb.on_incoming_call = &TwilioSipAccount::onIncomingCall;
        ua_cfg.cb.on_reg_started = &TwilioSipAccount::onRegStarted;
        ua_cfg.cb.on_reg_state2 = &TwilioSipAccount::onRegState;

        ua_cfg.cb.on_call_state = &TwilioSipCall::onCallState;
        ua_cfg.cb.on_call_sdp_created = &TwilioSipCall::onCallSdpCreated;
        ua_cfg.cb.on_call_rx_offer = &TwilioSipCall::onCallSdpUpdate;
        ua_cfg.cb.on_call_tsx_state = &TwilioSipCall::onCallRequest;

        status = pjsua_init(&ua_cfg, &log_cfg, &media_cfg);
        if (status != PJ_SUCCESS) {
            LOG_ERROR_STREAM << "Error initializing pjsua " << status << std::endl;
            throw;
        }
        // Create transport
        pjsip_transport_type_e transport_type;
        pjsua_transport_id transport_id;
        pjsua_transport_config tcfg;
        pjsua_transport_config_default(&tcfg);
        
        if (useTLS) {
            tcfg.tls_setting.method = PJSIP_TLSV1_METHOD;
            transport_type = PJSIP_TRANSPORT_TLS;
        } else {
            transport_type = PJSIP_TRANSPORT_TCP;
        }
        status = pjsua_transport_create(transport_type, &tcfg, &transport_id);
        if (status != PJ_SUCCESS) {
            pjsua_destroy();
            LOG_ERROR_STREAM << "Error creating transport " << status << std::endl;
            throw;
        }

        /* Initialize our module to handle otherwise unhandled request */
        status = pjsip_endpt_register_module(pjsua_get_pjsip_endpt(),
                                             &mod_info_handler);
        if (status != PJ_SUCCESS) {
            pjsua_destroy();
            LOG_ERROR_STREAM << "Error registering info module " << status << std::endl;
            throw;
        }
        status = pjsip_endpt_register_module(pjsua_get_pjsip_endpt(),
                                             &mod_options_handler);
        if (status != PJ_SUCCESS) {
            pjsua_destroy();
            LOG_ERROR_STREAM << "Error registering options module " << status << std::endl;
            throw;
        }

        const pj_str_t trickle = pj_str("trickle-ice");
        pjsip_endpt_add_capability(pjsua_get_pjsip_endpt(), NULL, PJSIP_H_SUPPORTED, NULL,
                                   1, &trickle);

        status = pjsua_start();
        if (status != PJ_SUCCESS) {
            pjsua_destroy();
            LOG_ERROR_STREAM << "Error starting pjsua " << status << std::endl;
            throw;
        }
    } catch (...) {
        return false;
    }
    return true;
}

void TwilioSipEndpoint::destroy() {
    register_thread();
    try {
        pjsua_destroy();
    } catch (...) {
    }
}

void TwilioSipEndpoint::register_thread() {
    try {
        if (!pj_thread_is_registered()) {
            pj_thread_desc desc;
            pj_thread_t *this_thread;
            pj_bzero(desc, sizeof(desc));
            pj_thread_register("thread", desc, &this_thread);
        }
    } catch (...) {
    }
}

pj_pool_t* TwilioSipEndpoint::getPool() {
    static pj_pool_t* pool = pjsua_pool_create("TwilioSipEndpoint", 512, 256);
    return pool;
}

} // namespace twiliosdk

// pjmedia related functions implementations
// need this just to link without pjsip

pj_status_t pjsua_aud_subsys_init() {
    return PJ_SUCCESS;
}

pj_status_t pjsua_aud_subsys_start() {
    return PJ_SUCCESS;
}

pj_status_t pjsua_aud_subsys_destroy() {
    return PJ_SUCCESS;
}

void pjsua_aud_stop_stream(pjsua_call_media *call_med) {
    pjmedia_transport_detach(call_med->tp, call_med);
}

void pjsua_check_snd_dev_idle() {
}

pj_status_t pjsua_set_snd_dev( int capture_dev, int playback_dev) {
    PJ_UNUSED_ARG(capture_dev);
    PJ_UNUSED_ARG(playback_dev);
    return PJ_SUCCESS;
}

pj_status_t pjsua_aud_channel_update(pjsua_call_media *call_med,
                                     pj_pool_t *tmp_pool,
                                     pjmedia_stream_info *si,
                                     const pjmedia_sdp_session *local_sdp,
                                     const pjmedia_sdp_session *remote_sdp) {
    PJ_UNUSED_ARG(call_med);
    PJ_UNUSED_ARG(tmp_pool);
    PJ_UNUSED_ARG(si);
    PJ_UNUSED_ARG(local_sdp);
    PJ_UNUSED_ARG(remote_sdp);
    return PJ_SUCCESS;
}
