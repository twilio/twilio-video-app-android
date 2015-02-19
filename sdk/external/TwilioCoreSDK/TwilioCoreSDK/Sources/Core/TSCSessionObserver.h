//
//  TSCSessionObserver.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/16/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_SESSION_OBSERVER_H
#define TSC_SESSION_OBSERVER_H

#include "TSCoreSDKTypes.h"

#include "talk/base/asyncinvoker.h"
#include "talk/base/thread.h"

namespace twiliosdk {

class TSCSessionObserver
{
public:
    TSCSessionObserver();
    virtual ~TSCSessionObserver();
    
    void onReceiveEvent(const TSCEventObjectRef& event = nullptr);
    
    void onStateChange(TSCSessionState state);

    void onStartComplete(const TSCErrorObjectRef& error = nullptr);
    void onStopComplete(const TSCErrorObjectRef& error = nullptr);
    
    void onParticipantConnect(const TSCParticipantObjectRef& participant,
                              const TSCErrorObjectRef& error = nullptr);
    void onParticipantDisconect(const TSCParticipantObjectRef& participant,
                                TSCDisconnectReason reason);

    void onMediaStreamAdd(TSCMediaStreamInfoObject* stream);
    void onMediaStreamRemove(TSCMediaStreamInfoObject* stream);

protected:
    virtual void onDidReceiveEvent(const TSCEventObjectRef& event);
    
    virtual void onStateDidChange(TSCSessionState state);
    
    virtual void onStartDidComplete(const TSCErrorObjectRef& error);
    virtual void onStopDidComplete(const TSCErrorObjectRef& error);

    virtual void onParticipantDidConnect(const TSCParticipantObjectRef& participant,
                                         const TSCErrorObjectRef& error);
    virtual void onParticipantDidDisconect(const TSCParticipantObjectRef& participant,
                                           TSCDisconnectReason reason);

    virtual void onMediaStreamDidAdd(TSCMediaStreamInfoObject* stream);
    virtual void onMediaStreamDidRemove(TSCMediaStreamInfoObject* stream);
    
private:
    void onReceiveEventPriv(TSCEventObject* event);
    
    void onStartCompletePriv(TSCErrorObject* error);
    void onStopCompletePriv(TSCErrorObject* error);

    void onParticipantConnectPriv(TSCParticipantObject* participant, TSCErrorObject* error);
    void onParticipantDisconectPriv(TSCParticipantObject* participant, TSCDisconnectReason reason);
    
    void onMediaStreamAddPriv(TSCMediaStreamInfoObject* stream);
    void onMediaStreamRemovePriv(TSCMediaStreamInfoObject* stream);
    
private:
    talk_base::AsyncInvoker m_invoker;
    talk_base::scoped_ptr<talk_base::Thread> m_thread;
};
    
}  // namespace twiliosdk

#endif  // TSC_SESSION_OBSERVER_H
