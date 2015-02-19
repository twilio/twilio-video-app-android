#ifndef TWILIOSIPCALL_H
#define TWILIOSIPCALL_H

#ifndef PJ_IS_LITTLE_ENDIAN
#define PJ_IS_LITTLE_ENDIAN 1
#endif

#ifndef PJ_IS_BIG_ENDIAN
#define PJ_IS_BIG_ENDIAN 0
#endif

#include <pjsua-lib/pjsua.h>

#include "talk/base/sigslot.h"
#include "twiliosdk.h"

namespace twiliosdk {

class TwilioSipAccount;

/**
 * Sip Call Observer with callback methods
 */
class TwilioSipCallObserverInterface : public sigslot::has_slots<> {
public:
    virtual void onSipCallStateChanged(const TwilioSdkCallState state) = 0;

    virtual void onSipInfoMethodReceived(const std::string& data) = 0;

    virtual void onSipPrackMethodReceived() = 0;

    virtual ~TwilioSipCallObserverInterface() {}
};

/**
 * Sip Call management
 */
class TwilioSipCall {
public:
    TwilioSipCall(TwilioSipAccount* account, int call_id = PJSUA_INVALID_ID);
    virtual ~TwilioSipCall();

    /**
     * Get TwilioSipCall by pjsip id
     *
     * @param [in] pjsip call id
     * @return TwilioSipCall instance
     */
    static TwilioSipCall* get(int id);

    /**
     * Subscribe this object event listener
     *
     * @param [in] event listener to subscribe
     */
    void subscribe(TwilioSipCallObserverInterface* listener);

    /**
     * Initiate sip call
     *
     * @param [in] call initiation params
     * @param [in] initiator side SDP offer
     * @return true if suceeded
     */
    bool call(TwilioSdkCallParams& params, std::string& offer);

    /**
     * Send re-INVITE only if INVITE session has not been disconnected
     */
    void reInvite(std::string& offer);

    /**
     * Send SIP INFO request with the specified data (as ICE candidate)
     * only if INVITE session has not been disconnected.
     *
     * @param [in] data Data to be send
     */
    void info(std::string& data);

    /**
     * Answer incoming sip call
     *
     * @param [in] answer status code, 200 or 183
     * @param [in] receiver side SDP offer
     */
    void answer(const int code, std::string& answer);

    /**
     * Hangup sip call
     */
    void hangup();

    /**
     * Reject incoming sip call
     */
    void reject();

    /**
     * Checked whether call is active
     *
     * @return true if active
     */
    bool isActive() const;

    /**
     * Get extra call details
     *
     * @return pjsua_call_info struct
     */
    pjsua_call_info getDetails() const;

    /**
     * Get pjsip call id
     *
     * @return pjsip call id
     */
    int getId() const;

    /**
     * Get remote username for this call
     *
     * @return remote username
     */
    std::string remoteUser() const;

    /**
     * Get remote SDP offer for this call
     *
     * @return remote SDP offer
     */
    std::string remoteOffer() const;

    /**
     * Set the local offer to be used in outgoing sdp negotiation
     *
     * @param [in] local offer
     */
    void setLocalOffer(std::string& offer);

    sigslot::signal1<const TwilioSdkCallState> SignalStateChanged;
    sigslot::signal1<const std::string&> SignalInfoReceived;
    sigslot::signal0<> SignalPrackReceived;

    //TODO: to be removed once moved to in-dialog INFO
    static pj_bool_t onInfoRequest(pjsip_rx_data *rdata);

private:
    friend class TwilioSipEndpoint;
    friend class TwilioSipAccount;

    static void onCallState(pjsua_call_id call_id,
                            pjsip_event *e);

    static void onCallSdpCreated(pjsua_call_id call_id,
                                 pjmedia_sdp_session *sdp,
                                 pj_pool_t *pool,
                                 const pjmedia_sdp_session *rem_sdp);

    static void onCallSdpUpdate(pjsua_call_id call_id,
                                const pjmedia_sdp_session *offer,
                                void *reserved,
                                pjsip_status_code *code,
                                pjsua_call_setting *opt);

    static void onCallRequest(pjsua_call_id call_id,
                              pjsip_transaction *tsx,
                              pjsip_event *e);

    static bool onCallInfoRequest(pjsua_call_id call_id,
                                  pjsip_rx_data *rdata);

    static bool onCallPrackRequest(pjsua_call_id call_id,
                                   pjsip_rx_data *rdata);
private:
    int call_id_;

    pjsua_call_info* call_info_;
    TwilioSipAccount* account_;
    std::string local_offer_;
    std::string remote_offer_;
    bool answered_initially_;

    //TODO: to be removed once moved to in-dialog INFO
    static int current_call_id_;
};

}  // namespace twiliosdk

#endif  // TWILIOSIPCALL_H
