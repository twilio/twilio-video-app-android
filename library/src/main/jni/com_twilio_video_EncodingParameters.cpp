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

#include "com_twilio_video_EncodingParameters.h"

#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "jni_utils.h"

namespace twilio_video_jni {

twilio::media::EncodingParameters getEncodingParameters(JNIEnv *env,
                                                        jobject j_encoding_parameters) {
    jclass j_encoding_parameters_class = GetObjectClass(env, j_encoding_parameters);
    jfieldID j_max_audio_bitrate_field_id = GetFieldID(env,
                                                       j_encoding_parameters_class,
                                                       "maxAudioBitrate",
                                                       "I");
    jfieldID j_max_video_bitrate_field_id = GetFieldID(env,
                                                       j_encoding_parameters_class,
                                                       "maxVideoBitrate",
                                                       "I");
    twilio::media::EncodingParameters encoding_parameters;
    encoding_parameters.max_audio_bitrate_ = (unsigned long)
            GetIntField(env,
                        j_encoding_parameters,
                        j_max_audio_bitrate_field_id);
    CHECK_EXCEPTION(env) << "Failed to get maxAudioBitrate field";
    encoding_parameters.max_video_bitrate_ = (unsigned long)
            GetIntField(env,
                        j_encoding_parameters,
                        j_max_video_bitrate_field_id);
    CHECK_EXCEPTION(env) << "Failed to get maxVideoBitrate field";

    return encoding_parameters;
}

}
