//
//  TSCScreenCaptureProvider.cpp
//  TwilioCoreSDK
//
//  Created by Serhiy Semenyuk on 1/28/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCScreenCaptureProvider.h"
#include "TSCThreadMonitor.h"
#include "TSCLogger.h"
#include "TSCSIPUtils.h"

namespace twiliosdk {
    
TSCScreenCaptureProvider::TSCScreenCaptureProvider()
{
    TSCSIPUtils::generateUniqueId("", m_id);
    m_thread.Start(new TSCThreadMonitor("TSCScreenCaptureProvider", this));
}

TSCScreenCaptureProvider::~TSCScreenCaptureProvider()
{
    m_thread.Stop();
}
    
#pragma mark-
    
void
TSCScreenCaptureProvider::start(const int frameRate, ITSCScreenCaptureDataCallback* recipient)
{
    if(recipient != nullptr)
        recipient->AddRef();
    m_invoker.AsyncInvoke<void>(&m_thread, talk_base::Bind(&TSCScreenCaptureProvider::startPriv, this, frameRate, recipient));
}
    
void
TSCScreenCaptureProvider::onStart(const int frameRate, ITSCScreenCaptureDataCallback* recipient)
{
    
}
    
void
TSCScreenCaptureProvider::startPriv(const int frameRate, ITSCScreenCaptureDataCallback* recipient)
{
    onStart(frameRate, recipient);
    recipient->Release();
}

#pragma mark-
    
void
TSCScreenCaptureProvider::stop()
{
    onStop();
    //m_invoker.AsyncInvoke<void>(&m_thread, talk_base::Bind(&TSCScreenCaptureProvider::onStop, this));
}

void
TSCScreenCaptureProvider::onStop()
{
    
}

std::string
TSCScreenCaptureProvider::getId() const
{
    return m_id;
}

} //namespace twiliosdk