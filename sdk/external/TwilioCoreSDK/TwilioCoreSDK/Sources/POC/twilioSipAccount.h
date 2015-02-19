#ifndef TWILIOSIPACCOUNT_H
#define TWILIOSIPACCOUNT_H

#ifdef stricmp
#undef stricmp
#endif

#ifdef strnicmp
#undef strnicmp
#endif

#ifndef PJ_IS_LITTLE_ENDIAN
#define PJ_IS_LITTLE_ENDIAN 1
#endif

#ifndef PJ_IS_BIG_ENDIAN
#define PJ_IS_BIG_ENDIAN 0
#endif

#include <pjsua-lib/pjsua.h>
#include <pjsip/sip_msg.h>

#include "talk/base/sigslot.h"
#include "twilioConstants.h"
#include "twiliosdk.h"

namespace twiliosdk {

/**
 * Sip Account Observer with callback methods
 */
class TwilioSipAccountObserverInterface : public sigslot::has_slots<> {
public:

    virtual void onSipAccountIncomingCall(const int call_id) = 0;
    virtual void onSipAccountStateChanged(const TwilioSdkInitState state) = 0;
    virtual void onSipAccountRemoteTrickleSupported(const bool supported) = 0;

    virtual ~TwilioSipAccountObserverInterface() {}
};

/**
 * Sip Account management
 */
class TwilioSipAccount {
public:

    TwilioSipAccount(bool useTLS);
    virtual ~TwilioSipAccount();

    /**
     * Get TwilioSipAccount by pjsip id
     *
     * @param [in] pjsip account id
     * @return TwilioSipAccount instance
     */
    static TwilioSipAccount* get(pjsua_acc_id id);

    /**
     * Subscribe this object event listener
     *
     * @param [in] event listener to subscribe
     */
    void subscribe(TwilioSipAccountObserverInterface* listener);

    /**
     * Initialize TwilioSipAccount & register
     *
     * @param [in] initialization params
     * @return true if succeded
     */
    bool init(TwilioSdkInitParams& params);

    /**
     * Send SIP OPTIONS request to check capabilities (as Trickle ICE)
     *
     * @param [in] to URL to put in To header
     */
    void options(TwilioSdkInitParams& params, const std::string& to);

    /**
     * Get pjsip account id
     *
     * @return pjsip account id
     */
    int getId() const;

    /**
     * Helper method to build sip uri from username.
     * Returns "name" <sip:name@domain>
     *
     * @param [in] sip username
     * @return sip uri
     */
    std::string buildUri(const std::string& name);
    std::string transportOption();
    int portOption(); 

    /**
     * Helper method to build extra headers for all sip methods
     *
     * @return sip headers vector
     */
    pjsip_hdr* getExtraHeaders();

    sigslot::signal1<const TwilioSdkInitState> SignalStateChanged;
    sigslot::signal1<const int> SignalIncomingCall;
    sigslot::signal1<const bool> SignalRemoteTrickleSupported;

    static pj_bool_t onOptionsResponse(pjsip_rx_data *rdata);

private:
    friend class TwilioSipEndpoint;
    static void onIncomingCall(pjsua_acc_id acc_id,
                               pjsua_call_id call_id,
                               pjsip_rx_data *rdata);
    static void onRegStarted(pjsua_acc_id acc_id,
                             pj_bool_t renew);
    static void onRegState(pjsua_acc_id acc_id,
                           pjsua_reg_info *info);


    pjsua_acc_id id_;
    std::string acc_sid_;
    std::string domain_;
    TwilioSipTransportType sip_transport_type_;
    pjsip_hdr extra_headers_;
};

}  // namespace twiliosdk

#endif  // TWILIOSIPACCOUNT_H
