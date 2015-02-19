//
//  TSCLogger.cpp
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/06/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#include "TSCLogger.h"
#include <string>

#include "Poco/Logger.h"
#include "Poco/Message.h"
#include "Poco/FormattingChannel.h"
#include "Poco/Formatter.h"
#include "Poco/PatternFormatter.h"
#include "Poco/AutoPtr.h"

#include "TSCWebRTCLoggerImpl.h"

namespace twiliosdk {

class TSCFormattingChannel : public Poco::FormattingChannel
{
public:
    TSCFormattingChannel(Poco::Formatter* pFormatter, Poco::Channel* pChannel):
    Poco::FormattingChannel(pFormatter, pChannel)
    {}
    
    void log(const Poco::Message& msg)
    {
        // add more details to message
        Poco::Message message = msg;
        message.setTid((long)pthread_self());
        
        Poco::FormattingChannel::log(message);
    }
};

typedef Poco::AutoPtr<Poco::PatternFormatter> TSCPatternFormatterRef;
typedef Poco::AutoPtr<TSCFormattingChannel> TSCFormattingChannelRef;
    
class TSCProxyLogChannel : public Poco::Channel
{
public:
    
    TSCProxyLogChannel(const TSCLogChannelRef& channel)
    {
        m_log_channel = channel;
    }
    virtual ~TSCProxyLogChannel(){}
    
    void log(const Poco::Message& msg)
    {
        std::string text = msg.getText();
        if(m_log_channel.get() != nullptr)
           m_log_channel->log(text);
    }

private:
    TSCProxyLogChannel();
    
    TSCLogChannelRef m_log_channel;
};

#pragma mark-
    
TSCLogChannel::TSCLogChannel()
{
}

TSCLogChannel::~TSCLogChannel()
{
}
    
void
TSCLogChannel::log(const std::string& message)
{
    printf("%s", message.c_str());
}

#pragma mark-
    
class TSCLogger::TImpl
{
public:
    
    TImpl():m_logger(Poco::Logger::create("TSCore", nullptr, 0))
    {
        m_log_module_mask = (kTSCoreLogModuleCoreSDK | kTSCoreLogModuleSignalSDK |
                             kTSCoreLogModulePJSIP | kTSCoreLogModuleWebRTC);

        m_webrtc_logger.reset(new TSCWebRTCLoggerImpl());
    }
    
    ~TImpl()
    {
    }

    void setLogLevel(TSCoreLogLevel level)
    {
        int pocoLogLevel = 0;
        switch(level)
        {
            case kTSCoreLogDisabled:
                pocoLogLevel = 0;
            break;
            case kTSCoreLogLevelFatal:
                pocoLogLevel = Poco::Message::PRIO_FATAL;
                break;
            case kTSCoreLogLevelCritical:
                pocoLogLevel = Poco::Message::PRIO_CRITICAL;
                break;
            case kTSCoreLogLevelError:
                pocoLogLevel = Poco::Message::PRIO_ERROR;
                break;
            case kTSCoreLogLevelWarning:
                pocoLogLevel = Poco::Message::PRIO_WARNING;
                break;
            case kTSCoreLogLevelNotice:
                pocoLogLevel = Poco::Message::PRIO_NOTICE;
                break;
            case kTSCoreLogLevelInfo:
                pocoLogLevel = Poco::Message::PRIO_INFORMATION;
                break;
            case kTSCoreLogLevelDebug:
                pocoLogLevel = Poco::Message::PRIO_DEBUG;
                break;
            case kTSCoreLogLevelTrace:
                pocoLogLevel = Poco::Message::PRIO_TRACE;
                break;
            default:{}
        }
        m_log_level = level;
        m_logger.setLevel(level);
        
        // update webrtc logger
        m_webrtc_logger->setLogLevel(level);
    }

    TSCoreLogLevel getLogLevel() const
    {
        return m_log_level;
    }
    
    void setLogChannel(TSCLogChannel* channel)
    {
        m_log_proxy_channel.reset(new TSCProxyLogChannel(channel));

        TSCPatternFormatterRef formatter(new Poco::PatternFormatter());
        formatter->setProperty("pattern", "TWSDK: %t");
        
        TSCFormattingChannelRef formattingChannel(new TSCFormattingChannel(formatter.get(), m_log_proxy_channel.get()));
        m_logger.setChannel(formattingChannel.get());
    }

    void setLogModule(int mask)
    {
        m_log_module_mask = mask;
    }
    
    void log(TSCoreLogModuleType module, TSCoreLogLevel level, const char* file, int line, const std::string& message)
    {
        if ((module & m_log_module_mask) == 0)
           return;
        
        std::string log_msg = createModulePrefix(module) + message;
        switch (level)
        {
            case kTSCoreLogLevelFatal:
                m_logger.fatal(log_msg, file, line);
            break;

            case kTSCoreLogLevelCritical:
                m_logger.critical(log_msg, file, line);
            break;
                
            case kTSCoreLogLevelError:
                m_logger.error(log_msg, file, line);
            break;
                
            case kTSCoreLogLevelWarning:
                m_logger.warning(log_msg, file, line);
            break;

            case kTSCoreLogLevelNotice:
                m_logger.notice(log_msg, file, line);
            break;

            case kTSCoreLogLevelInfo:
                m_logger.information(log_msg, file, line);
            break;

            case kTSCoreLogLevelDebug:
                m_logger.debug(log_msg, file, line);
            break;

            case kTSCoreLogLevelTrace:
                m_logger.trace(log_msg, file, line);
            break;
                
            default:
                break;
        }
    }
    
    std::string createModulePrefix(TSCoreLogModuleType module)
    {
        char buf[16];
        sprintf(buf, "##[%d]##:", (int) module);
        return (std::string) buf;
    }
    
private:
    Poco::Logger& m_logger;
    TSCoreLogLevel m_log_level;
    int m_log_module_mask;
    talk_base::scoped_ptr<TSCProxyLogChannel> m_log_proxy_channel;

    talk_base::scoped_ptr<TSCWebRTCLoggerImpl> m_webrtc_logger;
};

#pragma mark-

TSCLogger* TSCLogger::s_instance = nullptr;

TSCLogger::TSCLogger()
{
    m_impl.reset(new TImpl());
}

TSCLogger::~TSCLogger()
{
}
    
#pragma mark-
    
TSCLogger*
TSCLogger::instance()
{
    if (s_instance == nullptr) {
        s_instance = new TSCLogger();
        s_instance->setLogChannel();
    }
    return s_instance;
}

#pragma mark-

void
TSCLogger::setLogChannel(TSCLogChannel* channel)
{
    m_impl->setLogChannel(channel);
}
    
void
TSCLogger::setLogLevel(TSCoreLogLevel level)
{
    m_impl->setLogLevel(level);
}

void
TSCLogger::setLogModule(int mask)
{
    m_impl->setLogModule(mask);
}
    
#pragma mark-
    
void
TSCLogger::log(TSCoreLogModuleType module, TSCoreLogLevel level, const char* file, int line, const char* fmt, ...)
{
    char buffer[4096];
    
    va_list args;
    va_start(args, fmt);
    int result = vsnprintf(buffer, sizeof(buffer), fmt, args);
    va_end(args);
    
    if(result > 0)
    {
        std::string message = buffer;
        m_impl->log(module, level, file, line, message);
    }
}

void
TSCLogger::log(TSCoreLogModuleType module, TSCoreLogLevel level, const char* file, int line, const std::string& message)
{
    m_impl->log(module, level, file, line, message);
}
    
}