//
//  TSCOutgoingPeerConnectionImpl.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_OUTGOING_PEER_CONNECTION_IMPL_H
#define TSC_OUTGOING_PEER_CONNECTION_IMPL_H

#include "TSCPeerConnectionImpl.h"

namespace twiliosdk {
    
class TSCOutgoingPeerConnectionImpl : public TSCPeerConnectionImpl
{
public:
    TSCOutgoingPeerConnectionImpl(const TSCOptions& options, TSCPeerConnectionObserverObjectRef observer);
    
    void createLocalSessionDescription();
    void setRemoteDescription(const std::string& sdp);

private:
    TSCOutgoingPeerConnectionImpl();
    TSCOutgoingPeerConnectionImpl(const TSCOutgoingPeerConnection&);
    TSCOutgoingPeerConnectionImpl& operator=(TSCOutgoingPeerConnection&);
};
    
}  // namespace twiliosdk

#endif  // TSC_OUTGOING_PEER_CONNECTION_IMPL_H

