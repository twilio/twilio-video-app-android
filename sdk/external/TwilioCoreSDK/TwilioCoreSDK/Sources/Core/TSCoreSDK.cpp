//
//  TSCoreSDK.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 12/29/14.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#include "TSCoreSDK.h"
#include "TSCEndpoint.h"
#include "TSCEndpointObserver.h"
#include "TSCPJSUA.h"
#include "TSCLogger.h"
#include "TSCDeviceManager.h"
#include "TSCVideoCodecManager.h"

#include "ITSCVideoCodec.h"
// #include "TSCVersion.h" // temporary disabled to speed up linking & compilations

#include "talk/base/openssladapter.h"

namespace twiliosdk {

class TSCSDK::TState
{
public:
    
    TState()
    {
        m_pjsip = new TSCPJSUAObject();
        talk_base::OpenSSLAdapter::InitializeSSL(nullptr);
    }
    
    ~TState()
    {
        talk_base::CleanupSSL();
    }
    
    bool isInitialized() const
    {
        bool result = false;
        if(m_pjsip.get() != nullptr)
           result = m_pjsip->isInitialized();
        return result;
    }

    TSCPJSUAObjectRef getPJSIP() const
    {
        return m_pjsip;
    }
    
private:
    TSCPJSUAObjectRef m_pjsip;
};

#pragma mark-
    
TSCSDK* TSCSDK::s_instance = nullptr;

TSCSDK::TSCSDK()
{
    m_state.reset(new TState());
}

TSCSDK::~TSCSDK()
{
}

#pragma mark-
    
TSCSDK*
TSCSDK::instance()
{
    if (s_instance == nullptr) {
        TSCLogger::instance()->setLogLevel(twiliosdk::kTSCoreLogLevelTrace);
        s_instance = new TSCSDK();
    }
    return s_instance;
}

#pragma mark-
    
void
TSCSDK::destroy()
{
    if (s_instance != nullptr) {
        delete s_instance;
        s_instance = nullptr;
    }
}
    
#pragma mark-
    
bool
TSCSDK::isInitialized() const
{
    return m_state.get()->isInitialized();
}

#pragma mark-
    
TSCEndpointObjectRef
TSCSDK::createEndpoint(const TSCOptions& options, TSCEndpointObserverObjectRef observer)
{
    TSCEndpointObject* result = new TSCEndpointObject(m_state->getPJSIP(), options, observer);
    return TSCEndpointObjectRef(result);
}

#pragma mark-
    
TSCDeviceManagerObjectRef
TSCSDK::createDeviceManager()
{
    TSCDeviceManagerObject* result = new TSCDeviceManagerObject();
    return result;
}
    
#pragma mark-
void
TSCSDK::addExternalVideoCodec(ITSCVideoCodec* codec)
{
    TSCVideoCodecManager codec_manager;
    TSCVideoCodecRef codec_ref(codec);
    codec_manager.addVideoCodec(codec_ref);
}

} // namespace twiliosdk
