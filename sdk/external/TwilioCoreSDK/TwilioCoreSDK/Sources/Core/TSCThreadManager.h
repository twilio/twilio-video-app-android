//
//  TSCThreadManager.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 02/03/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_THREAD_MANAGER_H
#define TSC_THREAD_MANAGER_H

#include "TSCoreSDKTypes.h"
#include "talk/base/thread.h"

namespace twiliosdk {
    
class TSCThreadManager
{
public:
    TSCThreadManager();
    virtual ~TSCThreadManager();
    
    static void destroyThread(talk_base::Thread* thread);
    
private:
    TSCThreadManager(const TSCThreadManager&);
    TSCThreadManager& operator=(TSCThreadManager&);
    
    class TImpl;
};
    
}

#endif // TSC_THREAD_MANAGER_H
