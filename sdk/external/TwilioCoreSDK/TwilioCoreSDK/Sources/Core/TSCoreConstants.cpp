//
//  TSCoreConstants.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/05/15.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#include "TSCoreConstants.h"

namespace twiliosdk {

// configuration keys
const char* kTSCDomainKey = "domain";
const char* kTSCRegistrarKey = "registrar";
const char* kTSCAliasNameKey = "alias-name";
const char* kTSCUserNameKey = "user-name";
const char* kTSCPasswordKey = "password";
const char* kTSCTokenKey = "capability-token";
const char* kTSCAccountSidKey = "account-sid";
const char* kTSCSIPTransportTypeKey = "sip-transport-type";
const char* kTSCSIPTransportPortKey = "sip-transport-port";
const char* KTSCSIPClientVersionKey = "sip-client-version";
const char* KTSCSIPUserAgentKey = "sip-user-agent";

const char* kTSCStunURLKey = "stun-url";
const char* kTSCTurnURLKey = "turn-url";

const char* kTSCSIPHeaderUserAgent = "User-Agent";
// Standard Twilio SIP header key values
const char* kTSCSIPHeaderCallSid = "X-Twilio-CallSid";
const char* kTSCSIPHeaderUsername = "X-Twilio-Username";
const char* kTSCSIPHeaderPassword = "X-Twilio-Password";
const char* kTSCSIPHeaderParams = "X-Twilio-Params";
const char* kTSCSIPHeaderToken = "X-Twilio-Token";
const char* kTSCSIPHeaderClient = "X-Twilio-Client";
const char* kTSCSIPHeaderAccountSid = "X-Twilio-AccountSid";

// Client GLL SIP header constants
const char* kTSCSIPHeaderClientVersion = "X-Twilio-ClientVersion";
 
// Constant to identify invalid Id for all sorts of Ids
const int kTSCInvalidId = -1; //PJSUA_INVALID_ID
    
// Screen sharing device name
const char* kTSCScreenShareDeviceName = "Screen";
}
