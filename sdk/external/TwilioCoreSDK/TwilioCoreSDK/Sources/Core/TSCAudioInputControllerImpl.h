//
//  TSCAudioInputControllerImpl.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/27/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_AUDIO_INPUT_CONTROLLER_IMPL_H
#define TSC_AUDIO_INPUT_CONTROLLER_IMPL_H

#include "TSCoreSDKTypes.h"
#include "TSCSessionMediaControllers.h"

#include "talk/app/webrtc/mediastreaminterface.h"

namespace twiliosdk {

class TSCAudioInputControllerImpl: public IAudioInputControllerInterface
{
public:
    TSCAudioInputControllerImpl(webrtc::AudioTrackInterface* audioTrack);
    virtual ~TSCAudioInputControllerImpl();
    
    virtual void setObserver(IAudioInputControllerObserverInterface* observer);
    
    bool isMuted() const;
    void setMuted(bool muted);
    
    virtual bool isValid() const;
    
private:
    TSCAudioInputControllerImpl();
    TSCAudioInputControllerImpl(const TSCAudioInputControllerImpl&);
    TSCAudioInputControllerImpl& operator=(TSCAudioInputControllerImpl&);
    
    bool isMutedPriv() const;
    void setState(bool muted);
    
    talk_base::scoped_refptr<webrtc::AudioTrackInterface> m_audio_track;
    talk_base::scoped_refptr<IAudioInputControllerObserverInterface> m_observer;
    
    bool m_muted;
};
    
}

#endif // TSC_AUDIO_INPUT_CONTROLLER_IMPL_H
