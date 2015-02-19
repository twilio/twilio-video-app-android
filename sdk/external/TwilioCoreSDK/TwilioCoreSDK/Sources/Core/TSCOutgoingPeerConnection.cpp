//
//  TSCOutgoingPeerConnection.cpp
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCOutgoingPeerConnection.h"
#include "TSCOutgoingPeerConnectionImpl.h"

namespace twiliosdk {
    
    TSCOutgoingPeerConnection::TSCOutgoingPeerConnection(const TSCOptions& options,
                                                         TSCPeerConnectionObserverObjectRef observer):
    TSCPeerConnection(new TSCOutgoingPeerConnectionImpl(options, observer))
    {
    }
} // namespace twiliosdk

