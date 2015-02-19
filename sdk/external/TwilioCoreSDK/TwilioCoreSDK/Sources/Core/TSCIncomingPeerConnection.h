//
//  TSCIncomingPeerConnection.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_INCOMING_PEER_CONNECTION_H
#define TSC_INCOMING_PEER_CONNECTION_H

#include "TSCoreSDKTypes.h"
#include "TSCPeerConnection.h"

namespace twiliosdk {
    
class TSCIncomingPeerConnection : public TSCPeerConnection
{
public:
    TSCIncomingPeerConnection(const TSCOptions& options, TSCPeerConnectionObserverObjectRef observer);
private:
    TSCIncomingPeerConnection();
    TSCIncomingPeerConnection(const TSCPeerConnection&);
    TSCIncomingPeerConnection& operator=(TSCIncomingPeerConnection&);
};
    
}  // namespace twiliosdk

#endif  // TSC_INCOMING_PEER_CONNECTION_H

