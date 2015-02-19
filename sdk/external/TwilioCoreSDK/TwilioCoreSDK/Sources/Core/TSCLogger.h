//
//  TSCLogger.h
//  Twilio Signal Core SDK
//
//  Created by Alexander Trishyn on 01/08/15.
//  Copyright (c) 2015 Twilio. All rights reserved.
//

#ifndef TSC_LOGGER_H
#define TSC_LOGGER_H

#include "TSCoreSDKTypes.h"

#define TS_CORE_LOG_FATAL(...)                \
    twiliosdk::TSCLogger::instance()->log(kTSCoreLogModuleCoreSDK, twiliosdk::kTSCoreLogLevelFatal, __FILE__, __LINE__, "" __VA_ARGS__);

#define TS_CORE_LOG_CRITICAL(...)                \
    twiliosdk::TSCLogger::instance()->log(kTSCoreLogModuleCoreSDK, twiliosdk::kTSCoreLogLevelCritical, __FILE__, __LINE__, "" __VA_ARGS__);

#define TS_CORE_LOG_ERROR(...)                \
    twiliosdk::TSCLogger::instance()->log(kTSCoreLogModuleCoreSDK, twiliosdk::kTSCoreLogLevelError, __FILE__, __LINE__, "" __VA_ARGS__);

#define TS_CORE_LOG_WARNING(...)                \
    twiliosdk::TSCLogger::instance()->log(kTSCoreLogModuleCoreSDK, twiliosdk::kTSCoreLogLevelWarning, __FILE__, __LINE__, "" __VA_ARGS__);

#define TS_CORE_LOG_NOTICE(...)                \
    twiliosdk::TSCLogger::instance()->log(kTSCoreLogModuleCoreSDK, twiliosdk::kTSCoreLogLevelNotice, __FILE__, __LINE__, "" __VA_ARGS__);

#define TS_CORE_LOG_INFO(...)                \
    twiliosdk::TSCLogger::instance()->log(kTSCoreLogModuleCoreSDK, twiliosdk::kTSCoreLogLevelInfo, __FILE__, __LINE__, "" __VA_ARGS__);

#define TS_CORE_LOG_DEBUG(...)                \
    twiliosdk::TSCLogger::instance()->log(kTSCoreLogModuleCoreSDK, twiliosdk::kTSCoreLogLevelDebug, __FILE__, __LINE__, "" __VA_ARGS__);

#define TS_CORE_LOG_TRACE(...)                \
    twiliosdk::TSCLogger::instance()->log(kTSCoreLogModuleCoreSDK, twiliosdk::kTSCoreLogLevelTrace, __FILE__, __LINE__, "" __VA_ARGS__);

#define TS_CORE_LOG_MODULE(module, level, ...)                \
    twiliosdk::TSCLogger::instance()->log(module, level, __FILE__, __LINE__, "" __VA_ARGS__);

namespace twiliosdk {

typedef enum _TSCoreLogLevel {
    kTSCoreLogDisabled = 0,
    kTSCoreLogLevelFatal,
    kTSCoreLogLevelCritical,
    kTSCoreLogLevelError,
    kTSCoreLogLevelWarning,
    kTSCoreLogLevelNotice,
    kTSCoreLogLevelInfo,
    kTSCoreLogLevelDebug,
    kTSCoreLogLevelTrace
} TSCoreLogLevel;

typedef enum _TSCoreLogModuleType {
    kTSCoreLogModuleNone      = 0,
    kTSCoreLogModuleCoreSDK   = (1 << 0),
    kTSCoreLogModuleSignalSDK = (1 << 1),
    kTSCoreLogModulePJSIP     = (1 << 2),
    kTSCoreLogModuleWebRTC    = (1 << 3)
} TSCoreLogModuleType;
    
class TSCLogChannel: public talk_base::RefCountInterface
{
public:
    TSCLogChannel();
    virtual ~TSCLogChannel();
    
    virtual void log(const std::string& message);
};
    
class TSCLogger
{
public:
    static TSCLogger* instance();
    
    void setLogChannel(TSCLogChannel* channel = new talk_base::RefCountedObject<TSCLogChannel>());
    void setLogLevel(TSCoreLogLevel level);
    void setLogModule(int mask);
    
    void log(TSCoreLogModuleType module, TSCoreLogLevel level, const char* file, int line, const char* fmt, ...);
    void log(TSCoreLogModuleType module, TSCoreLogLevel level, const char* file, int line, const std::string& message);
    
private:
    TSCLogger();
    virtual ~TSCLogger();
    
    TSCLogger(const TSCLogger&);
    TSCLogger& operator=(TSCLogger&);
    
    static TSCLogger* s_instance;
    
    class TImpl;
    talk_base::scoped_ptr<TImpl> m_impl;
};
    
}

#endif //TSC_LOGGER_H
