//
//  TSCSessionLifeCycleObserver.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/19/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_SESSION_LIFECYCLE_OBSERVER_H
#define TSC_SESSION_LIFECYCLE_OBSERVER_H

#include "TSCoreSDKTypes.h"

#include "talk/base/asyncinvoker.h"
#include "talk/base/thread.h"

namespace twiliosdk {

class TSCEndpoint;
class TSCSessionLifeCycleObserver
{
public:
    TSCSessionLifeCycleObserver();
    virtual ~TSCSessionLifeCycleObserver();
    
    void setDelegate(TSCEndpoint* delegate);
    
    void onSessionStarted(TSCSession* session);
    void onSessionStoped(TSCSession* session);

protected:

    void onSessionDidStart(TSCSession* session);
    void onSessionDidStop(TSCSession* session);
    
private:
    
    void onSessionStartedPriv(TSCSession* session);
    void onSessionStoppedPriv(TSCSession* session);
    
private:
    talk_base::AsyncInvoker m_invoker;
    talk_base::scoped_ptr<talk_base::Thread> m_thread;
    
    TSCEndpoint* m_delegate;
};
    
}  // namespace twiliosdk

#endif  // TSC_SESSION_LIFECYCLE_OBSERVER_H
