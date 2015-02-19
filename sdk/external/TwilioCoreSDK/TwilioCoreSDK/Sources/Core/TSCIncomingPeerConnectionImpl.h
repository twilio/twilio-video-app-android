//
//  TSCIncomingPeerConnectionImpl.h
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_INCOMING_PEER_CONNECTION_IMPL_H
#define TSC_INCOMING_PEER_CONNECTION_IMPL_H

#include "TSCPeerConnectionImpl.h"

namespace twiliosdk {
    
class TSCIncomingPeerConnectionImpl : public TSCPeerConnectionImpl
{
public:
    TSCIncomingPeerConnectionImpl(const TSCOptions& options, TSCPeerConnectionObserverObjectRef observer);
    
    void createLocalSessionDescription();
    void setRemoteDescription(const std::string& sdp);

private:
    TSCIncomingPeerConnectionImpl();
    TSCIncomingPeerConnectionImpl(const TSCIncomingPeerConnection&);
    TSCIncomingPeerConnectionImpl& operator=(TSCIncomingPeerConnection&);
};
    
}  // namespace twiliosdk

#endif  // TSC_INCOMING_PEER_CONNECTION_IMPL_H

