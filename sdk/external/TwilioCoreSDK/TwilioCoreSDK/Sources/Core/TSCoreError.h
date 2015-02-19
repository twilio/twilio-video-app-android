//
//  TSCoreError.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/06/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_ERRROR_H
#define TSC_ERRROR_H

#include <string>

namespace twiliosdk {

// ERROR DOMAINS
extern const char* kTSCoreSDKErrorDomain;

typedef enum _TSCoreErrorCode {
    kTSCoreSuccessResultCode = 0,
    
    kTSCErrorGeneric = 99,
    kTSCErrorInvalidAuthData = 100,
    kTSCErrorInvalidEndpoint = 101,
    kTSCErrorInvalidSIPAccount = 102,
    kTSCErrorEndpointRegistration = 103,
    kTSCErrorEndpointUnregistration = 104,
    kTSCErrorInvalidSession = 105,
    kTSCErrorSessionParticipantNotAvailable = 106,
    kTSCErrorSessionRejected = 107,
    kTSCErrorSessionIgnored = 108,
    kTSCErrorSessionFailed = 109,
    kTSCErrorSessionTerminated = 110,
    kTSCErrorPeerConnectFailed = 111,
    
    kTSCErrorSessionCreationFailed = 204,
    kTSCErrorSessionDescriptionCreationFailed = 205,
    kTSCErrorSessionDescriptionSetupFailed = 206
    
} TSCoreErrorCode;

class TSCError
{
public:
    TSCError(const std::string& errorDomain, int errorCode, const std::string& errorMessage = "");
    virtual ~TSCError();
    
    int getCode() const;
    const std::string& getDomain() const;
    const std::string& getMessage() const;
    
private:
    TSCError();
    TSCError(const TSCError&);
    TSCError& operator=(TSCError&);
    
    std::string m_domain;
    int m_code;
    std::string m_message;
};
    
    
}

#endif //TSC_ERRROR_H
