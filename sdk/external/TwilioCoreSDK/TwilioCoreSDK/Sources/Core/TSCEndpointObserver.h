//
//  TSCEndpointObserver.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 12/30/14.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#ifndef TSC_ENDPOINT_OBSERVER_H
#define TSC_ENDPOINT_OBSERVER_H

#include "TSCoreSDKTypes.h"

#include "talk/base/asyncinvoker.h"
#include "talk/base/thread.h"

namespace twiliosdk {

class TSCEndpointObserver
{
public:
    TSCEndpointObserver();
    virtual ~TSCEndpointObserver();
    
    void onRegistrationComplete(const TSCErrorObjectRef& error = nullptr);
    void onUnregistrationComplete(const TSCErrorObjectRef& error = nullptr);
    void onStateChange(TSCEndpointState state);
    void onReceiveIncomingCall(const TSCIncomingSessionObjectRef& session);
    
protected:
    virtual void onRegistrationDidComplete(TSCErrorObject* error);
    virtual void onUnregistrationDidComplete(TSCErrorObject* error);
    virtual void onStateDidChange(TSCEndpointState state);
    virtual void onIncomingCallDidReceive(TSCIncomingSession* session);

private:
    void onRegistrationCompletePriv(TSCErrorObject* error);
    void onUnregistrationCompletePriv(TSCErrorObject* error);
    void onReceiveIncomingCallPriv(TSCIncomingSessionObject* session);
    
private:
    talk_base::AsyncInvoker m_invoker;
    talk_base::scoped_ptr<talk_base::Thread> m_thread;
};
    
}  // namespace twiliosdk

#endif  // TSC_ENDPOINT_OBSERVER_H
