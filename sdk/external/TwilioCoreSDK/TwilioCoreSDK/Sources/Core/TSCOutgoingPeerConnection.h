//
//  TSCOutgoingPeerConnection.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_OUTGOING_PEER_CONNECTION_H
#define TSC_OUTGOING_PEER_CONNECTION_H

#include "TSCoreSDKTypes.h"
#include "TSCPeerConnection.h"

namespace twiliosdk {
    
class TSCOutgoingPeerConnection : public TSCPeerConnection
{
public:
    TSCOutgoingPeerConnection(const TSCOptions& options, TSCPeerConnectionObserverObjectRef observer);
private:
    TSCOutgoingPeerConnection();
    TSCOutgoingPeerConnection(const TSCOutgoingPeerConnection&);
    TSCOutgoingPeerConnection& operator=(TSCOutgoingPeerConnection&);
};
    
}  // namespace twiliosdk

#endif  // TSC_OUTGOING_PEER_CONNECTION_H

