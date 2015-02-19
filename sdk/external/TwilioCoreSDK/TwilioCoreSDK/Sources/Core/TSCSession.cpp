//
//  TSCSession.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCSession.h"
#include "TSCSessionImpl.h"
#include "TSCSessionObserver.h"
#include "TSCLogger.h"

namespace twiliosdk {

TSCSession::TSCSession(TSCSessionImpl* impl)
{
    m_impl = impl;
}

TSCSession::~TSCSession()
{
    m_lifecycle_observer = nullptr;
    stop();
    // clear participants
    std::vector<TSCParticipant> empty_list;
    setParticipants(empty_list);
}

#pragma mark-

void
TSCSession::setSessionLifeCycleObserver(const TSCSessionLifeCycleObserverObjectRef& lifeCycleObserver)
{
    m_lifecycle_observer = lifeCycleObserver;
}
    
#pragma mark-
    
void
TSCSession::setParticipants(const std::vector<TSCParticipant>& participants)
{
    m_impl->setParticipants(participants);
}
    
const std::vector<TSCParticipant>
TSCSession::getParticipants() const
{
    return m_impl->getParticipants();
}

void
TSCSession::setVideoSurface(const TSCVideoSurfaceObjectRef& videoSurface)
{
    m_impl->setVideoSurface(videoSurface);
}
    
#pragma mark-
    
void
TSCSession::start()
{
    m_impl->start();
    if(m_lifecycle_observer.get() != nullptr)
       m_lifecycle_observer->onSessionStarted(this);
}
    
void
TSCSession::stop()
{
    m_impl->stop();
    if(m_lifecycle_observer.get() != nullptr)
       m_lifecycle_observer->onSessionStoped(this);
}

#pragma mark-
    
uint64
TSCSession::getId() const
{
    return m_impl->getId();
}

#pragma mark-

void
TSCSession::on_call_state(pjsua_call_id call_id, pjsip_event *e)
{
    m_impl->on_call_state(call_id, e);
}
    
void
TSCSession::on_call_sdp_created(pjsua_call_id call_id,
                                pjmedia_sdp_session *sdp,
                                pj_pool_t *pool,
                                const pjmedia_sdp_session *rem_sdp)
{
    m_impl->on_call_sdp_created(call_id, sdp, pool, rem_sdp);
}
    
void
TSCSession::on_call_rx_offer(pjsua_call_id call_id,
                             const pjmedia_sdp_session *offer,
                             void *reserved,
                             pjsip_status_code *code,
                             pjsua_call_setting *opt)
{
    m_impl->on_call_rx_offer(call_id, offer, reserved, code, opt);
}
    
void
TSCSession::on_call_tsx_state(pjsua_call_id call_id,
                              pjsip_transaction *tsx,
                              pjsip_event *e)
{
    m_impl->on_call_tsx_state(call_id, tsx, e);
}
    
#pragma mark-
    
IAudioInputControllerInterface*
TSCSession::getAudioInputController() const
{
    return m_impl->getAudioInputController();
}
    
IVideoCaptureControllerInterface*
TSCSession::getVideoCaptureController() const
{
    return m_impl->getVideoCaptureController();
}
    
} // namespace twiliosdk
