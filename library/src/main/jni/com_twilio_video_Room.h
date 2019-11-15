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

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_ROOM_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_ROOM_H_

#include <jni.h>
#include <memory>

#include "twilio/video/video.h"
#include "twilio/media/stats_observer.h"
#include "twilio/video/room.h"
#include "android_room_observer.h"
#include "android_stats_observer.h"
#include "com_twilio_video_ConnectOptions.h"

#ifdef __cplusplus
extern "C" {
#endif

namespace twilio_video_jni {

JNIEXPORT jlong JNICALL Java_com_twilio_video_Room_nativeConnect
        (JNIEnv *, jobject, jobject, jobject, jobject, jlong, jobject);

JNIEXPORT jboolean JNICALL Java_com_twilio_video_Room_nativeIsRecording
        (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeDisconnect
        (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeGetStats
        (JNIEnv *, jobject, jlong, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeOnNetworkChange
        (JNIEnv *, jobject, jlong, jobject);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeReleaseRoom
        (JNIEnv *, jobject, jlong);

JNIEXPORT void JNICALL Java_com_twilio_video_Room_nativeRelease
        (JNIEnv *, jobject, jlong);

}

#ifdef __cplusplus
}
#endif
#endif // VIDEO_ANDROID_COM_TWILIO_VIDEO_ROOM_H_
