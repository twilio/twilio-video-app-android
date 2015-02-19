//
//  TSCThreadManager.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 02/03/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCThreadManager.h"
#include "TSCThreadMonitor.h"
#include "TSCLogger.h"
#include "talk/base/asyncinvoker.h"

namespace twiliosdk {

class TSCThreadManager::TImpl
{
public:
    
    static TImpl& instance()
    {
        static TImpl instance;
        return instance;
    }
    
    ~TImpl()
    {
    }
    
    void destroyThread(talk_base::Thread* thread);
    
private:
    
    TImpl()
    {
        m_thread.Start(new TSCThreadMonitor("TSCThreadManager", this));
    }

    void onDestroyThread(talk_base::Thread* thread);

    talk_base::AsyncInvoker m_invoker;
    talk_base::Thread m_thread;
};

#pragma mark-
    
void
TSCThreadManager::TImpl::destroyThread(talk_base::Thread* thread)
{
    if(thread == nullptr)
       return;
    
    if(!thread->IsCurrent())
    {
        TS_CORE_LOG_DEBUG("destroyThread %p", thread);
        onDestroyThread(thread);
        return;
    } else
    {
        thread->Clear(nullptr);
    }
    
    m_invoker.AsyncInvoke<void>(&m_thread, talk_base::Bind(&TSCThreadManager::TImpl::onDestroyThread, this, thread));
}

void
TSCThreadManager::TImpl::onDestroyThread(talk_base::Thread* thread)
{
    thread->Stop();
    delete thread;
}
    
#pragma mark-
    
TSCThreadManager::TSCThreadManager()
{
}

TSCThreadManager::~TSCThreadManager()
{
}
    
#pragma mark-

void
TSCThreadManager::destroyThread(talk_base::Thread* thread)
{
    return TImpl::instance().destroyThread(thread);
}
    
}