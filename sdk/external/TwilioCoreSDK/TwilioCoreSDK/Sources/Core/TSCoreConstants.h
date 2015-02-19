//
//  TSCoreConstants.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/05/15.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#ifndef TSC_CONSTANTS_H
#define TSC_CONSTANTS_H

namespace twiliosdk {

// configuration keys
extern const char* kTSCDomainKey;
extern const char* kTSCRegistrarKey;
extern const char* kTSCAliasNameKey;
extern const char* kTSCUserNameKey;
extern const char* kTSCPasswordKey;
extern const char* kTSCTokenKey;
extern const char* kTSCAccountSidKey;
extern const char* kTSCSIPTransportTypeKey;
extern const char* kTSCSIPTransportPortKey;
extern const char* KTSCSIPClientVersionKey;
extern const char* kTSCStunURLKey;
extern const char* kTSCTurnURLKey;
extern const char* KTSCSIPUserAgentKey;
    
extern const char* kTSCSIPHeaderUserAgent;
// Standard Twilio SIP header key values
extern const char* kTSCSIPHeaderCallSid;
extern const char* kTSCSIPHeaderUsername;
extern const char* kTSCSIPHeaderPassword;
extern const char* kTSCSIPHeaderParams;
extern const char* kTSCSIPHeaderToken;
extern const char* kTSCSIPHeaderClient;
extern const char* kTSCSIPHeaderAccountSid;

// Client GLL SIP header constants
extern const char* kTSCSIPHeaderClientVersion;
    
// Constant to identify invalid Id for all sorts of Ids
extern const int kTSCInvalidId;
    
// Screen sharing device name
extern const char* kTSCScreenShareDeviceName;
}

#endif //TSC_CONSTANTS_H
