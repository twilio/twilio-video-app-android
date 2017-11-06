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

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_TWILIOEXCEPTION_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_TWILIOEXCEPTION_H_

#include <jni.h>
#include "video/twilio_error.h"

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

static const char *const kTwilioExceptionConstructoSignature = "("
        "I"
        "Ljava/lang/String;"
        "Ljava/lang/String;"
        ")V";

jobject createJavaTwilioException(JNIEnv *env,
                                  jclass j_twilio_exception_class,
                                  jmethodID j_twilio_exception_ctor_id,
                                  const twilio::video::TwilioError &twilio_error);

};

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_TWILIOEXCEPTION_H_
