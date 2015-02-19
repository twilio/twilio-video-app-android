//
//  TSCIncomingPeerConnectionImpl.cpp
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/26/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCIncomingPeerConnection.h"
#include "TSCIncomingPeerConnectionImpl.h"

namespace twiliosdk {

TSCIncomingPeerConnectionImpl::TSCIncomingPeerConnectionImpl(const TSCOptions& options,
                                                             TSCPeerConnectionObserverObjectRef observer) :
    TSCPeerConnectionImpl(options, observer)
{
}
    
#pragma mark-
    
void
TSCIncomingPeerConnectionImpl::createLocalSessionDescription()
{
    TS_CORE_LOG_INFO("IncomingPeerConnection: create local answer");
    talk_base::scoped_refptr<TSCCreateSessionLocalDescriptionObserver<TSCPeerConnectionImpl>>
    observer(new talk_base::RefCountedObject<
             TSCCreateSessionLocalDescriptionObserver<TSCPeerConnectionImpl>>(this));
    m_peer_connection->CreateAnswer(observer, &m_sdp_constraints);
}

void
TSCIncomingPeerConnectionImpl::setRemoteDescription(const std::string& sdp)
{
    TS_CORE_LOG_INFO("IncomingPeerConnection: set remote offer");
    if(m_peer_connection.get() != nullptr)
    {
        talk_base::scoped_refptr<TSCSetSessionRemoteDescriptionObserver<TSCPeerConnectionImpl>>
        observer(new talk_base::RefCountedObject<TSCSetSessionRemoteDescriptionObserver<TSCPeerConnectionImpl>>(this));
        
        webrtc::SdpParseError e;
        
        webrtc::SessionDescriptionInterface *remote_description =
        webrtc::CreateSessionDescription(webrtc::SessionDescriptionInterface::kOffer, sdp, &e);
        if (!remote_description) {
            TS_CORE_LOG_INFO("IncomingPeerConnection: create sdp failed due to: %s", e.description.c_str());
            observer->OnFailure(e.description);
        } else {
            m_peer_connection->SetRemoteDescription(observer, remote_description);
        }
    }
}

} // namespace twiliosdk