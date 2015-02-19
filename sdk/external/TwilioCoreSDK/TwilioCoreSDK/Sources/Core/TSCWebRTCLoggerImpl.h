//
//  TSCWebRTCLoggerImpl.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/23/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_WEBRTC_LOGGER_IMPL_H
#define TSC_WEBRTC_LOGGER_IMPL_H

#include "TSCoreSDKTypes.h"
#include "TSCLogger.h"

namespace twiliosdk {

class TSCWebRTCLoggerImpl
{
public:
    TSCWebRTCLoggerImpl();
    virtual ~TSCWebRTCLoggerImpl();

    void setLogLevel(TSCoreLogLevel level);
    
private:
    TSCWebRTCLoggerImpl(const TSCWebRTCLoggerImpl&);
    TSCWebRTCLoggerImpl& operator=(TSCWebRTCLoggerImpl&);
    
    class TImpl;
    talk_base::scoped_ptr<TImpl> m_impl;
};
    
}

#endif //TSC_WEBRTC_LOGGER_IMPL_H
