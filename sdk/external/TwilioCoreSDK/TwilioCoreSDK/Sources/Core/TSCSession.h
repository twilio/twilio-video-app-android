//
//  TSCSession.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_SESSION_H
#define TSC_SESSION_H

#include <pjsua-lib/pjsua.h>
#include <pjsua-lib/pjsua_internal.h>

#include "TSCoreSDKTypes.h"
#include "TSCSessionLifeCycleObserver.h"
#include "TSCParticipant.h"

namespace twiliosdk {
    
class TSCSessionImpl;

class TSCSession: public talk_base::RefCountInterface
{
public:
    virtual ~TSCSession();

    void setSessionLifeCycleObserver(const TSCSessionLifeCycleObserverObjectRef& lifeCycleObserver);
    
    uint64 getId() const;

    void setParticipants(const std::vector<TSCParticipant>& participants);
    const std::vector<TSCParticipant> getParticipants() const;
    void setVideoSurface(const TSCVideoSurfaceObjectRef& videoSurface);
    
    void start();
    void stop();

    // call session callbacks
    virtual void on_call_state(pjsua_call_id call_id, pjsip_event *e);
    virtual void on_call_sdp_created(pjsua_call_id call_id,
                             pjmedia_sdp_session *sdp,
                             pj_pool_t *pool,
                             const pjmedia_sdp_session *rem_sdp);
    virtual void on_call_rx_offer(pjsua_call_id call_id,
                          const pjmedia_sdp_session *offer,
                          void *reserved,
                          pjsip_status_code *code,
                          pjsua_call_setting *opt);
    virtual void on_call_tsx_state(pjsua_call_id call_id,
                           pjsip_transaction *tsx,
                           pjsip_event *e);
    
    // media input controllers
    IAudioInputControllerInterface* getAudioInputController() const;
    IVideoCaptureControllerInterface* getVideoCaptureController() const;
    
protected:
    TSCSession(TSCSessionImpl* impl);
private:
    TSCSession();

protected:
    talk_base::scoped_refptr<TSCSessionImpl> m_impl;

private:
    TSCSessionLifeCycleObserverObjectRef m_lifecycle_observer;
};
    
}  // namespace twiliosdk

#endif  // TSC_SESSION_H
