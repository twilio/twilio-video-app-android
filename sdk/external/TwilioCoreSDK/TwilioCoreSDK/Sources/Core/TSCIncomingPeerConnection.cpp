//
//  TSCIncomingPeerConnection.cpp
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCIncomingPeerConnection.h"
#include "TSCIncomingPeerConnectionImpl.h"

namespace twiliosdk {
    
TSCIncomingPeerConnection::TSCIncomingPeerConnection(const TSCOptions& options,
                                                     TSCPeerConnectionObserverObjectRef observer):
    TSCPeerConnection(new TSCIncomingPeerConnectionImpl(options, observer))
{
}
} // namespace twiliosdk

