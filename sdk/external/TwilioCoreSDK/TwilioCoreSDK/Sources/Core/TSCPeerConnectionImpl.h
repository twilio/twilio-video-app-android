//
//  TSCPeerConnection.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/12/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_PEER_CONNECTION_IMPL_H
#define TSC_PEER_CONNECTION_IMPL_H

#include "talk/app/webrtc/jsep.h"
#include "talk/app/webrtc/peerconnectioninterface.h"
#include "talk/app/webrtc/mediastreaminterface.h"
#include "talk/app/webrtc/videosourceinterface.h"

#include "TSCPeerConnection.h"
#include "TSCPeerConnectionObserver.h"
#include "TSCSessionDescriptionObservers.h"
#include "TSCConstraintsImpl.h"

namespace twiliosdk {

class TSCPeerConnectionImpl : public webrtc::PeerConnectionObserver
{
public:
    
    TSCPeerConnectionImpl(const TSCOptions& options, TSCPeerConnectionObserverObjectRef observer);
    virtual ~TSCPeerConnectionImpl();

    TSCPeerConnection::TSCIceGatheringState getIceGatheringState() const;
    TSCPeerConnection::TSCSignalingState getSignalingState() const;
    TSCPeerConnection::TSCIceConnectionState getIceConnectionState() const;
    
    virtual void open();
    virtual void close();

    std::string getLocalDescription() const;
    virtual void createLocalSessionDescription();
    
    std::string getRemoteDescription() const;
    virtual void setRemoteDescription(const std::string& sdp);

    // PeerConnection Observer
    virtual void OnError();
    virtual void OnSignalingChange(webrtc::PeerConnectionInterface::SignalingState new_state);
    virtual void OnIceGatheringChange(webrtc::PeerConnectionInterface::IceGatheringState new_state);
    virtual void OnIceConnectionChange(webrtc::PeerConnectionInterface::IceConnectionState new_state);
    virtual void OnAddStream(webrtc::MediaStreamInterface* stream);
    virtual void didAddStream(webrtc::MediaStreamInterface* stream, TSCMediaStreamOrigin origin);
    virtual void OnRemoveStream(webrtc::MediaStreamInterface* stream);
    virtual void didRemoveStream(webrtc::MediaStreamInterface* stream, TSCMediaStreamOrigin origin);
    virtual void OnRenegotiationNeeded();
    virtual void OnIceCandidate(const webrtc::IceCandidateInterface* candidate);
    virtual void onCreateSessionLocalDescription(webrtc::SessionDescriptionInterface* desc,
                                                 const TSCErrorObjectRef& error);
    virtual void onSetSessionLocalDescription(const TSCErrorObjectRef& error);
    virtual void onSetSessionRemoteDescription(const TSCErrorObjectRef& error);

private:
    TSCPeerConnectionImpl();
    TSCPeerConnectionImpl(const TSCPeerConnectionImpl&);
    TSCPeerConnectionImpl& operator=(TSCPeerConnectionImpl&);
    
    void setupICEServers();

protected:
    TSCOptions m_options;
    TSCPeerConnectionObserverObjectRef m_observer;
    
    webrtc::PeerConnectionInterface::IceServers m_ice_servers;
    talk_base::scoped_refptr<webrtc::PeerConnectionFactoryInterface> m_peer_connection_factory;
    talk_base::scoped_ptr<talk_base::Thread> m_peer_connection_signaling_thread;
    talk_base::scoped_ptr<talk_base::Thread> m_peer_connection_worker_thread;
    talk_base::scoped_refptr<webrtc::PeerConnectionInterface> m_peer_connection;
    TSCPeerConnection::TSCIceGatheringState m_current_ice_gathering_state;
    
    talk_base::scoped_refptr<webrtc::AudioTrackInterface> m_audio_track;
    talk_base::scoped_refptr<webrtc::VideoTrackInterface> m_video_track;
    talk_base::scoped_refptr<webrtc::MediaStreamInterface> m_local_stream;
    
    TSCAudioInputControllerImplRef m_audio_input_controller;
    TSCVideoCaptureControllerImplRef m_video_capture_controller;
    
    std::vector<talk_base::scoped_refptr<webrtc::MediaStreamInterface>> m_remote_streams;
    
    TSCConstraints m_peer_connection_constraints;
    TSCConstraints m_sdp_constraints;
    TSCConstraints m_video_constraints;
};
    
}  // namespace twiliosdk

#endif  // TSC_PEER_CONNECTION_IMPL_H
