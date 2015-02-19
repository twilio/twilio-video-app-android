//
//  TSCoreSDK.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 12/29/14.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#ifndef TSC_SDK_H
#define TSC_SDK_H

#include "TSCoreSDKTypes.h"

namespace twiliosdk {

class TSCSDK
{
public:
    static TSCSDK* instance();
    static void destroy();
    
    bool isInitialized() const;
    
    TSCEndpointObjectRef createEndpoint(const TSCOptions& options, TSCEndpointObserverObjectRef observer);
    TSCDeviceManagerObjectRef createDeviceManager();
    
    void addExternalVideoCodec(ITSCVideoCodec* codec);
    
private:
    TSCSDK();
    virtual ~TSCSDK();
    
    TSCSDK(const TSCSDK&);
    TSCSDK& operator=(TSCSDK&);
    
    static TSCSDK* s_instance;
    
    class TState;
    talk_base::scoped_ptr<TState> m_state;

};
    
}  // namespace twiliosdk

#endif  // TSC_SDK_H
