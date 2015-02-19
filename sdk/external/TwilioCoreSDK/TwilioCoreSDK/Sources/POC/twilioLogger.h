#ifndef TWILIOLOGGER_H_
#define TWILIOLOGGER_H_

#include <string>

#include "Poco/Logger.h"
#include "Poco/LogStream.h"

namespace twiliosdk {

enum LogLevel {
    LEVEL_NO_LOG = 0,
    LEVEL_FATAL = 1,   /// A fatal error. The application will most likely terminate. This is the highest priority.
    LEVEL_CRITICAL,    /// A critical error. The application might not be able to continue running successfully.
    LEVEL_ERROR,       /// An error. An operation did not complete successfully, but the application as a whole is not affected.
    LEVEL_WARNING,     /// A warning. An operation completed with an unexpected result.
    LEVEL_NOTICE,      /// A notice, which is an information with just a higher priority.
    LEVEL_INFORMATION, /// An informational message, usually denoting the successful completion of an operation.
    LEVEL_DEBUG,       /// A debugging message.
    LEVEL_TRACE        /// A tracing message. This is the lowest priority.
};

struct TwilioLoggerConfig {

    TwilioLoggerConfig(LogLevel log_level, std::string file) :
        log_level_(log_level),
        file_(file) {}

    LogLevel log_level_;
    std::string file_;

};

/**
 * Basic logger functionality
 */
class TwilioLogger {
public:
    static void init(TwilioLoggerConfig& config);
    
    static Poco::Logger& logger();
    static Poco::LogStream& logger_stream();
private:
    TwilioLogger();
    ~TwilioLogger();
};
}

#define LOG_DEBUG(msg) TwilioLogger::logger().debug(msg)
#define LOG_INFO(msg)  TwilioLogger::logger().information(msg)
#define LOG_WARN(msg)  TwilioLogger::logger().warning(msg)
#define LOG_ERROR(msg) TwilioLogger::logger().error(msg)
#define LOG_FATAL(msg) TwilioLogger::logger().fatal(msg)

#define LOG_DEBUG_STREAM TwilioLogger::logger_stream().debug()
#define LOG_INFO_STREAM  TwilioLogger::logger_stream().information()
#define LOG_WARN_STREAM  TwilioLogger::logger_stream().warning()
#define LOG_ERROR_STREAM TwilioLogger::logger_stream().error()
#define LOG_FATAL_STREAM TwilioLogger::logger_stream().fatal()

#endif /* TWILIOLOGGER_H_ */
