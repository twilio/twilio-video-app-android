//
//  TSCAudioInputController.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/27/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_AUDIO_INPUT_CONTROLLER_H
#define TSC_AUDIO_INPUT_CONTROLLER_H

#include "TSCoreSDKTypes.h"
#include "TSCSessionMediaControllers.h"

namespace twiliosdk {

class TSCAudioInputControllerImpl;
class TSCAudioInputController: public IAudioInputControllerInterface
{
public:
    TSCAudioInputController();
    virtual ~TSCAudioInputController();
    
    void setImpl(IAudioInputControllerInterface* impl);
    
    virtual void setObserver(IAudioInputControllerObserverInterface* observer);
    
    virtual bool isMuted() const;
    virtual void setMuted(bool muted);
    
    virtual bool isValid() const;
    
private:
    TSCAudioInputController(const TSCAudioInputController&);
    TSCAudioInputController& operator=(TSCAudioInputController&);
    
    talk_base::scoped_refptr<IAudioInputControllerInterface> m_impl;
    talk_base::scoped_refptr<IAudioInputControllerObserverInterface> m_observer;
    
    mutable talk_base::CriticalSection m_lock;
};
    
}

#endif // TSC_AUDIO_INPUT_CONTROLLER_H
