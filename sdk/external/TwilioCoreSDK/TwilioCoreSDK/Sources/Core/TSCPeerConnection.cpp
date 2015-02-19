//
//  TSCPeerConnection.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/12/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCPeerConnection.h"
#include "TSCPeerConnectionImpl.h"
#include "TSCLogger.h"

namespace twiliosdk {
    
TSCPeerConnection::TSCPeerConnection(TSCPeerConnectionImpl* impl)
{
    m_impl.reset(impl);
}

TSCPeerConnection::~TSCPeerConnection()
{
}

#pragma mark-
    
TSCPeerConnection::TSCIceGatheringState
TSCPeerConnection::getIceGatheringState() const
{
    return m_impl->getIceGatheringState();
}
    
TSCPeerConnection::TSCSignalingState
TSCPeerConnection::getSignalingState() const
{
    return m_impl->getSignalingState();
}

TSCPeerConnection::TSCIceConnectionState
TSCPeerConnection::getIceConnectionState() const
{
    return m_impl->getIceConnectionState();
}
    
#pragma mark-

void
TSCPeerConnection::open()
{
    return m_impl->open();
}

void
TSCPeerConnection::close()
{
    return m_impl->close();
}

#pragma mark-

std::string
TSCPeerConnection::getLocalDescription() const
{
    return m_impl->getLocalDescription();
}
    
void
TSCPeerConnection::createLocalSessionDescription()
{
    m_impl->createLocalSessionDescription();
}
    
std::string
TSCPeerConnection::getRemoteDescription() const
{
    return m_impl->getRemoteDescription();
}
    
void
TSCPeerConnection::setRemoteDescription(const std::string& sdp)
{
    m_impl->setRemoteDescription(sdp);
}
    
} // namespace twiliosdk
