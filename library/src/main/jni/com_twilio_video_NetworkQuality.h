/*
 * Copyright (C) 2020 Twilio, Inc.
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

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_NETWORK_QUALITY_H
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_NETWORK_QUALITY_H

#include "webrtc/sdk/android/src/jni/jni_helpers.h"
#include "twilio/video/network_quality.h"

namespace twilio_video_jni {

#ifdef __cplusplus
extern "C" {
#endif

jobject createJavaNetworkQualityLevel(JNIEnv *env, twilio::video::NetworkQualityLevel level);

#ifdef __cplusplus
}
#endif

twilio::video::NetworkQualityConfiguration getCoreNetworkQualityConfiguration(JNIEnv *env, jobject j_network_quality_config);

}

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_NETWORK_QUALITY_H
