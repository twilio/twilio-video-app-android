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

#include "com_twilio_video_TwilioException.h"
#include "jni_utils.h"
#include "webrtc/sdk/android/src/jni/jni_helpers.h"

namespace twilio_video_jni {

jobject createJavaTwilioException(JNIEnv *env,
                                  jclass j_twilio_exception_class,
                                  jmethodID j_twilio_exception_ctor_id,
                                  const twilio::video::TwilioError &twilio_error) {
    jobject j_twilio_exception = env->NewObject(j_twilio_exception_class,
                                                j_twilio_exception_ctor_id,
                                                twilio_error.getCode(),
                                                JavaUTF16StringFromStdString(env, twilio_error.getMessage()),
                                                JavaUTF16StringFromStdString(env, twilio_error.getExplanation()));
    CHECK_EXCEPTION(env) << "Failed to create TwilioException";

    return j_twilio_exception;
}

}
