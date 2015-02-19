//
//  TSCAudioInputControllerObserver.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/29/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_AUDIO_INPUT_CONTROLLER_OBSERVER_H
#define TSC_AUDIO_INPUT_CONTROLLER_OBSERVER_H

#include "TSCoreSDKTypes.h"
#include "TSCSessionMediaControllers.h"

#include "talk/base/asyncinvoker.h"
#include "talk/base/thread.h"

namespace twiliosdk {

class TSCAudioInputControllerObserver: public IAudioInputControllerObserverInterface
{
public:
    TSCAudioInputControllerObserver();
    virtual ~TSCAudioInputControllerObserver();
    
    void onStateChanged(bool muted);
    
protected:
    virtual void onStateDidChange(bool muted);
    
private:
    talk_base::AsyncInvoker m_invoker;
    talk_base::scoped_ptr<talk_base::Thread> m_thread;
};
    
}  // namespace twiliosdk

#endif  // TSC_AUDIO_INPUT_CONTROLLER_OBSERVER_H
