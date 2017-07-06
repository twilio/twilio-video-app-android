#ifndef VIDEO_ANDROID_LOGGING_H_
#define VIDEO_ANDROID_LOGGING_H_

#include "video/video.h"

/*
 * Convenience log macro that inserts the file, function, and line into a log message.
 */
#define VIDEO_ANDROID_LOG(module, level, format, ...) \
    twilio_video_jni::log(module, \
                          level, \
                          __FILE__, \
                          __PRETTY_FUNCTION__, \
                          __LINE__, \
                          format, \
                          ##__VA_ARGS__);

namespace twilio_video_jni {

void log(twilio::video::LogModule module,
         twilio::video::LogLevel level,
         const char* file,
         const char* func,
         int line,
         const char* fmt,
         ...);

}

#endif // VIDEO_ANDROID_LOGGING_H_
