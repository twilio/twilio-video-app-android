//
//  TSCPeerConnection.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/12/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCPeerConnectionImpl.h"
#include "TSCoreConstants.h"
#include "TSCDeviceManager.h"
#include "TSCIdentityService.h"
#include "TSCoreConstants.h"
#include "TSCVideoCapturer.h"
#include "TSCLogger.h"
#include "TSCSIPUtils.h"
#include "TSCAudioInputControllerImpl.h"
#include "TSCVideoCaptureControllerImpl.h"
#include "TSCScreenCapturer.h"
#include "TSCScreenCaptureProvider.h"
#include "TSCVideoCodecManager.h"
#include "ITSCVideoCodec.h"
#include "TSCThreadMonitor.h"

namespace twiliosdk {

TSCPeerConnectionImpl::TSCPeerConnectionImpl(const TSCOptions& options, TSCPeerConnectionObserverObjectRef observer)
{
    m_options = options;
    setupICEServers();
    
    m_observer = observer;
    
    TSCVideoCodecManager codec_manager;
    std::vector<TSCVideoCodecRef> extra_video_codecs = codec_manager.getVideoCodecs();
    if (extra_video_codecs.size() == 0) {
         // no extra codecs provided, so use default creation
         m_peer_connection_factory = webrtc::CreatePeerConnectionFactory();
    } else {
        // utilize first codec so far
        m_peer_connection_signaling_thread.reset(new talk_base::Thread());
        m_peer_connection_signaling_thread->Start(new TSCThreadMonitor("TSCPeerConnectionImpl_signaling", this));
        
        m_peer_connection_worker_thread.reset(new talk_base::Thread());
        m_peer_connection_worker_thread->Start(new TSCThreadMonitor("TSCPeerConnectionImpl_worker", this));
        //TODO: check whether its possible to optimize threads count
        m_peer_connection_factory = webrtc::CreatePeerConnectionFactory(m_peer_connection_signaling_thread.get(),
                                                                        m_peer_connection_worker_thread.get(),
                                                                        nullptr,
                                                                        extra_video_codecs[0]->createVideoEncoderFactory(),
                                                                        extra_video_codecs[0]->createVideoDecoderFactory());
    }
   
    m_peer_connection_constraints.SetMandatory(TSCConstraints::kEnableDtlsSrtp, true);
    m_current_ice_gathering_state = TSCPeerConnection::kTSCIceGatheringStateNA;
    
    m_sdp_constraints.SetMandatory(TSCConstraints::kOfferToReceiveAudio, false);
    m_sdp_constraints.SetMandatory(TSCConstraints::kOfferToReceiveVideo, false);
}
    
TSCPeerConnectionImpl::~TSCPeerConnectionImpl()
{
    m_observer = nullptr;
}

TSCPeerConnection::TSCIceGatheringState
TSCPeerConnectionImpl::getIceGatheringState() const
{
    TSCPeerConnection::TSCIceGatheringState state = TSCPeerConnection::kTSCIceGatheringStateNA;
    if(m_peer_connection.get() == nullptr)
       return state;
    switch(m_peer_connection->ice_gathering_state())
    {
        case webrtc::PeerConnectionInterface::kIceGatheringNew:
            state = TSCPeerConnection::kTSCIceGatheringNew;
            break;
        case webrtc::PeerConnectionInterface::kIceGatheringGathering:
            state = TSCPeerConnection::kTSCIceGatheringGathering;
            break;
        case webrtc::PeerConnectionInterface::kIceGatheringComplete:
            state = TSCPeerConnection::kTSCIceGatheringComplete;
            break;
    }
    return state;
}
    
TSCPeerConnection::TSCSignalingState
TSCPeerConnectionImpl::getSignalingState() const
{
    TSCPeerConnection::TSCSignalingState state = TSCPeerConnection::kTSCSignalingStateNA;
    if(m_peer_connection.get() == nullptr)
        return state;
    switch(m_peer_connection->signaling_state())
    {
        case webrtc::PeerConnectionInterface::kStable:
            state = TSCPeerConnection::kTSCStable;
            break;
        case webrtc::PeerConnectionInterface::kHaveLocalOffer:
            state = TSCPeerConnection::kTSCHaveLocalOffer;
            break;
        case webrtc::PeerConnectionInterface::kHaveLocalPrAnswer:
            state = TSCPeerConnection::kTSCHaveLocalPrAnswer;
            break;
        case webrtc::PeerConnectionInterface::kHaveRemoteOffer:
            state = TSCPeerConnection::kTSCHaveRemoteOffer;
            break;
        case webrtc::PeerConnectionInterface::kHaveRemotePrAnswer:
            state = TSCPeerConnection::kTSCHaveRemotePrAnswer;
            break;
        case webrtc::PeerConnectionInterface::kClosed:
            state = TSCPeerConnection::kTSCClosed;
            break;
    }
    return state;
}

TSCPeerConnection::TSCIceConnectionState
TSCPeerConnectionImpl::getIceConnectionState() const
{
    TSCPeerConnection::TSCIceConnectionState state = TSCPeerConnection::kTSCIceConnectionStateNA;
    if(m_peer_connection.get() == nullptr)
       return state;
    switch(m_peer_connection->ice_connection_state())
    {
        case webrtc::PeerConnectionInterface::kIceConnectionNew:
            state = TSCPeerConnection::kTSCIceConnectionNew;
            break;
        case webrtc::PeerConnectionInterface::kIceConnectionChecking:
            state = TSCPeerConnection::kTSCIceConnectionChecking;
            break;
        case webrtc::PeerConnectionInterface::kIceConnectionConnected:
            state = TSCPeerConnection::kTSCIceConnectionConnected;
            break;
        case webrtc::PeerConnectionInterface::kIceConnectionCompleted:
            state = TSCPeerConnection::kTSCIceConnectionCompleted;
            break;
        case webrtc::PeerConnectionInterface::kIceConnectionFailed:
            state = TSCPeerConnection::kTSCIceConnectionFailed;
            break;
        case webrtc::PeerConnectionInterface::kIceConnectionDisconnected:
            state = TSCPeerConnection::kTSCIceConnectionDisconnected;
            break;
        case webrtc::PeerConnectionInterface::kIceConnectionClosed:
            state = TSCPeerConnection::kTSCIceConnectionClosed;
            break;
    }
    return state;
}
    
#pragma mark-

// PeerConnection Observer
void
TSCPeerConnectionImpl::OnError()
{
    TS_CORE_LOG_DEBUG("PeerConnection: OnError()!");
}

void
TSCPeerConnectionImpl::OnSignalingChange(webrtc::PeerConnectionInterface::SignalingState new_state)
{
    TS_CORE_LOG_DEBUG("New signaling state: %d", new_state);
}

void
TSCPeerConnectionImpl::OnIceGatheringChange(webrtc::PeerConnectionInterface::IceGatheringState new_state)
{
    TSCPeerConnection::TSCIceGatheringState state = getIceGatheringState();
    if (m_current_ice_gathering_state == state) {
        // Do not notify about already notified state
        return;
    }
    m_current_ice_gathering_state = state;
    switch(new_state)
    {
        case webrtc::PeerConnectionInterface::kIceGatheringGathering:
        {
            TS_CORE_LOG_DEBUG("Ice Gathering...");
        }
        break;
        case webrtc::PeerConnectionInterface::kIceGatheringComplete:
        {
            TS_CORE_LOG_DEBUG("Ice Gathering Complete!");
            if(m_observer.get())
               m_observer->onIceGatheringComplete();
        }
        break;
        default:{}
    }
}

void
TSCPeerConnectionImpl::OnIceConnectionChange(webrtc::PeerConnectionInterface::IceConnectionState new_state)
{
    switch(new_state)
    {
        case webrtc::PeerConnectionInterface::kIceConnectionChecking:
        {
            TS_CORE_LOG_DEBUG("PeerConnection: checking connection...");
        }
        break;
        case webrtc::PeerConnectionInterface::kIceConnectionConnected:
        {
            TS_CORE_LOG_DEBUG("PeerConnection: connection established.");
            if(m_observer.get())
               m_observer->onPeerConnectionConnected();
        }
        break;
        case webrtc::PeerConnectionInterface::kIceConnectionCompleted:
        {
            TS_CORE_LOG_DEBUG("PeerConnection: connection completed.");
        }
        break;
        case webrtc::PeerConnectionInterface::kIceConnectionFailed:
        {
            TS_CORE_LOG_DEBUG("PeerConnection: connection failed.");
            if(m_observer.get())
            {
                TSCErrorObjectRef error = new TSCErrorObject(kTSCoreSDKErrorDomain,
                                                             kTSCErrorPeerConnectFailed);
                m_observer->onPeerConnectionConnected(error);
            }
        }
        break;
        case webrtc::PeerConnectionInterface::kIceConnectionDisconnected:
        {
            TS_CORE_LOG_DEBUG("PeerConnection: disconnected.");
            if(m_observer.get())
               m_observer->onPeerConnectionDisconnected(kTSCDisconnectReasonWillReconnectPeer);
        }
        break;
        case webrtc::PeerConnectionInterface::kIceConnectionClosed:
        {
            TS_CORE_LOG_DEBUG("PeerConnection: closed.");
        }
        break;
        default:{}
    }
}

void
TSCPeerConnectionImpl::OnAddStream(webrtc::MediaStreamInterface* stream)
{
    m_remote_streams.push_back(stream);
    didAddStream(stream, kTSCMediaStreamRemote);
}

void
TSCPeerConnectionImpl::didAddStream(webrtc::MediaStreamInterface* stream, TSCMediaStreamOrigin origin)
{
    if(m_observer.get())
       m_observer->onAddStream(stream, origin);
}

void
TSCPeerConnectionImpl::OnRemoveStream(webrtc::MediaStreamInterface* stream)
{
    for(auto it = m_remote_streams.begin(); it != m_remote_streams.end(); ++it) {
        if(it->get() == stream)
        {
            m_remote_streams.erase(it);
            break;
        }
    }
    didRemoveStream(stream, kTSCMediaStreamRemote);
}

void
TSCPeerConnectionImpl::didRemoveStream(webrtc::MediaStreamInterface* stream, TSCMediaStreamOrigin origin)
{
    if(m_observer.get())
        m_observer->onRemoveStream(stream, origin);
}

void
TSCPeerConnectionImpl::OnRenegotiationNeeded()
{
    // TODO: Invoked after AddStream()/RemoveStream() or ICE restart
    TS_CORE_LOG_DEBUG("PeerConnection: renegotiation needed!");
}

void
TSCPeerConnectionImpl::OnIceCandidate(const webrtc::IceCandidateInterface* candidate)
{
    TS_CORE_LOG_DEBUG("Got local ICE candidate %s, id: %d",
                      candidate->sdp_mid().c_str(),
                      candidate->sdp_mline_index());
}

void
TSCPeerConnectionImpl::onCreateSessionLocalDescription(webrtc::SessionDescriptionInterface* desc,
                                             const TSCErrorObjectRef& error)
{
    talk_base::scoped_refptr<TSCSetSessionLocalDescriptionObserver<TSCPeerConnectionImpl>>
    observer(new talk_base::RefCountedObject<TSCSetSessionLocalDescriptionObserver<TSCPeerConnectionImpl>>(this));
    
    std::string offer;
    desc->ToString(&offer);
    TS_CORE_LOG_DEBUG("Created local SDP: %s", offer.c_str());
    
    m_peer_connection->SetLocalDescription(observer, desc);
}

void
TSCPeerConnectionImpl::onSetSessionLocalDescription(const TSCErrorObjectRef& error)
{
    if(m_observer.get())
       m_observer->onSetSessionLocalDescription(error);
}

void
TSCPeerConnectionImpl::onSetSessionRemoteDescription(const TSCErrorObjectRef& error)
{
    if(m_observer.get())
        m_observer->onSetSessionRemoteDescription(error);
}
    
void
TSCPeerConnectionImpl::setupICEServers()
{
    webrtc::PeerConnectionInterface::IceServer server;
    if (m_options.find(kTSCStunURLKey) != m_options.end())
    {
        server.uri = m_options[kTSCStunURLKey];
        m_ice_servers.push_back(server);
    }
    if (m_options.find(kTSCTurnURLKey) != m_options.end())
    {
        server.uri = m_options[kTSCTurnURLKey];
        server.username = m_options[kTSCUserNameKey];
        server.password = m_options[kTSCPasswordKey];
        m_ice_servers.push_back(server);
    }
}

#pragma mark-

void
TSCPeerConnectionImpl::open()
{
    m_peer_connection = m_peer_connection_factory->CreatePeerConnection(m_ice_servers, &m_peer_connection_constraints,
                                                                        nullptr, new TSCIdentityService(), this);
    // create the local media stream
    std::string stream_label;
    TSCSIPUtils::generateUniqueId(m_options[kTSCUserNameKey], stream_label);
    m_local_stream = m_peer_connection_factory->CreateLocalMediaStream(stream_label);
    
    // create the audio track
    std::string audio_label;
    TSCSIPUtils::generateUniqueId(m_options[kTSCUserNameKey], audio_label);
    m_audio_track = m_peer_connection_factory->CreateAudioTrack(audio_label,
                                                                m_peer_connection_factory->CreateAudioSource(nullptr));
    m_local_stream->AddTrack(m_audio_track);
    
    // create the video track if possilbe
    TSCVideoCapturer* video_capturer = nullptr;
    
    TSCDeviceManagerObjectRef device_manager = new TSCDeviceManagerObject;
    TSCVideoCaptureDeviceInfo video_capture_device = device_manager->getDefaultVideoCaptureDevice();
    if (video_capture_device.isValid()) {
        video_capturer = new TSCVideoCapturer(video_capture_device.getDeviceId());
        talk_base::scoped_refptr<webrtc::VideoSourceInterface> video_source =
            m_peer_connection_factory->CreateVideoSource(video_capturer, &m_video_constraints);
        std::string video_label;
        TSCSIPUtils::generateUniqueId(m_options[kTSCUserNameKey], video_label);
        m_video_track = m_peer_connection_factory->CreateVideoTrack(video_label, video_source);
        m_local_stream->AddTrack(m_video_track);
    }
    
    // add the stream to the connection
    if (!m_peer_connection->AddStream(m_local_stream, nullptr)) {
        TS_CORE_LOG_DEBUG("WebrtcConnectionManager: Adding stream to PeerConnection failed");
    } else {
        TS_CORE_LOG_DEBUG("WebrtcConnectionManager: Added stream to PeerConnection");
        
        // attach local stream to render
        didAddStream(m_local_stream, kTSCMediaStreamLocal);
        
        // link controllers
        m_audio_input_controller = new talk_base::RefCountedObject<TSCAudioInputControllerImpl>(m_audio_track.get());
        m_video_capture_controller = new talk_base::RefCountedObject<TSCVideoCaptureControllerImpl>(m_video_track.get(), video_capturer);
        if(m_observer.get())
        {
            m_observer->onLinkAudioInputController(m_audio_input_controller.get());
            m_observer->onLinkVideoCaptureController(m_video_capture_controller.get());
        }
    }
}

void
TSCPeerConnectionImpl::close()
{
    if(m_peer_connection.get() != nullptr)
    {
        m_peer_connection->Close();
        if (m_local_stream.get() != nullptr )
        {
            // detach local stream from render
            didRemoveStream(m_local_stream, kTSCMediaStreamLocal);
            
            m_peer_connection->RemoveStream(m_local_stream.get());
            
            if (m_audio_track.get())
                m_local_stream->RemoveTrack(m_audio_track.get());
        
            if (m_video_track.get())
                m_local_stream->RemoveTrack(m_video_track.get());
            
            m_local_stream = nullptr;
        }
        
        // remove remote streams
        for(int i = 0; i < (int)m_remote_streams.size(); i++)
        {
            didRemoveStream(m_remote_streams[i], kTSCMediaStreamRemote);
        }
        m_remote_streams.clear();
        
        // unlink media controllers
        if(m_observer.get())
        {
            m_observer->onUnlinkAudioInputController(m_audio_input_controller.get());
            m_observer->onUnlinkVideoCaptureController(m_video_capture_controller.get());
            m_audio_input_controller = nullptr;
            m_video_capture_controller = nullptr;
        }
        
        m_audio_track = nullptr;
        m_video_track = nullptr;
        m_peer_connection = nullptr;
    }
}
    
#pragma mark-

std::string
TSCPeerConnectionImpl::getLocalDescription() const
{
    if(m_peer_connection.get() != nullptr)
    {
        const webrtc::SessionDescriptionInterface* local_description = m_peer_connection->local_description();
        if(local_description != nullptr)
        {
            std::string offer;
            local_description->ToString(&offer);
            return offer;
        }
    }
    return std::string("");
}
    
void
TSCPeerConnectionImpl::createLocalSessionDescription()
{
}
    
std::string
TSCPeerConnectionImpl::getRemoteDescription() const
{
    if(m_peer_connection.get() != nullptr)
    {
        const webrtc::SessionDescriptionInterface* remote_description = m_peer_connection->remote_description();
        if(remote_description != nullptr)
        {
            std::string offer;
            remote_description->ToString(&offer);
            return offer;
        }
    }
    return std::string("");
}
    
void
TSCPeerConnectionImpl::setRemoteDescription(const std::string& sdp)
{
}

#pragma mark-
    
} // namespace twiliosdk
