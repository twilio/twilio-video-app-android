//
//  TSCPJSUA.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 12/26/14.
//  Copyright (c) 2014 Twilio. All rights reserved.
//

#ifndef TSC_TSCPJSUA_H
#define TSC_TSCPJSUA_H

#include "TSCoreSDKTypes.h"

namespace twiliosdk {

class TSCEndpoint;
    
class TSCPJSUA
{
public:
    TSCPJSUA();
    virtual ~TSCPJSUA();
    
    bool isInitialized() const;
    
    void registerEndpoint(const TSCEndpointObjectRef& endpoint);
    void unregisterEndpoint(const TSCEndpointObjectRef& endpoint);

private:
    class TState;
    talk_base::scoped_ptr<TState> m_state;
};
    
}  // namespace twiliosdk

#endif  // TSC_TSCPJSUA_H
