//
//  TSCPeerConnection.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/12/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_PEER_CONNECTION_H
#define TSC_PEER_CONNECTION_H

#include "TSCoreSDKTypes.h"

namespace twiliosdk {

class TSCPeerConnectionImpl;

class TSCPeerConnection : public talk_base::RefCountInterface
{
public:

    typedef enum _TSCIceGatheringState {
        kTSCIceGatheringStateNA = -1,
        kTSCIceGatheringNew = 0,
        kTSCIceGatheringGathering,
        kTSCIceGatheringComplete
    } TSCIceGatheringState;
    
    typedef enum _TSCSignalingState {
        kTSCSignalingStateNA = -1,
        kTSCStable = 0,
        kTSCHaveLocalOffer,
        kTSCHaveLocalPrAnswer,
        kTSCHaveRemoteOffer,
        kTSCHaveRemotePrAnswer,
        kTSCClosed,
    } TSCSignalingState;
    
    typedef enum _TSCIceConnectionState {
        kTSCIceConnectionStateNA = -1,
        kTSCIceConnectionNew = 0,
        kTSCIceConnectionChecking,
        kTSCIceConnectionConnected,
        kTSCIceConnectionCompleted,
        kTSCIceConnectionFailed,
        kTSCIceConnectionDisconnected,
        kTSCIceConnectionClosed,
    } TSCIceConnectionState;
    
    virtual ~TSCPeerConnection();

    TSCIceGatheringState getIceGatheringState() const;
    TSCSignalingState getSignalingState() const;
    TSCIceConnectionState getIceConnectionState() const;
    
    virtual void open();
    virtual void close();

    std::string getLocalDescription() const;
    virtual void createLocalSessionDescription();
    
    std::string getRemoteDescription() const;
    virtual void setRemoteDescription(const std::string& sdp);
    
protected:
    TSCPeerConnection(TSCPeerConnectionImpl* impl);
private:
    TSCPeerConnection();
    TSCPeerConnection(const TSCPeerConnection&);
    TSCPeerConnection& operator=(TSCPeerConnection&);
  
    talk_base::scoped_ptr<TSCPeerConnectionImpl> m_impl;
};
    
}  // namespace twiliosdk

#endif  // TSC_PEER_CONNECTION_H
