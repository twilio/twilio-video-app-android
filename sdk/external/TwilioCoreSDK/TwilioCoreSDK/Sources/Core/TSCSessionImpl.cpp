//
//  TSCSession.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCSessionImpl.h"
#include "TSCoreError.h"
#include "TSCSessionObserver.h"
#include "TSCLogger.h"

// TODO: remove pjsip dependency from TSCPJSUA interface
#include <pj/types.h>
#include <pjsua-lib/pjsua.h>
#include "TSCPJSUA.h"
#include "TSCSIPCallContext.h"
#include "TSCOutgoingParticipantConnection.h"
#include "TSCVideoSurface.h"
#include "TSCMediaTrackInfo.h"
#include "TSCMediaStreamInfo.h"
#include "TSCMediaStreamUtils.h"
#include "TSCAudioInputController.h"
#include "TSCVideoCaptureController.h"

namespace twiliosdk {

TSCSessionImpl::TSCSessionImpl(int accountId,
      const TSCOptions& options,
      const TSCSessionObserverObjectRef& observer)
{
    m_state = kTSCSessionStateInitialized;
    m_account_id = accountId;
    m_options = options;
    m_observer = observer;
    
    m_audio_input_controller = new talk_base::RefCountedObject<TSCAudioInputController>();
    m_video_capture_controller = new talk_base::RefCountedObject<TSCVideoCaptureController>();
}

TSCSessionImpl::~TSCSessionImpl()
{
    m_observer = nullptr;
    
    m_audio_input_controller = nullptr;
    m_video_capture_controller = nullptr;
}

void
TSCSessionImpl::setParticipants(const std::vector<TSCParticipant>& participants)
{
    m_participant_connections.clear();
    for (auto &participant : participants) {
        m_participant_connections.push_back(new TSCOutgoingParticipantConnection(participant, m_options, this));
    }
}
    
const std::vector<TSCParticipant>
TSCSessionImpl::getParticipants() const
{
    std::vector<TSCParticipant> participants;
    for (auto &connection : m_participant_connections) {
        participants.push_back(connection->getParticipant());
    }
    return participants;
}

void
TSCSessionImpl::setVideoSurface(const TSCVideoSurfaceObjectRef& videoSurface)
{
    m_video_surface = videoSurface;
}

uint64
TSCSessionImpl::getId() const
{
    return (uint64)this;
}
    
void
TSCSessionImpl::setSessionObserver(const TSCSessionObserverObjectRef& observer)
{
    m_observer = observer;
}
    
void
TSCSessionImpl::start()
{
}

void
TSCSessionImpl::stop()
{
    changeState(kTSCSessionStateStopping);
    for (auto &connection: m_participant_connections) {
        connection->stop();
    }
    changeState(kTSCSessionStateStopped);
    if(m_observer.get() != nullptr) {
        m_observer->onStopComplete();
    }
    changeState(kTSCSessionStateInitialized);
}
    
void
TSCSessionImpl::reject()
{
}
    
void
TSCSessionImpl::ignore()
{
}
    
void
TSCSessionImpl::ringing()
{
}

#pragma mark-
    
void
TSCSessionImpl::processParticipantConnect(const TSCParticipantConnectionRef& participant, const TSCErrorObjectRef& error)
{
    if (error.get() == nullptr) {
        TS_CORE_LOG_DEBUG("Participant %s connected", participant->getParticipant().getAddress().c_str());
        changeState(kTSCSessionStateInProgress);
    } else {
        changeState(kTSCSessionStateStartFailed);
        participant->stop();
    }
    if(m_observer.get() != nullptr) {
        TSCParticipantObjectRef participant_object = new TSCParticipantObject(participant->getParticipant());
        m_observer->onParticipantConnect(participant_object, error);
    }
}
    
void
TSCSessionImpl::changeState(const TSCSessionState state)
{
    m_state = state;
    if(m_observer.get()) {
        m_observer->onStateChange(state);
    }
}

#pragma mark-
    
void
TSCSessionImpl::onDidIceGatheringComplete(const TSCParticipantConnectionRef& connection, const TSCErrorObjectRef& error)
{
    if(m_observer.get() != nullptr) {
        m_observer->onStartComplete(error);
    }
}
    
void
TSCSessionImpl::onDidSetSessionLocalDescription(const TSCParticipantConnectionRef& connection, const TSCErrorObjectRef& error)
{
    if (error.get() != nullptr) {
        TS_CORE_LOG_INFO("Session: failed to set local offer due to: %s", error->getMessage().c_str());
        if(m_observer.get() != nullptr) {
            m_observer->onStartComplete(error);
        }
    } else {
        TS_CORE_LOG_INFO("Session: local offer set succesfully");
    }
}
    
void
TSCSessionImpl::onDidSetSessionRemoteDescription(const TSCParticipantConnectionRef& connection, const TSCErrorObjectRef& error)
{
    if (error.get() != nullptr) {
        TS_CORE_LOG_INFO("Session: failed to set remote offer due to: %s", error->getMessage().c_str());
        processParticipantConnect(connection, error);
    } else {
        TS_CORE_LOG_INFO("Session: remote offer set succesfully");
    }
}
    
void
TSCSessionImpl::onDidConnectPeerConnection(const TSCParticipantConnectionRef& connection, const TSCErrorObjectRef& error)
{
    processParticipantConnect(connection, error);
}

void
TSCSessionImpl::onDidDisconnectPeerConnection(const TSCParticipantConnectionRef& connection, TSCDisconnectReason reason)
{
    if(m_observer.get() != nullptr) {
        TSCParticipantObjectRef participant = new TSCParticipantObject(connection->getParticipant());
        m_observer->onParticipantDisconect(participant, reason);
        TS_CORE_LOG_DEBUG("Participant %s disconnected", participant->getAddress().c_str());
    }
}

#pragma mark-

void
TSCSessionImpl::onDidAddStream(const TSCParticipantConnectionRef& connection, const MediaStreamInterfaceRef& stream, TSCMediaStreamOrigin origin)
{
    TSCMediaStreamInfoObjectRef info = TSCMediaStreamUtils::createMediaStreamInfo(getId(), stream.get(),
                                               connection->getParticipant().getAddress(), origin);

    if(m_observer.get() != nullptr) {
       m_observer->onMediaStreamAdd(info.get());
    }

    if(m_video_surface.get() != nullptr)
    {
        std::vector<TSCVideoTrackInfo> videoTracks = info->getVideoTracks();
        for (unsigned int i = 0; i < videoTracks.size(); ++i)
        {
            TSCVideoTrackInfoObjectRef infoTrack = new TSCVideoTrackInfoObject(videoTracks[i].getIdentity(),
                                                                               videoTracks[i].getParticipantAddress());
            m_video_surface->onAddTrack(infoTrack, stream->GetVideoTracks()[i].get());
        }
    }
}
    
void
TSCSessionImpl::onDidRemoveStream(const TSCParticipantConnectionRef& connection, const MediaStreamInterfaceRef& stream, TSCMediaStreamOrigin origin)
{
    TSCMediaStreamInfoObjectRef info = TSCMediaStreamUtils::createMediaStreamInfo(getId(), stream.get(),
                                                                connection->getParticipant().getAddress(), origin);

    if(m_observer.get() != nullptr) {
        m_observer->onMediaStreamRemove(info.get());
    }
    
    if(m_video_surface.get() != nullptr)
    {
        std::vector<TSCVideoTrackInfo> videoTracks = info->getVideoTracks();
        for (unsigned int i = 0; i < videoTracks.size(); ++i)
        {
            TSCVideoTrackInfoObjectRef infoTrack = new TSCVideoTrackInfoObject(videoTracks[i].getIdentity(),
                                                                               videoTracks[i].getParticipantAddress());
            m_video_surface->onRemoveTrack(infoTrack);
        }
    }
}
    
#pragma mark-
    
void
TSCSessionImpl::on_call_state(pjsua_call_id call_id, pjsip_event *e)
{

}

void
TSCSessionImpl::on_call_sdp_created(pjsua_call_id call_id,
                                    pjmedia_sdp_session *sdp,
                                    pj_pool_t *pool,
                                    const pjmedia_sdp_session *rem_sdp)
{
    m_participant_connections[0]->on_call_sdp_created(call_id, sdp, pool, rem_sdp);
}
    
void
TSCSessionImpl::on_call_rx_offer(pjsua_call_id call_id,
                                    const pjmedia_sdp_session *offer,
                                    void *reserved,
                                    pjsip_status_code *code,
                                    pjsua_call_setting *opt)
{
}
    
void
TSCSessionImpl::on_call_tsx_state(pjsua_call_id call_id,
                                     pjsip_transaction *tsx,
                                     pjsip_event *e)
{
}

#pragma mark-

void
TSCSessionImpl::onDidLinkAudioInputController(IAudioInputControllerInterface* controller)
{
    m_audio_input_controller->setImpl(controller);
}
    
void
TSCSessionImpl::onDidUnlinkAudioInputController(IAudioInputControllerInterface* controller)
{
    m_audio_input_controller->setImpl(nullptr);
}

void
TSCSessionImpl::onDidLinkVideoCaptureController(IVideoCaptureControllerInterface* controller)
{
    m_video_capture_controller->setImpl(controller);
}
    
void
TSCSessionImpl::onDidUnlinkVideoCaptureController(IVideoCaptureControllerInterface* controller)
{
    m_video_capture_controller->setImpl(nullptr);
}

#pragma mark-
    
IAudioInputControllerInterface*
TSCSessionImpl::getAudioInputController() const
{
    return m_audio_input_controller.get();
}
    
IVideoCaptureControllerInterface*
TSCSessionImpl::getVideoCaptureController() const
{
    return m_video_capture_controller.get();
}

} // namespace twiliosdk
