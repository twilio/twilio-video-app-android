//
//  TSCIdentityService.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/12/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_IDENTITY_SERVICE_H
#define TSC_IDENTITY_SERVICE_H

#include "TSCoreSDKTypes.h"
#include "talk/app/webrtc/peerconnectioninterface.h"

namespace twiliosdk {

class TSCIdentityService: public webrtc::DTLSIdentityServiceInterface
{
public:
    TSCIdentityService();
    virtual ~TSCIdentityService();
    
    bool RequestIdentity(const std::string &identity_name,
                         const std::string &common_name,
                         webrtc::DTLSIdentityRequestObserver* observer);
private:
    
    class TImpl;
    talk_base::scoped_ptr<TImpl> m_impl;
};

}

#endif  // TSC_IDENTITY_SERVICE_H
