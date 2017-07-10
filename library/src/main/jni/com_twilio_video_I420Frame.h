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

#include <jni.h>

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_I420FRAME_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_I420FRAME_H_

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

JNIEXPORT void JNICALL Java_com_twilio_video_I420Frame_nativeRelease(JNIEnv *, jobject, jlong);

}

#ifdef __cplusplus
}
#endif

#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_I420FRAME_H_
