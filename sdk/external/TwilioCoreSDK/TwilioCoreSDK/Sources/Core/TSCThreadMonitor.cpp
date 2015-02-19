//
//  TSCThreadMonitor.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 02/03/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCThreadMonitor.h"

namespace twiliosdk {

TSCThreadMonitor::TSCThreadMonitor(const std::string& name, void* object)
{
    m_thread_name = name;
    if(object != nullptr)
    {
        char buf[128];
        sprintf(buf, " [%p]", object);
        m_thread_name += (std::string) buf;
    }
}
    
TSCThreadMonitor::~TSCThreadMonitor()
{
}

#pragma mark-
    
void
TSCThreadMonitor::Run(talk_base::Thread* thread)
{
#ifdef ANDROID

#else
    pthread_setname_np(m_thread_name.c_str());
#endif
    thread->Run();
    delete this;
}
    
}
