#include "logging.h"

namespace twilio_video_jni {

const static int kLogBufferSize = 8096;

/*
 * Log function that formats var args into std::string.
 */
void log(twilio::video::TSCoreLogModule module,
         twilio::video::TSCoreLogLevel level,
         const char* file,
         const char* func,
         int line,
         const char* fmt,
         ...) {
    char buffer[kLogBufferSize];
    va_list args;
    va_start(args, fmt);
    int result = vsnprintf(buffer, sizeof(buffer), fmt, args);
    va_end(args);

    if (result > 0) {
        std::string message = buffer;
        twilio::video::logLine(module, level, file, func, line, message);
    }
}

}