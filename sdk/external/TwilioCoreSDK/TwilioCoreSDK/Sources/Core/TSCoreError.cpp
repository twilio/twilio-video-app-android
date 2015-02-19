//
//  TSCoreError.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/06/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCoreError.h"

namespace twiliosdk {

// ERROR DOMAINS
const char* kTSCoreSDKErrorDomain = "signal.coresdk.domain.error";

// ERROR MESSAGES
namespace {

struct TSCoreMessageDescription {
    int code;
    const char* message;
};
    
const TSCoreMessageDescription s_error_descriptions[] = {
    {kTSCErrorGeneric, "Unknown error."},
    {kTSCErrorInvalidAuthData, "Auth data is invalid."},
    {kTSCErrorInvalidEndpoint, "Endpoint not initialized."},
    {kTSCErrorInvalidSIPAccount, "SIP account is invalid."},
    {kTSCErrorEndpointRegistration, "Failed to register endpoint."},
    {kTSCErrorEndpointUnregistration, "Failed to unregister endpoint."},
    {kTSCErrorInvalidSession, "Session not initialized."},
    {kTSCErrorSessionParticipantNotAvailable, "Participant is unavailable at the moment."},
    {kTSCErrorSessionRejected, "Participant rejects the call."},
    {kTSCErrorSessionIgnored, "Participant is busy."},
    {kTSCErrorSessionFailed, "Call failed."},
    {kTSCErrorPeerConnectFailed, "Cannot establish connection with participant."},
    {kTSCErrorSessionCreationFailed, "Cannot create session."},
    {kTSCErrorSessionDescriptionCreationFailed, "Cannot create session description."},
    {kTSCErrorSessionDescriptionSetupFailed, "Cannot set session description."}
};
    
}
    
#pragma mark-
    
TSCError::TSCError(const std::string& errorDomain, int errorCode, const std::string& errorMessage)
{
    m_domain = errorDomain;
    m_code = errorCode;
    m_message = errorMessage;
    if(m_message.empty())
    {
        size_t i = 0;
        for( i = 0; i < sizeof(s_error_descriptions) / sizeof(TSCoreMessageDescription); i++)
        {
            if(s_error_descriptions[i].code == errorCode)
            {
                m_message = s_error_descriptions[i].message;
                break;
            }
        }
    }
}

TSCError::~TSCError()
{
}

#pragma mark-

int
TSCError::getCode() const
{
    return m_code;
}

const std::string&
TSCError::getDomain() const
{
    return m_domain;
}

const std::string&
TSCError::getMessage() const
{
    return m_message;
}

}