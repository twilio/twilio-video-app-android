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

#ifndef VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALDATATRACK_H_
#define VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALDATATRACK_H_

#include <jni.h>
#include "twilio/media/track.h"
#include "twilio/media/data_track.h"

namespace twilio_video_jni {

struct LocalDataTrackContext {
    std::shared_ptr<twilio::media::LocalDataTrack> local_data_track;
};

std::shared_ptr<twilio::media::LocalDataTrack> getLocalDataTrack(jlong);

std::string getLocalDataTrackHash(std::shared_ptr<twilio::media::LocalDataTrack> local_data_track);

jobject createJavaLocalDataTrack(std::shared_ptr<twilio::media::LocalDataTrack> local_data_track,
                                 jobject j_context);

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_twilio_video_LocalDataTrack_nativeBufferSend(JNIEnv *,
                                                                             jobject,
                                                                             jlong,
                                                                             jbyteArray);

JNIEXPORT void JNICALL Java_com_twilio_video_LocalDataTrack_nativeStringSend(JNIEnv *,
                                                                             jobject,
                                                                             jlong,
                                                                             jstring);

JNIEXPORT void JNICALL Java_com_twilio_video_LocalDataTrack_nativeRelease(JNIEnv *,
                                                                          jobject,
                                                                          jlong);

}

#ifdef __cplusplus
}
#endif

#endif //VIDEO_ANDROID_COM_TWILIO_VIDEO_LOCALDATATRACK_H_
