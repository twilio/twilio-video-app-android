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

#include "logging.h"

namespace twilio_video_jni {

const static int kLogBufferSize = 8096;

/*
 * Log function that formats var args into std::string.
 */
void log(twilio::LogModule module,
         twilio::LogLevel level,
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
        twilio::logLine(module, level, file, func, line, message);
    }
}

}
