//
//  TSCWebRTCLoggerImpl.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/23/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCWebRTCLoggerImpl.h"
#include "TSCLogger.h"

#include "talk/base/stream.h"
#include "talk/base/logging.h"

namespace twiliosdk {

class TSCLogRouteStream: public talk_base::StreamInterface
{
public:
    TSCLogRouteStream(int logLevel):m_log_level(logLevel){}
    
    virtual talk_base::StreamState GetState() const { return talk_base::SS_OPEN; }
    virtual talk_base::StreamResult Read(void* buffer, size_t buffer_len,
                                         size_t* read, int* error)
    {
        return talk_base::SR_ERROR;
    }
    virtual talk_base::StreamResult Write(const void* data, size_t data_len,
                                          size_t* written, int* error)
    {
        std::string message((const char*)data, data_len);
        switch (m_log_level)
        {
            case talk_base::LS_SENSITIVE:
                TS_CORE_LOG_MODULE(kTSCoreLogModuleWebRTC, twiliosdk::kTSCoreLogLevelTrace, "%s", message.c_str());
                break;
            case talk_base::LS_VERBOSE:    
                TS_CORE_LOG_MODULE(kTSCoreLogModuleWebRTC, twiliosdk::kTSCoreLogLevelNotice, "%s", message.c_str());
                break;
            case talk_base::LS_ERROR:
                TS_CORE_LOG_MODULE(kTSCoreLogModuleWebRTC, twiliosdk::kTSCoreLogLevelError, "%s", message.c_str());
                break;
            case talk_base::LS_WARNING:
                TS_CORE_LOG_MODULE(kTSCoreLogModuleWebRTC, twiliosdk::kTSCoreLogLevelWarning, "%s", message.c_str());
                break;
            case talk_base::LS_INFO:
                TS_CORE_LOG_MODULE(kTSCoreLogModuleWebRTC, twiliosdk::kTSCoreLogLevelInfo, "%s", message.c_str());
                break;
            default:
                TS_CORE_LOG_MODULE(kTSCoreLogModuleWebRTC, twiliosdk::kTSCoreLogLevelDebug, "%s", message.c_str()); break;
        }
        if(written)
           *written = data_len;
        if(error)
          *error = 0;
        return talk_base::SR_SUCCESS;
    }
    
    virtual void Close() {}
    virtual bool SetPosition(size_t position)
    {
        return true;
    }
    virtual bool GetPosition(size_t* position) const
    {
        if (position) *position = 0;
        return true;
    }
    virtual bool GetSize(size_t* size) const
    {
        return false;
    }
    virtual bool GetAvailable(size_t* size) const
    {
        return false;
    }

private:
    
    int m_log_level;
};

class TSCWebRTCLoggerImpl::TImpl
{
public:
    
    TImpl()
    {
        // set max log level for redirection
        talk_base::LogMessage::LogToDebug(talk_base::LS_ERROR + 1);
        clearRedirection();
    }
    
    ~TImpl()
    {
    }
    
    void clearRedirection()
    {
        // empty all previously installed streams
        talk_base::LogMessage::LogToStream(nullptr, talk_base::LS_SENSITIVE);
    }
    
    void setLogLevel(TSCoreLogLevel level)
    {
        clearRedirection();
        
        int targetLogLevel = -1;
        switch (level)
        {
            case kTSCoreLogLevelFatal:
            case kTSCoreLogLevelCritical:
            case kTSCoreLogLevelError:
                targetLogLevel = talk_base::LS_ERROR;
                break;
                
            case kTSCoreLogLevelWarning:
                targetLogLevel = talk_base::LS_WARNING;
                break;
                
            case kTSCoreLogLevelNotice:
            case kTSCoreLogLevelInfo:
                targetLogLevel = talk_base::LS_INFO;
                break;
                
            case kTSCoreLogLevelDebug:
                targetLogLevel = talk_base::LS_VERBOSE;
                break;

            case kTSCoreLogLevelTrace:
                targetLogLevel = talk_base::LS_SENSITIVE;
                break;
                
            default:{}
        }
        if(targetLogLevel != -1)
           talk_base::LogMessage::AddLogToStream(new TSCLogRouteStream(targetLogLevel), targetLogLevel);
    }
};

#pragma mark-

TSCWebRTCLoggerImpl::TSCWebRTCLoggerImpl()
{
    m_impl.reset(new TImpl());
}

TSCWebRTCLoggerImpl::~TSCWebRTCLoggerImpl()
{
}

void
TSCWebRTCLoggerImpl::setLogLevel(TSCoreLogLevel level)
{
    m_impl->setLogLevel(level);
}
    
}