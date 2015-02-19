//
//  TSCSessionDescriptionObservers.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/13/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_SESSION_DESCRIPTION_OBSERVER_H
#define TSC_SESSION_DESCRIPTION_OBSERVER_H

#include "TSCoreSDKTypes.h"
#include "talk/app/webrtc/jsep.h"
#include "TSCoreError.h"

namespace twiliosdk {

template <class T>
class TSCCreateSessionLocalDescriptionObserver: public webrtc::CreateSessionDescriptionObserver
{
public:
    
    TSCCreateSessionLocalDescriptionObserver(T* delegate)
    {
        m_delegate = delegate;
    }

    virtual void OnSuccess(webrtc::SessionDescriptionInterface* desc)
    {
        m_delegate->onCreateSessionLocalDescription(desc, nullptr);
    }
    
    virtual void OnFailure(const std::string& errorMessage)
    {
        TSCErrorObjectRef error = new TSCErrorObject(kTSCoreSDKErrorDomain,
                                                     kTSCErrorSessionDescriptionCreationFailed,
                                                     errorMessage);
        m_delegate->onCreateSessionLocalDescription(nullptr, error);
    }
    
private:
    
    T* m_delegate;
};

template <class T>
class TSCSetSessionLocalDescriptionObserver : public webrtc::SetSessionDescriptionObserver
{
public:
    
    TSCSetSessionLocalDescriptionObserver(T* delegate)
    {
        m_delegate = delegate;
    }
    
    virtual void OnSuccess()
    {
        m_delegate->onSetSessionLocalDescription(nullptr);
    }
    
    virtual void OnFailure(const std::string& errorMessage)
    {
        TSCErrorObjectRef error = new TSCErrorObject(kTSCoreSDKErrorDomain,
                                                     kTSCErrorSessionDescriptionSetupFailed,
                                                     errorMessage);
        m_delegate->onSetSessionLocalDescription(error);
    }
    
private:

    T* m_delegate;
};
    
template <class T>
class TSCSetSessionRemoteDescriptionObserver : public webrtc::SetSessionDescriptionObserver
{
public:
    
    TSCSetSessionRemoteDescriptionObserver(T* delegate)
    {
        m_delegate = delegate;
    }
    
    virtual void OnSuccess()
    {
        m_delegate->onSetSessionRemoteDescription(nullptr);
    }
    
    virtual void OnFailure(const std::string& errorMessage)
    {
        TSCErrorObjectRef error = new TSCErrorObject(kTSCoreSDKErrorDomain,
                                                     kTSCErrorSessionDescriptionSetupFailed,
                                                     errorMessage);
        m_delegate->onSetSessionRemoteDescription(error);
    }
    
private:
    
    T* m_delegate;
};

}

#endif  // TSC_SESSION_DESCRIPTION_OBSERVER_H
