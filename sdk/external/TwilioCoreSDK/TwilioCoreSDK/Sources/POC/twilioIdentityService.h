/*
 * twilioIdentityService.h
 *
 *  Created on: Aug 25, 2014
 *      Author: ykhodosh
 */

#ifndef TWILIO_IDENTITYSERVICE_H_
#define TWILIO_IDENTITYSERVICE_H_

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wunused-parameter"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif

#include "talk/base/thread.h"
#include "talk/app/webrtc/peerconnectioninterface.h"

#if defined(DARWIN) || defined(WEBRTC_IOS)
#pragma clang diagnostic pop
#pragma GCC diagnostic pop
#endif

namespace twiliosdk {

class TwilioIdentityService : public talk_base::MessageHandler,
        public webrtc::DTLSIdentityServiceInterface {

 public:
    // request data to process on a thread
    struct Request {
        Request(const std::string &cn, webrtc::DTLSIdentityRequestObserver *cb)
                : cn_(cn),
                  cb_(cb) {
        }

        std::string cn_;
        talk_base::scoped_refptr<webrtc::DTLSIdentityRequestObserver> cb_;
    };

    typedef talk_base::TypedMessageData<Request> MessageData;

    // c-tor/d-tor
    TwilioIdentityService() {
    }
    virtual ~TwilioIdentityService() {
    }

    // DTLSIdentityServiceInterface implemenation.
    virtual bool RequestIdentity(const std::string &identity_name,
                                 const std::string &common_name,
                                 webrtc::DTLSIdentityRequestObserver *observer);

 private:
    // invoked when thread is processing our message
    void OnMessage(talk_base::Message* msg);
};

}  //namespace twiliosdk

#endif  // TWILIO_IDENTITYSERVICE_H_
