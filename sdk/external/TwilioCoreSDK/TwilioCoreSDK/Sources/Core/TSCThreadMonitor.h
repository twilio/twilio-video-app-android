//
//  TSCThreadMonitor.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 02/03/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_THREAD_MONITOR_H
#define TSC_THREAD_MONITOR_H

#include "TSCoreSDKTypes.h"
#include "talk/base/thread.h"

namespace twiliosdk {

class TSCThreadMonitor: public talk_base::Runnable
{
public:
    TSCThreadMonitor(const std::string& name, void* object = nullptr);
    virtual ~TSCThreadMonitor();
    
    virtual void Run(talk_base::Thread* thread);
    
private:
    std::string m_thread_name;
};
    
}

#endif //TSC_THREAD_MONITOR_H
