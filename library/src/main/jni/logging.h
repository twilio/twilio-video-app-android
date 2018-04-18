/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
